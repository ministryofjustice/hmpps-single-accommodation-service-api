package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew

class EvalContextTest {
  @Test
  fun `EvalContext can be created with DomainData and ServiceResult`() {
    val domainData = buildDomainData()
    val serviceResult = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED)

    val context = EvaluationContext(data = domainData, currentResult = serviceResult)

    assertThat(context.data).isEqualTo(domainData)
    assertThat(context.currentResult).isEqualTo(serviceResult)
  }

  @Test
  fun `EvalContext update creates new instance with updated fields`() {
    val originalData = buildDomainData()
    val originalResult = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED)
    val context = EvaluationContext(data = originalData, currentResult = originalResult)

    val updatedResult = buildServiceResultNew()
    val updatedContext = context.copy(currentResult = updatedResult)

    assertThat(updatedContext.data).isEqualTo(originalData)
    assertThat(updatedContext.currentResult).isEqualTo(updatedResult)
    assertThat(context.currentResult).isEqualTo(originalResult) // Original unchanged
  }

  @Test
  fun `EvalContext copy can update data field`() {
    val originalData = buildDomainData()
    val originalResult = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED)
    val context = EvaluationContext(data = originalData, currentResult = originalResult)

    val updatedData = buildDomainData(crn = "DIFFERENT_CRN")
    val updatedContext = context.copy(data = updatedData)

    assertThat(updatedContext.data).isEqualTo(updatedData)
    assertThat(updatedContext.currentResult).isEqualTo(originalResult)
    assertThat(context.data).isEqualTo(originalData) // Original unchanged
  }

  @Test
  fun `EvalContext instances with same values are equal`() {
    val domainData = buildDomainData()
    val serviceResult = buildServiceResultNew(
      serviceStatus = ServiceStatusNew.CAS1_PLACEMENT_BOOKED,
    )

    val context1 = EvaluationContext(data = domainData, currentResult = serviceResult)
    val context2 = EvaluationContext(data = domainData, currentResult = serviceResult)

    assertThat(context1).isEqualTo(context2)
    assertThat(context1.hashCode()).isEqualTo(context2.hashCode())
  }

  @Test
  fun `EvalContext instances with different values are not equal`() {
    val domainData1 = buildDomainData()
    val domainData2 = buildDomainData(crn = "DIFFERENT_CRN")
    val serviceResult = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED)

    val context1 = EvaluationContext(data = domainData1, currentResult = serviceResult)
    val context2 = EvaluationContext(data = domainData2, currentResult = serviceResult)

    assertThat(context1).isNotEqualTo(context2)
  }

  @Test
  fun `EvalContext instances with different ServiceResult are not equal`() {
    val domainData = buildDomainData()
    val result1 = buildServiceResultNew(ServiceStatusNew.CAS1_PLACEMENT_BOOKED)
    val result2 = buildServiceResultNew()

    val context1 = EvaluationContext(data = domainData, currentResult = result1)
    val context2 = EvaluationContext(data = domainData, currentResult = result2)

    assertThat(context1).isNotEqualTo(context2)
  }
}
