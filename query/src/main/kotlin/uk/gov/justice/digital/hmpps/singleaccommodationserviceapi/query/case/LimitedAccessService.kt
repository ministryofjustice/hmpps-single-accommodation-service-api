package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailureTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.getResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_USER_ACCESS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.ProbationAccessControlService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.UserCaseAccess

@Service
class LimitedAccessService(
  private val probationAccessControlService: ProbationAccessControlService,
) {
  private val userAccessBatchSize = 500

  // returns user access calls so orchestration services can run them in parallel
  fun userAccessCalls(username: String, crns: List<String>): Map<String, () -> UserCaseAccess> = crns.chunked(userAccessBatchSize)
    .withIndex()
    .associate { (batchIndex, batch) ->
      getUserAccessCallKey(batchIndex) to { probationAccessControlService.getUserAccess(username, batch) }
    }

  fun getCaseAccess(results: Map<String, Any>): List<CaseAccess> = results.keys
    .filter { it.startsWith(GET_USER_ACCESS) }
    .flatMap { results.getResult<UserCaseAccess>(it)?.access ?: emptyList() }

  fun getLimitedAccessCrns(crns: List<String>, caseAccess: List<CaseAccess>): Set<String> {
    val accessByCrn = caseAccess.associateBy { it.crn }
    return crns.filter { crn -> accessByCrn[crn]?.let { it.userExcluded || it.userRestricted } ?: true }.toSet()
  }

  fun throwIfUserAccessFailed(upstreamFailures: List<UpstreamFailure>) {
    upstreamFailures.firstOrNull { it.callKey.startsWith(GET_USER_ACCESS) }?.let {
      throw UpstreamFailureException(UpstreamFailureTransformer.toUpstreamFailureDto(it))
    }
  }

  private fun getUserAccessCallKey(batchIndex: Int) = GET_USER_ACCESS + batchIndex
}
