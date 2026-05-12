package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.PaEligibilityTreeProvider

@Service
class EligibilityService(

  private val accommodationQueryService: AccommodationQueryService,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val caseRepository: CaseRepository,
  private val dutyToReferQueryService: DutyToReferQueryService,
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  private val cas1Tree: Cas1EligibilityTreeProvider,
  private val cas3Tree: Cas3EligibilityTreeProvider,
  private val dtrTree: DtrEligibilityTreeProvider,
  private val crsTree: CrsEligibilityTreeProvider,
  private val paTree: PaEligibilityTreeProvider,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getEligibility(
    crn: String,
    gender: String,
    caseEntity: CaseEntity?,
    dutyToRefer: DutyToReferDto?,
  ): EligibilityDto {
    log.info("Calculating eligibility for CRN: $crn from the sas_case table")
    val data = DomainData(
      crn = crn,
      sexCode = SexCode.findByGender(gender),
      caseEntity,
      dutyToRefer,
    )
    return getEligibility(data)
  }

  fun getEligibility(crn: String): EligibilityDto {
    log.info("Calculating eligibility for CRN: $crn using external APIs")
    val data = getDomainData(crn)
    return getEligibility(data)
  }

  fun getEligibility(data: DomainData): EligibilityDto {
    log.debug(
      "Eligibility input data: crn={}, tierScore={}, sex={}, currentAccommodationEndDate={}, currentAccommodationStatus={}, currentAccommodationType={}, nextAccommodationStartDate={}, nextAccommodationStatus={}, nextAccommodationType={}",
      data.crn,
      data.tierScore,
      data.sex,
      data.currentAccommodation?.endDate,
      data.currentAccommodation?.status?.description,
      data.currentAccommodation?.type?.description,
      data.nextAccommodation?.startDate,
      data.nextAccommodation?.status?.description,
      data.nextAccommodation?.type?.description,
    )

    val cas1 = evaluate("CAS1", data, cas1Tree)
    val cas3 = evaluate("CAS3", data, cas3Tree)
    val crs = evaluate("CRS", data, crsTree)
    val dtr = evaluate("DTR", data, dtrTree)
    val pa = evaluate("PA", data, paTree)

    return EligibilityTransformer.toEligibilityDto(
      crn = data.crn,
      cas1 = cas1,
      cas3 = cas3,
      dtr = dtr,
      crs = crs,
      pa = pa,
      data = data,
    ).also { log.info("Finished calculating eligibility for CRN: ${data.crn}") }
  }

  internal fun evaluate(provider: EligibilityTreeProvider, data: DomainData): ServiceResult = provider.tree().eval(provider.initialContext(data))

  private fun evaluate(line: String, data: DomainData, provider: EligibilityTreeProvider): ServiceResult {
    log.info("Calculating $line eligibility for CRN: ${data.crn}")
    return evaluate(provider, data).also {
      log.info(
        "$line Service Result for CRN ${data.crn}: serviceStatus={}, action={}, link={}",
        it.serviceStatus,
        it.action,
        it.link,
      )
    }
  }

  fun getDomainData(crn: String): DomainData {
    val accommodationTypes = accommodationTypeRepository.findAll()
    val caseEntity = caseRepository.findByCrn(crn)

    val dutyToRefer = caseEntity?.let { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) }

    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn)
    val currentAccommodation = eligibilityOrchestrationDto.data.cpr?.addresses?.let {
      accommodationQueryService.getCurrentAccommodation(
        crn,
        addresses = it,
      )
    }

    val nextAccommodation = eligibilityOrchestrationDto.data.cpr?.addresses?.let {
      accommodationQueryService.getNextAccommodation(
        crn,
        addresses = it,
      )
    }

    if (eligibilityOrchestrationDto.upstreamFailures.isNotEmpty()) {
      log.error("Eligibility upstream failures for CRN {}: {}", crn, eligibilityOrchestrationDto.upstreamFailures)
    }

    return DomainData(
      crn = crn,
      cpr = eligibilityOrchestrationDto.data.cpr,
      tier = eligibilityOrchestrationDto.data.tier,
      cas1Application = eligibilityOrchestrationDto.data.cas1Application,
      cas3Application = eligibilityOrchestrationDto.data.cas3Application,
      currentAccommodation = currentAccommodation,
      nextAccommodation = nextAccommodation,
      dutyToRefer = dutyToRefer,
      // TODO connect to crs endpoint when it becomes available
      commissionedRehabilitativeServices = null,
      accommodationTypes = accommodationTypes,
    )
  }
}
