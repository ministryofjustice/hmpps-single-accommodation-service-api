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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshSummary
import java.time.LocalDate

@Service
class CaseService(
  val aggregatorService: AggregatorService,
  val probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService,
  val corePersonRecordCachingService: CorePersonRecordCachingService,
  val probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService,
) {
  fun getCases(crns: List<String>): List<Case> {
    val res = aggregatorService.orchestrateAsyncCalls(
      crns,
      mapOf(
        "delius" to { crn -> probationIntegrationDeliusCachingService.getCaseSummary(crn) },
        "corePersonRecord" to { crn -> corePersonRecordCachingService.getCorePersonRecord(crn) },
        "oasys" to { crn -> probationIntegrationOasysCachingService.getRoshSummary(crn) },
      ),
    )

    return res.entries.map { it ->
      val cpr = it.value["corePersonRecord"] as CorePersonRecord
      val oasys = it.value["oasys"] as RoshSummary
      val delius = (it.value["delius"] as CaseSummaries).cases[0]
      buildCase(it.key, cpr, oasys, delius)
    }
  }

  fun getCase(crn: String): Case {
    val res = aggregatorService.orchestrateAsyncCalls(
      mapOf(
        "delius" to { probationIntegrationDeliusCachingService.getCaseSummary(crn) },
        "corePersonRecord" to { corePersonRecordCachingService.getCorePersonRecord(crn) },
        "oasys" to { probationIntegrationOasysCachingService.getRoshSummary(crn) },
      ),
    )

    val cpr = res["corePersonRecord"] as CorePersonRecord
    val oasys = res["oasys"] as RoshSummary
    val delius = (res["delius"] as CaseSummaries).cases[0]
    return buildCase(crn, cpr, oasys, delius)
  }

  fun buildCase(crn: String, cpr: CorePersonRecord, oasys: RoshSummary, delius: CaseSummary): Case = Case(
    name = "${delius.name.forename} ${cpr.lastName}",
    dateOfBirth = delius.dateOfBirth,
    crn = crn,
    prisonNumber = cpr.identifiers?.prisonNumbers[0],
    tier = oasys.assessmentId.toString(),
    rosh = "TODO()",
    pncReference = delius.pnc,
    assignedTo = AssignedTo(1L, delius.manager.team.name),
    currentAccommodation = CurrentAccommodation("AIRBNB", LocalDate.now().plusDays(10)),
    nextAccommodation = NextAccommodation("PRISON", LocalDate.now().plusDays(100)),
  )
}
