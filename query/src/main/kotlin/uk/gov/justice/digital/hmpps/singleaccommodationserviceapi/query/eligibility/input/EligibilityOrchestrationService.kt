package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getFailures
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_IF_EXISTS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_CURRENT_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_1_RESIDENCE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_2_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CAS_3_CURRENT_PREMISES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CORE_PERSON_RECORD_BY_CRN
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CRS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_SEXUAL_OFFENCE_REGISTRATIONS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_TIER_V3
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingPremises
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServicesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SasAndDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class EligibilityOrchestrationService(
  val aggregatorService: AggregatorService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
  val commissionedRehabilitativeServicesCachingService: CommissionedRehabilitativeServicesCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val prisonerSearchCachingService: PrisonerSearchCachingService,
  val tierCachingService: TierCachingService,
  val sasAndDeliusCachingService: SasAndDeliusCachingService,
) {

  fun getData(crn: String, prisonNumber: String?): OrchestrationResultDto<EligibilityOrchestrationDto> {
    val calls = buildMap {
      put(GET_CORE_PERSON_RECORD_BY_CRN) { corePersonRecordCachingService.getCorePersonRecordByCrn(crn) }
      put(GET_TIER_V3) { tierCachingService.getTier(crn) }
      put(GET_CASE_IF_EXISTS) { sasAndDeliusCachingService.getCase(crn) }
      put(GET_SEXUAL_OFFENCE_REGISTRATIONS) { sasAndDeliusCachingService.getSexualOffenceRegistrations(crn) }
      put(GET_CAS_1_CURRENT_APPLICATION) { approvedPremisesCachingService.getCurrentCas1Application(crn) }
      put(GET_CAS_2_APPLICATION) { approvedPremisesCachingService.getSuitableCas2Application(crn) }
      put(GET_CAS_3_APPLICATION) { approvedPremisesCachingService.getSuitableCas3Application(crn) }
      put(GET_CRS) { commissionedRehabilitativeServicesCachingService.getCrs(crn) }
      put(GET_CAS_1_RESIDENCE) { approvedPremisesCachingService.getCas1Residence(crn) }
      put(GET_CAS_3_CURRENT_PREMISES) { approvedPremisesCachingService.getCas3CurrentPremises(crn) }
      prisonNumber?.let { num -> put(GET_PRISONER) { prisonerSearchCachingService.getPrisoner(num) } }
    }
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    ).standardCallsNoIterationResults!!

    val cpr = results.getResult<CorePersonRecord>(GET_CORE_PERSON_RECORD_BY_CRN)
    val tier = results.getResult<Tier>(GET_TIER_V3)
    val case = results.getResult<Case>(GET_CASE_IF_EXISTS)
    val sexualOffenceRegistrations = results.getResult<SexualOffenceRegistrations>(GET_SEXUAL_OFFENCE_REGISTRATIONS)
    val cas1Application = results.getResult<Cas1ApplicationV2>(GET_CAS_1_CURRENT_APPLICATION)
    val cas2Application = results.getResult<Cas2Application>(GET_CAS_2_APPLICATION)
    val cas3Application = results.getResult<Cas3Application>(GET_CAS_3_APPLICATION)
    val crs = results.getResult<List<CommissionedRehabilitativeServices>>(GET_CRS)
    val prisoner = results.getResult<Prisoner>(GET_PRISONER)
    val cas1Residence = results.getResult<Cas1PlacementV2>(GET_CAS_1_RESIDENCE)
    val cas3CurrentPremises = results.getResult<Cas3BookingPremises>(GET_CAS_3_CURRENT_PREMISES)

    return OrchestrationResultDto(
      data = EligibilityOrchestrationDto(
        crn = crn,
        cpr = cpr,
        tier = tier,
        cas1Application = cas1Application,
        cas2Application = cas2Application,
        cas3Application = cas3Application,
        commissionedRehabilitativeServices = crs,
        prisoner = prisoner,
        cas1Residence = cas1Residence,
        cas3CurrentPremises = cas3CurrentPremises,
        sexualOffenceRegistrations = sexualOffenceRegistrations,
        case = case,
      ),
      upstreamFailures = results.getFailures(),
    )
  }
}
