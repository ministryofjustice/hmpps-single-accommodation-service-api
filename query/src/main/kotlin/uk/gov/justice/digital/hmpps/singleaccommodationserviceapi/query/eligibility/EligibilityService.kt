package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation.AccommodationQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityTransformer.toFailedEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.Cas1EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.Cas3EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.crs.CrsEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.dtr.DtrEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.pa.PaEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

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
    log.debug("Calculating eligibility for CRN: {} from the sas_case table", crn)
    val data = DomainData(
      crn = crn,
      sexCode = SexCode.findByGender(gender),
      caseEntity,
      dutyToRefer,
    )
    return getEligibility(data)
  }

  fun getEligibility(crn: String): ApiResponseDto<EligibilityDto> {
    log.debug("Calculating eligibility for CRN: {} using external APIs", crn)

    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn)
    val upstreamFailures = eligibilityOrchestrationDto.upstreamFailures
    val blockingFailures = upstreamFailures.filterNot { it.errorDetail.httpStatus == HttpStatus.NOT_FOUND }

    if (blockingFailures.isNotEmpty()) {
      log.error("Eligibility upstream failures for CRN {}: {}", crn, blockingFailures)
      return toApiResponseDto(data = toFailedEligibilityDto(crn), upstreamFailures = upstreamFailures)
    }
    if (upstreamFailures.isNotEmpty()) {
      log.warn("Eligibility upstream 404s for CRN {}: {}", crn, upstreamFailures)
    }

    val data = buildDomainData(crn, eligibilityOrchestrationDto.data)
    val eligibility = getEligibility(data)
    return toApiResponseDto(data = eligibility, upstreamFailures = upstreamFailures)
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
    log.debug("Calculating {} eligibility for CRN: {}}", line, data.crn)
    return evaluate(provider, data).also {
      log.info(
        "$line Service Result for CRN ${data.crn}: serviceStatus={}, action={}, link={}",
        it.serviceStatus,
        it.action,
        it.link,
      )
    }
  }

  fun buildDomainData(crn: String, eligibilityOrchestrationDto: EligibilityOrchestrationDto): DomainData {
    val accommodationTypes = accommodationTypeRepository.findAll()
    val caseEntity = caseRepository.findByCrn(crn)

    val dutyToRefer = caseEntity?.let { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) }

    val currentAccommodation = eligibilityOrchestrationDto.cpr?.addresses?.let {
      accommodationQueryService.getCurrentAccommodation(
        crn,
        addresses = it,
      )
    }

    val nextAccommodation = eligibilityOrchestrationDto.cpr?.addresses?.let {
      accommodationQueryService.getNextAccommodation(
        crn,
        addresses = it,
      )
    }

    val suitableCrsReferral = eligibilityOrchestrationDto.commissionedRehabilitativeServices
      ?.filter { it.status == CrsReferralStatus.LIVE || it.status == CrsReferralStatus.COMPLETED }
      ?.filter { it.sentAt != null }
      ?.maxByOrNull { it.sentAt!! }

    return DomainData(
      crn = crn,
      cpr = eligibilityOrchestrationDto.cpr,
      tier = eligibilityOrchestrationDto.tier,
      cas1Application = eligibilityOrchestrationDto.cas1Application,
      cas3Application = eligibilityOrchestrationDto.cas3Application,
      currentAccommodation = currentAccommodation,
      nextAccommodation = nextAccommodation,
      dutyToRefer = dutyToRefer,
      commissionedRehabilitativeServices = suitableCrsReferral,
      accommodationTypes = accommodationTypes,
    )
  }
}
