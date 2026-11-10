package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.engine

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResultNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.engine.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainData
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
    FailureReason.CRS_NOT_SUBMITTED_MALE,
    FailureReason.DTR_REFERRAL_EXPIRED,
  )

  @Nested
  inner class UpdateTests {
    @Test
    fun `update passes failure reasons to toServiceResult context`() {
      var reasonsSeenByToServiceResult: List<FailureReason> = emptyList()
      val updater = object : ContextUpdater<DomainData>() {
        override fun toServiceResult(context: EvaluationContext<DomainData>): ServiceResultNew {
          reasonsSeenByToServiceResult = context.currentResult!!.failureReasons
          return buildServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS)
        }
      }

      updater.update(context, failureReasons)

      Assertions.assertThat(reasonsSeenByToServiceResult).containsExactlyElementsOf(failureReasons)
    }

    @Test
    fun `update does not propagate failure reasons by default`() {
      val updater = object : ContextUpdater<DomainData>() {
        override fun toServiceResult(context: EvaluationContext<DomainData>): ServiceResultNew = buildServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS)
      }

      val result = updater.update(context, failureReasons)

      Assertions.assertThat(result.currentResult!!.failureReasons).isEmpty()
    }

    @Test
    fun `update propagates failure reasons when configured`() {
      val updater = object : ContextUpdater<DomainData>() {
        override val propagatesFailureReasons: Boolean = true

        override fun toServiceResult(context: EvaluationContext<DomainData>): ServiceResultNew = buildServiceResultNew(serviceStatus = ServiceStatusNew.CAS3_CANNOT_START_YET_SUBMIT_DTR_CRS)
      }

      val result = updater.update(context, failureReasons)

      Assertions.assertThat(result.currentResult!!.failureReasons).containsExactlyElementsOf(failureReasons)
    }
  }

  @Nested
  inner class CompanionObjectTests {
    @Test
    fun `constant updater replaces service result and does not propagate failure reasons`() {
      val constantResult = buildServiceResultNew(serviceStatus = ServiceStatusNew.DTR_ACCEPTED)

      val result = ContextUpdater.constant(constantResult).update(context, failureReasons)

      Assertions.assertThat(result.currentResult).isEqualTo(constantResult)
    }

    @Test
    fun `identity updater keeps current result fields and propagates failure reasons`() {
      val result = ContextUpdater.identity().update(context, failureReasons)

      Assertions.assertThat(result.currentResult!!.serviceStatus).isEqualTo(context.currentResult!!.serviceStatus)
      Assertions.assertThat(result.currentResult.failureReasons).containsExactlyElementsOf(failureReasons)
    }

    @Test
    fun `identity updater throws a clear error when currentResult has not been set`() {
      val contextWithoutResult = EvaluationContext(
        data = buildDomainData(),
        currentResult = null,
      )

      Assertions.assertThatIllegalArgumentException()
        .isThrownBy { ContextUpdater.identity().update(contextWithoutResult, failureReasons) }
        .withMessageContaining("identity")
    }
  }
}
