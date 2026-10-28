package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.AggregatorCallOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.ErrorDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.FailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.GET_USER_ACCESS
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.ApiCallKeys.POST_CASE_SUMMARIES
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.CaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.ProbationAccessControlService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationaccesscontrol.UserCaseAccess
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.LimitedAccessService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildUpstreamFailure

@ExtendWith(MockKExtension::class)
class LimitedAccessServiceTest {
  @MockK
  lateinit var probationAccessControlService: ProbationAccessControlService

  @InjectMockKs
  lateinit var limitedAccessService: LimitedAccessService

  @Nested
  inner class UserAccessCalls {

    @Test
    fun `should batch crns into calls of 500 max`() {
      val crns = (1..1001).map { "CRN$it" }
      every { probationAccessControlService.getUserAccess(any(), any()) } returns UserCaseAccess(emptyList())

      val calls = limitedAccessService.userAccessCalls("user1", crns)
      calls.values.forEach { it() }

      assertThat(calls.keys).containsExactly(GET_USER_ACCESS + 0, GET_USER_ACCESS + 1, GET_USER_ACCESS + 2)
      verify { probationAccessControlService.getUserAccess("user1", crns.subList(0, 500)) }
      verify { probationAccessControlService.getUserAccess("user1", crns.subList(500, 1000)) }
      verify { probationAccessControlService.getUserAccess("user1", crns.subList(1000, 1001)) }
    }

    @Test
    fun `should return no calls when there are no crns`() {
      assertThat(limitedAccessService.userAccessCalls("user1", emptyList())).isEmpty()
    }
  }

  @Nested
  inner class GetCaseAccess {
    private fun userCaseAccess(crn: String) = UserCaseAccess(listOf(CaseAccess(crn = crn, userExcluded = false, userRestricted = false)))

    @Test
    fun `should combine the access from every successful batch and ignore other calls`() {
      val results = mapOf<String, Any>(
        GET_USER_ACCESS + 0 to AggregatorCallOutcome.Success(userCaseAccess("A111111")),
        GET_USER_ACCESS + 1 to AggregatorCallOutcome.Success(userCaseAccess("B222222")),
        GET_USER_ACCESS + 2 to AggregatorCallOutcome.Failure(FailureType.TIMEOUT, ErrorDetail(message = "timeout")),
        POST_CASE_SUMMARIES to AggregatorCallOutcome.Success(userCaseAccess("C333333")),
      )

      assertThat(limitedAccessService.getCaseAccess(results).map { it.crn }).containsExactlyInAnyOrder("A111111", "B222222")
    }
  }

  @Nested
  inner class GetLimitedAccessCrns {
    @Test
    fun `should return crns the user is excluded or restricted from`() {
      val caseAccess = listOf(
        CaseAccess(crn = "A111111", userExcluded = false, userRestricted = false),
        CaseAccess(crn = "B222222", userExcluded = true, userRestricted = false),
        CaseAccess(crn = "C333333", userExcluded = false, userRestricted = true),
        CaseAccess(crn = "D444444", userExcluded = true, userRestricted = true),
      )

      val result = limitedAccessService.getLimitedAccessCrns(listOf("A111111", "B222222", "C333333", "D444444"), caseAccess)
      assertThat(result).containsExactlyInAnyOrder("B222222", "C333333", "D444444")
    }

    @Test
    fun `should fail closed for crns missing from the access response`() {
      val caseAccess = listOf(CaseAccess(crn = "A111111", userExcluded = false, userRestricted = false))

      val result = limitedAccessService.getLimitedAccessCrns(listOf("A111111", "B222222"), caseAccess)
      assertThat(result).containsExactly("B222222")
    }
  }

  @Nested
  inner class ThrowIfUserAccessFailed {
    @Test
    fun `should throw an upstream failure error when any user access batch calls fail`() {
      val failures = listOf(buildUpstreamFailure(callKey = POST_CASE_SUMMARIES), buildUpstreamFailure(callKey = GET_USER_ACCESS + 1))

      assertThatThrownBy { limitedAccessService.throwIfUserAccessFailed(failures) }
        .isInstanceOf(UpstreamFailureException::class.java)
    }

    @Test
    fun `should not throw when other upstream calls fail`() {
      val failures = listOf(buildUpstreamFailure(callKey = POST_CASE_SUMMARIES))

      limitedAccessService.throwIfUserAccessFailed(failures)
    }
  }
}
