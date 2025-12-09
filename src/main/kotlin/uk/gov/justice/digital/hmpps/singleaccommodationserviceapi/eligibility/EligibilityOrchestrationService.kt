package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.orchestration

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.ApiCallKeys.GET_CAS_1_APPLICATION
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ApprovedPremisesCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import java.time.OffsetDateTime

@Service
class EligibilityOrchestrationService(
  val aggregatorService: AggregatorService,
  val approvedPremisesCachingService: ApprovedPremisesCachingService,
) {

  fun getData(crn: String): DomainData {
    val calls = mapOf(
      GET_CAS_1_APPLICATION to { approvedPremisesCachingService.getSuitableCas1Application(crn) },
    )
    val results = aggregatorService.orchestrateAsyncCalls(
      standardCallsNoIteration = calls,
    )

    val cas1Application = results.standardCallsNoIterationResults!![GET_CAS_1_APPLICATION] as? Cas1Application

    return DomainData(
      crn = crn,
      tier = "A1",
      sex = Sex(
        code = "M",
        description = "Male",
      ),
      releaseDate = OffsetDateTime.now().plusMonths(6),
      cas1Application = cas1Application,
    )
  }
}
