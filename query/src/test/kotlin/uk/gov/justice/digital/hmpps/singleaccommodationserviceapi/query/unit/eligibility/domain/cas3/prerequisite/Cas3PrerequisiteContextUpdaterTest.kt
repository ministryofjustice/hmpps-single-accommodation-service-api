package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.prerequisite

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FailureReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas3.prerequisite.Cas3PrerequisiteContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import java.time.LocalDate
import java.util.UUID

class Cas3PrerequisiteContextUpdaterTest {
  private val updater = Cas3PrerequisiteContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result using toServiceResult`() {
      val currentAccommodationEndDate = LocalDate.parse("2026-12-31")
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        currentAccommodation = buildAccommodationSummaryDto(endDate = currentAccommodationEndDate),
        cas3Application = buildCas3Application(
          id = applicationId,
          applicationStatus = Cas3ApplicationStatus.REJECTED,
        ),
      )
      val failureReasons = listOf(FailureReason.DTR_REFERRAL_EXPIRED, FailureReason.CRS_NOT_SUBMITTED, FailureReason.CRS_EXPIRED)
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val result = updater.update(context, failureReasons)

      assertThat(result.currentResult.action).isEqualTo(CaseAction(type = CaseActionType.SUBMIT_CRS_REFERRAL))
      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.CANNOT_START_YET)
      assertThat(result.currentResult.failureReasons).isEqualTo(failureReasons)
      assertThat(updater.propagatesFailureReasons).isTrue
    }

    @Test
    fun `Sets CaseActionType depending on SexCode`() {
      val data = buildDomainData(
        cas3Application = buildCas3Application(),
      )
      val updatedData = data.copy(sex = SexCode.M)
      var context = EvaluationContext(
        data = updatedData,
        currentResult = buildServiceResult(),
      )
      var result = updater.update(context)
      assertThat(result.currentResult.action).isEqualTo(CaseAction(CaseActionType.SUBMIT_CRS_ACCOMMODATION_REFERRAL))
      val dataNonMale = data.copy(sex = SexCode.F)
      context = EvaluationContext(
        data = dataNonMale,
        currentResult = buildServiceResult(),
      )
      result = updater.update(context)
      assertThat(result.currentResult.action).isEqualTo(CaseAction(CaseActionType.SUBMIT_CRS_REFERRAL))
    }
  }
}
