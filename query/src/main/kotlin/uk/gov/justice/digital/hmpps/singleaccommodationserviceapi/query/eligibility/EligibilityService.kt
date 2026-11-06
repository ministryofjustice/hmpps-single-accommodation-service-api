package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ApiResponseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.EligibilityDtoNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_CURRENT_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_CURRENT_PREMISES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_2_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_CURRENT_PREMISES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DeeplinkResolver
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.Cas1EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.Cas2EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.Cas3EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.CrsEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.DtrEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.trees.PaEligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EligibilityTreeProvider
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.EligibilityOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.output.EligibilityNewToOldTransformer.toEligibilityDtoOld
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.output.EligibilityTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.output.EligibilityTransformer.toFailedEligibilityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.shared.ApiResponseTransformer.toApiResponseDto

@Service
class EligibilityService(
  private val domainDataFactory: DomainDataFactory,
  private val caseRepository: CaseRepository,
  private val eligibilityOrchestrationService: EligibilityOrchestrationService,
  private val deeplinkResolver: DeeplinkResolver,
  private val cas1Tree: Cas1EligibilityTreeProvider,
  private val cas2Tree: Cas2EligibilityTreeProvider,
  private val cas3Tree: Cas3EligibilityTreeProvider,
  private val dtrTree: DtrEligibilityTreeProvider,
  private val crsTree: CrsEligibilityTreeProvider,
  private val paTree: PaEligibilityTreeProvider,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getEligibility(crn: String): ApiResponseDto<EligibilityDto> {
    log.debug("Calculating eligibility for CRN: {} using external APIs", crn)

    val caseEntity = caseRepository.findByCrn(crn)
    val prisonNumber = caseEntity?.latestPrisonNumber()

    val eligibilityOrchestrationDto = eligibilityOrchestrationService.getData(crn, prisonNumber)

    val casCallKeysWith404Failures = setOf(
      GET_CAS_1_APPLICATION,
      GET_CAS_1_CURRENT_APPLICATION,
      GET_CAS_2_APPLICATION,
      GET_CAS_3_APPLICATION,
      GET_CAS_1_CURRENT_PREMISES,
      GET_CAS_3_CURRENT_PREMISES,
    )

    val failuresRelevantToRulesEngine = eligibilityOrchestrationDto.upstreamFailures
      .filter { it.errorDetail.httpStatus != HttpStatus.NOT_FOUND || it.callKey in casCallKeysWith404Failures }
    if (failuresRelevantToRulesEngine.isNotEmpty()) {
      log.error("Eligibility upstream failures for CRN {}: {}", crn, failuresRelevantToRulesEngine)

      val failedEligibilityDto = toFailedEligibilityDto(crn)

      return toApiResponseDto(data = toEligibilityDtoOld(failedEligibilityDto), upstreamFailures = failuresRelevantToRulesEngine)
    }

    val data = domainDataFactory.buildDomainData(crn, eligibilityOrchestrationDto.data, caseEntity)
    val eligibility = getEligibility(data)

    return toApiResponseDto(data = toEligibilityDtoOld(eligibility), upstreamFailures = emptyList())
  }

  fun getEligibility(data: DomainData): EligibilityDtoNew {
    log.debug(
      "Eligibility input data: crn={}, tierScore={}, sex={}, currentAccommodationEndDate={}, currentAccommodationStatus={}, currentAccommodationType={}, nextAccommodationsSize={}",
      data.crn,
      data.tierScore,
      data.sex,
      data.currentAccommodation?.endDate,
      data.currentAccommodation?.status?.description,
      data.currentAccommodation?.type?.description,
      data.nextAccommodations.size,
    )

    val cas1 = evaluate("CAS1", data, cas1Tree)
    val cas2 = evaluate("CAS2", data, cas2Tree)
    val cas3 = evaluate("CAS3", data, cas3Tree)
    val crs = evaluate("CRS", data, crsTree)
    val dtr = evaluate("DTR", data, dtrTree)
    val pa = evaluate("PA", data, paTree)

    return EligibilityTransformer.toEligibilityDto(
      crn = data.crn,
      cas1 = cas1,
      cas2 = cas2,
      cas3 = cas3,
      dtr = dtr,
      crs = crs,
      pa = pa,
      data = data,
    ).also { log.debug("Eligibility result for CRN {}: {}", data.crn, it) }
  }

  internal fun evaluate(provider: EligibilityTreeProvider, data: DomainData): ServiceResultNew {
    val result = provider.tree().eval(provider.initialContext(data))
    return deeplinkResolver.resolve(result, data)
  }

  private fun evaluate(line: String, data: DomainData, provider: EligibilityTreeProvider): ServiceResultNew {
    log.debug("Calculating {} eligibility for CRN: {}}", line, data.crn)
    return evaluate(provider, data).also {
      log.debug(
        "$line Service Result for CRN ${data.crn}: serviceStatus={}",
        it.serviceStatus,
      )
    }
  }
}
