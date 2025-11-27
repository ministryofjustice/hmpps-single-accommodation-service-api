package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.ProbationIntegrationDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.ProbationIntegrationOasysClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshSummary
import java.time.LocalDate

@Service
class CaseService(
  val aggregatorService: AggregatorService,
  val probationIntegrationDeliusClient: ProbationIntegrationDeliusClient,
  val corePersonRecordClient: CorePersonRecordClient,
  val probationIntegrationOasysClient: ProbationIntegrationOasysClient,
) {
  fun getCases(crns: List<String>): List<Case> {
    val res = aggregatorService.orchestrateAsyncCalls(
      crns,
      mapOf(
        "delius" to { crn -> probationIntegrationDeliusClient.postCaseSummaries(listOf(crn)) },
        "corePersonRecord" to { crn -> corePersonRecordClient.getCorePersonRecord(crn) },
        "oasys" to { crn -> probationIntegrationOasysClient.getRoshSummary(crn) },
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
        "delius" to { probationIntegrationDeliusClient.postCaseSummaries(listOf(crn)) },
        "corePersonRecord" to { corePersonRecordClient.getCorePersonRecord(crn) },
        "oasys" to { probationIntegrationOasysClient.getRoshSummary(crn) },
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
