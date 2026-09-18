package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew

class ContextUpdaterTest {
  private val context = EvaluationContext(
    data = buildDomainData(),
    currentResult = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS1_NOT_STARTED,
    ),
  )

  private val failureReasons = listOf(
    FailureReason.CRS_NOT_SUBMITTED,
    FailureReason.DTR_REFERRAL_EXPIRED,
  )

  @Nested
  inner class UpdateTests {
    @Test
    fun `update passes failure reasons to toServiceResult context`() {
      var reasonsSeenByToServiceResult: List<FailureReason> = emptyList()
      val updater = object : ContextUpdater() {
        override fun toServiceResult(context: EvaluationContext): ServiceResultNew {
          reasonsSeenByToServiceResult = context.currentResult.failureReasons
          return buildServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET)
        }
      }

      updater.update(context, failureReasons)

      assertThat(reasonsSeenByToServiceResult).containsExactlyElementsOf(failureReasons)
    }

    @Test
    fun `update does not propagate failure reasons by default`() {
      val updater = object : ContextUpdater() {
        override fun toServiceResult(context: EvaluationContext): ServiceResultNew = buildServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET)
      }

      val result = updater.update(context, failureReasons)

      assertThat(result.currentResult.failureReasons).isEmpty()
    }

    @Test
    fun `update propagates failure reasons when configured`() {
      val updater = object : ContextUpdater() {
        override val propagatesFailureReasons: Boolean = true

        override fun toServiceResult(context: EvaluationContext): ServiceResultNew = buildServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET)
      }

      val result = updater.update(context, failureReasons)

      assertThat(result.currentResult.failureReasons).containsExactlyElementsOf(failureReasons)
    }
  }

  @Nested
  inner class CompanionObjectTests {
    @Test
    fun `constant updater replaces service result and does not propagate failure reasons`() {
      val constantResult = buildServiceResultNew(serviceStatus = ServiceStatusNew.DTR_ACCEPTED)

      val result = ContextUpdater.constant(constantResult).update(context, failureReasons)

      assertThat(result.currentResult).isEqualTo(constantResult)
    }

    @Test
    fun `identity updater keeps current result fields and propagates failure reasons`() {
      val result = ContextUpdater.identity().update(context, failureReasons)

      assertThat(result.currentResult.serviceStatus).isEqualTo(context.currentResult.serviceStatus)
      assertThat(result.currentResult.failureReasons).containsExactlyElementsOf(failureReasons)
    }
  }
}
