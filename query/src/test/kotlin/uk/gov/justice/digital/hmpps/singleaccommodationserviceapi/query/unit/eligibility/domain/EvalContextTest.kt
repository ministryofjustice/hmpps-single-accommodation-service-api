package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvalContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildDomainData
import java.util.UUID

class EvalContextTest {
  @Test
  fun `EvalContext can be created with DomainData and ServiceResult`() {
    val domainData = buildDomainData()
    val serviceResult = ServiceResult(ServiceStatus.CONFIRMED)

    val context = EvalContext(data = domainData, current = serviceResult)

    assertThat(context.data).isEqualTo(domainData)
    assertThat(context.current).isEqualTo(serviceResult)
  }

  @Test
  fun `EvalContext copy creates new instance with updated fields`() {
    val originalData = buildDomainData()
    val originalResult = ServiceResult(ServiceStatus.CONFIRMED)
    val context = EvalContext(data = originalData, current = originalResult)

    val updatedResult = ServiceResult(ServiceStatus.NOT_ELIGIBLE)
    val updatedContext = context.copy(current = updatedResult)

    assertThat(updatedContext.data).isEqualTo(originalData)
    assertThat(updatedContext.current).isEqualTo(updatedResult)
    assertThat(context.current).isEqualTo(originalResult) // Original unchanged
  }

  @Test
  fun `EvalContext copy can update data field`() {
    val originalData = buildDomainData()
    val originalResult = ServiceResult(ServiceStatus.CONFIRMED)
    val context = EvalContext(data = originalData, current = originalResult)

    val updatedData = buildDomainData(crn = "DIFFERENT_CRN")
    val updatedContext = context.copy(data = updatedData)

    assertThat(updatedContext.data).isEqualTo(updatedData)
    assertThat(updatedContext.current).isEqualTo(originalResult)
    assertThat(context.data).isEqualTo(originalData) // Original unchanged
  }

  @Test
  fun `EvalContext instances with same values are equal`() {
    val domainData = buildDomainData()
    val serviceResult = ServiceResult(
      serviceStatus = ServiceStatus.CONFIRMED,
      suitableApplicationId = UUID.randomUUID(),
    )

    val context1 = EvalContext(data = domainData, current = serviceResult)
    val context2 = EvalContext(data = domainData, current = serviceResult)

    assertThat(context1).isEqualTo(context2)
    assertThat(context1.hashCode()).isEqualTo(context2.hashCode())
  }

  @Test
  fun `EvalContext instances with different values are not equal`() {
    val domainData1 = buildDomainData()
    val domainData2 = buildDomainData(crn = "DIFFERENT_CRN")
    val serviceResult = ServiceResult(ServiceStatus.CONFIRMED)

    val context1 = EvalContext(data = domainData1, current = serviceResult)
    val context2 = EvalContext(data = domainData2, current = serviceResult)

    assertThat(context1).isNotEqualTo(context2)
  }

  @Test
  fun `EvalContext instances with different ServiceResult are not equal`() {
    val domainData = buildDomainData()
    val result1 = ServiceResult(ServiceStatus.CONFIRMED)
    val result2 = ServiceResult(ServiceStatus.NOT_ELIGIBLE)

    val context1 = EvalContext(data = domainData, current = result1)
    val context2 = EvalContext(data = domainData, current = result2)

    assertThat(context1).isNotEqualTo(context2)
  }
}