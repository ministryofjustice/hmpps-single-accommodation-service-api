package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.CallsPerIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.ProbationIntegrationOasysCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierCachingService
import java.time.LocalDate
import kotlin.collections.component1
import kotlin.collections.component2

@Service
class CaseService(
  val aggregatorService: AggregatorService,
  val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
  val tierCachingService: TierCachingService,
) {
  fun getCases(crns: List<String>): List<Case> {
    val bulkCall = mapOf(
      ApiCallKeys.GET_CASE_SUMMARIES to { probationIntegrationDeliusCachingService.getCaseSummaries(crns) },
    )
    val callsPerIdentifier = mapOf(
      ApiCallKeys.GET_CORE_PERSON_RECORD to { crn: String -> corePersonRecordCachingService.getCorePersonRecord(crn) },
      ApiCallKeys.GET_ROSH_DETAIL to { crn: String -> probationIntegrationOasysCachingService.getRoshDetails(crn) },
      ApiCallKeys.GET_TIER to { crn: String -> tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = bulkCall,
      callsPerIdentifier = CallsPerIdentifier(
        identifiersToIterate = crns,
        calls = callsPerIdentifier,
      ),
    )
    val call1Results = results.standardCallsNoIterationResults
      ?.get(ApiCallKeys.GET_CASE_SUMMARIES) as? CaseSummaries
      ?: error("${ApiCallKeys.GET_CASE_SUMMARIES} failed")

    return results.callsPerIdentifierResults!!.map { (crn, calls) ->
      val caseSummary = call1Results.cases.first { it.crn == crn }

      val cpr = calls[ApiCallKeys.GET_CORE_PERSON_RECORD] as? CorePersonRecord
        ?: error("${ApiCallKeys.GET_CORE_PERSON_RECORD} failed for $crn")
      val roshDetails = calls[ApiCallKeys.GET_ROSH_DETAIL] as? RoshDetails
        ?: error("${ApiCallKeys.GET_ROSH_DETAIL} failed for $crn")
      val tier = calls[ApiCallKeys.GET_TIER] as? Tier
        ?: error("${ApiCallKeys.GET_TIER} failed for $crn")

      buildCase(crn, cpr, roshDetails, tier, caseSummary)
    }
  }

  fun getCase(crn: String): Case {
    val calls = mapOf(
      ApiCallKeys.GET_CASE_SUMMARY to { probationIntegrationDeliusCachingService.getCaseSummary(crn) },
      ApiCallKeys.GET_CORE_PERSON_RECORD to { corePersonRecordCachingService.getCorePersonRecord(crn) },
      ApiCallKeys.GET_ROSH_DETAIL to { probationIntegrationOasysCachingService.getRoshDetails(crn) },
      ApiCallKeys.GET_TIER to { tierCachingService.getTier(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val caseSummary = results.standardCallsNoIterationResults!![ApiCallKeys.GET_CASE_SUMMARY] as? CaseSummaries
      ?: error("${ApiCallKeys.GET_CASE_SUMMARY} failed for $crn")
    val cpr = results.standardCallsNoIterationResults!![ApiCallKeys.GET_CORE_PERSON_RECORD] as? CorePersonRecord
      ?: error("${ApiCallKeys.GET_CORE_PERSON_RECORD} failed for $crn")
    val roshDetails = results.standardCallsNoIterationResults!![ApiCallKeys.GET_ROSH_DETAIL] as? RoshDetails
      ?: error("${ApiCallKeys.GET_ROSH_DETAIL} failed for $crn")
    val tier = results.standardCallsNoIterationResults!![ApiCallKeys.GET_TIER] as? Tier
      ?: error("${ApiCallKeys.GET_TIER} failed for $crn")
    return buildCase(crn, cpr, roshDetails, tier, caseSummary.cases[0])
  }

  fun buildCase(crn: String, cpr: CorePersonRecord, roshDetails: RoshDetails, tier: Tier, caseSummary: CaseSummary): Case = Case(
    name = cpr.fullName,
    dateOfBirth = caseSummary.dateOfBirth,
    crn = crn,
    prisonNumber = cpr.identifiers?.prisonNumbers[0],
    tier = tier.tierScore,
    riskLevel = roshDetails.rosh.determineOverallRiskLevel(),
    pncReference = caseSummary.pnc,
    assignedTo = AssignedTo(1L, caseSummary.manager.team.name),
    currentAccommodation = CurrentAccommodation("AIRBNB", LocalDate.now().plusDays(10)),
    nextAccommodation = NextAccommodation("PRISON", LocalDate.now().plusDays(100)),
  )
}
