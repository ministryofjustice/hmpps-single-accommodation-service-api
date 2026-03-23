package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.CallsPerIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_CASE_LIST
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_PRISONER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.PrisonerSearchCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.ProbationIntegrationOasysCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.CaseList
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationsasdelius.ProbationIntegrationSasDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierCachingService

@Service
class CaseApplicationOrchestrationService(
  val aggregatorService: AggregatorService,
  val probationIntegrationSasDeliusCachingService: ProbationIntegrationSasDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
  val tierCachingService: TierCachingService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
  val prisonerSearchCachingService: PrisonerSearchCachingService,
) {

  fun getFreshCases(crns: List<String>): List<CaseApplicationOrchestrationDto> {
    val callsPerIdentifier = mapOf(
//      ApiCallKeys.GET_CORE_PERSON_RECORD to { crn: String -> corePersonRecordCachingService.getCorePersonRecord(crn) },
//      ApiCallKeys.GET_ROSH_DETAIL to { crn: String -> probationIntegrationOasysCachingService.getRoshDetail(crn) },
      ApiCallKeys.GET_TIER to { crn: String -> tierCachingService.getTier(crn) },
      ApiCallKeys.GET_CAS_1_APPLICATION to { crn: String -> approvedPremisesCachingService.getSuitableCas1Application(crn) },
      ApiCallKeys.GET_CAS_2_HDC_APPLICATION to { crn: String -> approvedPremisesCachingService.getSuitableCas2HdcApplication(crn) },
      ApiCallKeys.GET_CAS_2_PRISON_BAIL_APPLICATION to { crn: String -> approvedPremisesCachingService.getSuitableCas2PrisonBailApplication(crn) },
      ApiCallKeys.GET_CAS_2_COURT_BAIL_APPLICATION to { crn: String -> approvedPremisesCachingService.getSuitableCas2CourtBailApplication(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = emptyMap(),
      callsPerIdentifier = CallsPerIdentifier(
        identifiersToIterate = crns,
        calls = callsPerIdentifier,
      ),
    )
    return results.callsPerIdentifierResults!!.map { (crn, calls) ->
      val cpr = calls[ApiCallKeys.GET_CORE_PERSON_RECORD] as? CorePersonRecord
      val roshDetail = calls[ApiCallKeys.GET_ROSH_DETAIL] as? RoshDetails
      val tier = calls[ApiCallKeys.GET_TIER] as? Tier
      val cas1Application = calls[ApiCallKeys.GET_CAS_1_APPLICATION] as? Cas1Application
      val cas2HdcApplication = calls[ApiCallKeys.GET_CAS_2_HDC_APPLICATION] as? Cas2HdcApplication
      val cas2PrisonBailApplication = calls[ApiCallKeys.GET_CAS_2_PRISON_BAIL_APPLICATION] as? Cas2PrisonBailApplication
      val cas2CourtBailApplication = calls[ApiCallKeys.GET_CAS_2_COURT_BAIL_APPLICATION] as? Cas2CourtBailApplication

      CaseApplicationOrchestrationDto(
        crn,
        cpr,
        roshDetail,
        tier,
        cas1Application,
        cas2HdcApplication,
        cas2PrisonBailApplication,
        cas2CourtBailApplication,
      )
    }
  }

  fun getPartialCases(crns: List<String>): List<CaseApplicationOrchestrationDto> {
    val callsPerIdentifier = mapOf(
      ApiCallKeys.GET_CAS_1_APPLICATION to { crn: String -> approvedPremisesCachingService.getSuitableCas1Application(crn) },
      ApiCallKeys.GET_CAS_2_HDC_APPLICATION to { crn: String -> approvedPremisesCachingService.getSuitableCas2HdcApplication(crn) },
      ApiCallKeys.GET_CAS_2_PRISON_BAIL_APPLICATION to { crn: String -> approvedPremisesCachingService.getSuitableCas2PrisonBailApplication(crn) },
      ApiCallKeys.GET_CAS_2_COURT_BAIL_APPLICATION to { crn: String -> approvedPremisesCachingService.getSuitableCas2CourtBailApplication(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = emptyMap(),
      callsPerIdentifier = CallsPerIdentifier(
        identifiersToIterate = crns,
        calls = callsPerIdentifier,
      ),
    )
    return results.callsPerIdentifierResults!!.map { (crn, calls) ->
      val cas1Application = calls[ApiCallKeys.GET_CAS_1_APPLICATION] as? Cas1Application
      val cas2HdcApplication = calls[ApiCallKeys.GET_CAS_2_HDC_APPLICATION] as? Cas2HdcApplication
      val cas2PrisonBailApplication = calls[ApiCallKeys.GET_CAS_2_PRISON_BAIL_APPLICATION] as? Cas2PrisonBailApplication
      val cas2CourtBailApplication = calls[ApiCallKeys.GET_CAS_2_COURT_BAIL_APPLICATION] as? Cas2CourtBailApplication

      CaseApplicationOrchestrationDto(
        crn,
        null,
        null,
        null,
        cas1Application,
        cas2HdcApplication,
        cas2PrisonBailApplication,
        cas2CourtBailApplication,
      )
    }
  }

  fun getPrisoners(prisonerNumbers: List<String>): List<Prisoner> {
    val calls = prisonerNumbers.associate {
      "$GET_PRISONER$it" to { prisonerSearchCachingService.getPrisoner(it) }
    }

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    return prisonerNumbers.mapNotNull {
      results.standardCallsNoIterationResults!!["$GET_PRISONER$it"] as? Prisoner
    }
  }

//  fun getPrisoners(prisonerNumbers: List<String>): List<Prisoner> {
//    val bulkCall = mapOf<String, () -> CaseSummaries>(
//      // TODO add in once the bulk end points are created
// //      GET_PRISONERS to { prisonerSearchCachingService.getPrisoners(prisonerNumbers) }
//    )
//
//    val results = aggregatorService.orchestrateAsyncCalls(
//      standardCallsNoIteration = bulkCall,
//    )
//
//    return results.standardCallsNoIterationResults!![GET_PRISONERS] as? List<Prisoner> ?: emptyList()
//  }

  fun getCaseList(username: String): CaseList {
    val bulkCall = mapOf(
      GET_CASE_LIST to { probationIntegrationSasDeliusCachingService.getCaseList(username) },
    )

    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = bulkCall,
    )
    return results.standardCallsNoIterationResults
      ?.get(GET_CASE_LIST) as? CaseList
      ?: CaseList(emptyList())
  }

//  fun getFreshCases(crns: List<String>): List<CaseApplicationOrchestrationDto> {
//    // these are all non-existent bulk calls - we might want to switch these out for single calls
//    val bulkCalls = mapOf(
//
//      // TODO add in once we are no longer using case summary to get the data
// //      GET_CORE_PERSON_RECORDS to { corePersonRecordCachingService.getCorePersonRecords(crns) },
//      GET_TIERS to { tierCachingService.getTiers(crns) },
// //      GET_ROSH_DETAILS to { probationIntegrationOasysCachingService.getRoshDetails(crns) },
//      GET_CAS_1_APPLICATIONS to { approvedPremisesCachingService.getSuitableCas1Applications(crns) },
//      GET_CAS_2_HDC_APPLICATIONS to { approvedPremisesCachingService.getSuitableCas2HdcApplications(crns) },
//      GET_CAS_2_PRISON_BAIL_APPLICATIONS to { approvedPremisesCachingService.getSuitableCas2PrisonBailApplications(crns) },
//      GET_CAS_2_COURT_BAIL_APPLICATIONS to { approvedPremisesCachingService.getSuitableCas2CourtBailApplications(crns) },
//    )
//    val results = aggregatorService.orchestrateAsyncCalls(
//      standardCallsNoIteration = bulkCalls,
//    )
//
//    val cprs = results.standardCallsNoIterationResults!![GET_CORE_PERSON_RECORDS] as? List<CorePersonRecord>
//      ?: emptyList()
//    val tiers = results.standardCallsNoIterationResults!![GET_TIERS] as? List<Tier>
//      ?: emptyList()
//    val cas1Applications = results.standardCallsNoIterationResults!![GET_CAS_1_APPLICATIONS] as? List<Cas1Application>
//      ?: error("$GET_CAS_1_APPLICATIONS failed")
//    val cas2HdcApplications = results.standardCallsNoIterationResults!![GET_CAS_2_HDC_APPLICATIONS] as? List<Cas2HdcApplication>
//      ?: error("$GET_CAS_2_HDC_APPLICATIONS failed")
//    val cas2PrisonBailApplications = results.standardCallsNoIterationResults!![GET_CAS_2_PRISON_BAIL_APPLICATIONS] as? List<Cas2PrisonBailApplication>
//      ?: error("$GET_CAS_2_PRISON_BAIL_APPLICATIONS failed")
//    val cas2CourtBailApplications = results.standardCallsNoIterationResults!![GET_CAS_2_COURT_BAIL_APPLICATIONS] as? List<Cas2CourtBailApplication>
//      ?: error("$GET_CAS_2_COURT_BAIL_APPLICATIONS failed")
//    val roshDetails = results.standardCallsNoIterationResults!![GET_ROSH_DETAILS] as? List<RoshDetails>
//      ?: emptyList()
//
//    return crns.map { crn ->
//
//      val cpr = cprs.find { it.identifiers!!.crns.contains(crn) }
//      val tier = tiers.find { it.crn == crn }
//      val roshDetail = roshDetails.find { it.crn == crn }
//      val cas1Application = cas1Applications.find { it.crn == crn }
//      val cas2HdcApplication = cas2HdcApplications.find { it.crn == crn }
//      val cas2PrisonBailApplication = cas2PrisonBailApplications.find { it.crn == crn }
//      val cas2CourtBailApplication = cas2CourtBailApplications.find { it.crn == crn }
//
//      CaseApplicationOrchestrationDto(
//        crn,
//        cpr,
//        roshDetail,
//        tier,
//        cas1Application,
//        cas2HdcApplication,
//        cas2PrisonBailApplication,
//        cas2CourtBailApplication,
//      )
//    }
//  }
}
