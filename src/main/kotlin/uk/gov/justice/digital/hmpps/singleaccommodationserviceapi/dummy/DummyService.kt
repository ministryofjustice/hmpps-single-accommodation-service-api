package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.dummy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecordClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.ProbationIntegrationDeliusClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.ProbationIntegrationOasysClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.RoshDetails

@Service
class DummyService(
  val probationIntegrationDeliusClient: ProbationIntegrationDeliusClient,
  val probationIntegrationOasysClient: ProbationIntegrationOasysClient,
  val corePersonRecordClient: CorePersonRecordClient,
) {
  private val serviceCallMap: Map<String, (String) -> Any> = mapOf(
    "oasysInfo" to { crn -> getOasys(crn) },
    "deliusInfo" to { crn -> getDelius(crn) },
    "corePersonRecord" to { crn -> getCorePersonRecord(crn) },
  )

  fun getDelius(crn: String): List<CaseSummary> = probationIntegrationDeliusClient.postCaseSummaries(listOf(crn)).cases

  fun getOasys(crn: String): RoshDetails = probationIntegrationOasysClient.getRoshDetails(crn)

  fun getCorePersonRecord(crn: String): CorePersonRecord {
//    Thread.sleep(500)
    return corePersonRecordClient.getCorePersonRecord(crn)
  }

  fun getInfoSequential(crns: List<String>): Map<String, Any> = crns.associateWith { crn ->
    val oasysInfo = getOasys(crn)
    val deliusInfo = getDelius(crn)
    val corePersonRecord = getCorePersonRecord(crn)
    mapOf("oasysInfo" to oasysInfo, "deliusInfo" to deliusInfo, "corePersonRecord" to corePersonRecord)
  }

  suspend fun getResult(crn: String): Map<String, Any> = supervisorScope {
    val results = serviceCallMap.mapValues { (_, supplier) ->
      async(Dispatchers.IO) {
        runCatching { supplier(crn) }
          .getOrElse { "Failed with message: ${it.message}" }
      }
    }

    results.mapValues { (_, deferred) -> deferred.await() }
  }

  suspend fun getResults(crns: List<String>): Map<String, Map<String, Any>> = supervisorScope {
    // one coroutine per CRN
    val deferredResults = crns.associateWith { crn ->
      async(Dispatchers.IO) {
        supervisorScope {
          // inner async calls for each service
          val oasys = async {
            runCatching { getOasys(crn) }
              .getOrElse { "Failed: ${it.message}" }
          }

          val delius = async {
            runCatching { getDelius(crn) }
              .getOrElse { "Failed: ${it.message}" }
          }

          val core = async {
            runCatching { getCorePersonRecord(crn) }
              .getOrElse { "Failed: ${it.message}" }
          }

          // wait for all 3
          mapOf(
            "oasysInfo" to oasys.await(),
            "deliusInfo" to delius.await(),
            "corePersonRecord" to core.await(),
          )
        }
      }
    }

    // await all CRN results
    deferredResults.mapValues { (_, deferred) -> deferred.await() }
  }
}
