package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.ApprovedPremisesClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.ProbationIntegrationDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.ProbationIntegrationOasysClient
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.StructuredTaskScope


@Service
class DummyService(
  val probationIntegrationDeliusClient: ProbationIntegrationDeliusClient,
  val probationIntegrationOasysClient: ProbationIntegrationOasysClient,
  val approvedPremisesClient: ApprovedPremisesClient,
) {
  fun getInfoSequential(): Map<String, String> {
    val oasysInfo = probationIntegrationOasysClient.getInfo()
    val deliusInfo = probationIntegrationDeliusClient.getInfo()
    val approvedPremisesInfo = approvedPremisesClient.getInfo()

    return mapOf("oasysInfo" to oasysInfo, "deliusInfo" to deliusInfo, "approvedPremisesInfo" to approvedPremisesInfo)

  }

  suspend fun getInfoCoroutine(): Map<String, String> = coroutineScope {
    val oasys  = async { probationIntegrationOasysClient.getInfo() }
    val delius = async { probationIntegrationDeliusClient.getInfo() }
    val ap     = async { approvedPremisesClient.getInfo() }

    mapOf(
      "oasysInfo" to oasys.await(),
      "deliusInfo" to delius.await(),
      "approvedPremisesInfo" to ap.await(),
    )
  }

  fun getInfoVirtualThreads(): Map<String, String> {
    StructuredTaskScope.ShutdownOnFailure().use { scope ->
      val oasys  = scope.fork { probationIntegrationOasysClient.getInfo() }
      val delius = scope.fork { probationIntegrationDeliusClient.getInfo() }
      val ap     = scope.fork { approvedPremisesClient.getInfo() }

      scope.join()        // wait for all
//      scope.throwIfFailed() // throw first failure

      return mapOf(
        "oasysInfo" to oasys.get(),
        "deliusInfo" to delius.get(),
        "approvedPremisesInfo" to ap.get(),
      )
    }
  }
}
