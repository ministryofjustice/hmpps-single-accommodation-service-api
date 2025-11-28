package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.AssignedTo
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.CurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.NextAccommodation
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

@Service
class CaseService(
  val aggregatorService: AggregatorService,
  val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
  val tierCachingService: TierCachingService,
) {
  fun getCases(crns: List<String>): List<Case> {
    val res = aggregatorService.orchestrateAsyncCalls(
      crns,
      mapOf(
        "delius" to { crn -> probationIntegrationDeliusCachingService.getCaseSummary(crn) },
        "corePersonRecord" to { crn -> corePersonRecordCachingService.getCorePersonRecord(crn) },
        "roshDetails" to { crn -> probationIntegrationOasysCachingService.getRoshSummary(crn) },
        "tier" to { crn -> tierCachingService.getTier(crn) },
      ),
    )

    return res.entries.map { it ->
      val cpr = it.value["corePersonRecord"] as CorePersonRecord
      val roshDetails = it.value["roshDetails"] as RoshDetails
      val tier = it.value["tier"] as Tier
      val delius = (it.value["delius"] as CaseSummaries).cases[0]
      buildCase(it.key, cpr, roshDetails, tier, delius)
    }
  }

  fun getCase(crn: String): Case {
    val res = aggregatorService.orchestrateAsyncCalls(
      mapOf(
        "delius" to { probationIntegrationDeliusCachingService.getCaseSummary(crn) },
        "corePersonRecord" to { corePersonRecordCachingService.getCorePersonRecord(crn) },
        "roshDetails" to { probationIntegrationOasysCachingService.getRoshSummary(crn) },
        "tier" to { tierCachingService.getTier(crn) },
      ),
    )

    val cpr = res["corePersonRecord"] as CorePersonRecord
    val roshDetails = res["roshDetails"] as RoshDetails
    val tier = res["tier"] as Tier
    val delius = (res["delius"] as CaseSummaries).cases[0]
    return buildCase(crn, cpr, roshDetails, tier, delius)
  }

  fun buildCase(crn: String, cpr: CorePersonRecord, roshDetails: RoshDetails, tier: Tier, delius: CaseSummary): Case = Case(
    name = formatName(cpr),
    dateOfBirth = delius.dateOfBirth,
    crn = crn,
    prisonNumber = cpr.identifiers?.prisonNumbers[0],
    tier = tier.tierScore,
    riskLevel = roshDetails.rosh.determineOverallRiskLevel(),
    pncReference = delius.pnc,
    assignedTo = AssignedTo(1L, delius.manager.team.name),
    currentAccommodation = CurrentAccommodation("AIRBNB", LocalDate.now().plusDays(10)),
    nextAccommodation = NextAccommodation("PRISON", LocalDate.now().plusDays(100)),
  )

  fun formatName(cpr: CorePersonRecord) = listOfNotNull(
    cpr.firstName,
    cpr.middleNames?.takeIf { it.isNotBlank() },
    cpr.lastName,
  )
    .joinToString(" ")
}
