package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas3.prerequisite

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.action).isEqualTo(CaseAction(type = CaseActionType.SUBMIT_DTR_BEFORE_CAS3))
      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.CANNOT_START_YET)
    }

    @Nested
    inner class SetsCaseActionTypeDependingOnFailureReasonAndSexCode {
      private fun context(sex: SexCode): EvaluationContext {
        val data = buildDomainData(
          cas3Application = buildCas3Application(),
        ).copy(sex = sex)

        return EvaluationContext(
          data = data,
          currentResult = buildServiceResult(),
        )
      }

      @ParameterizedTest
      @EnumSource(value = SexCode::class)
      fun `When only DTR is outstanding`(sex: SexCode) {
        val result = updater.update(context(sex), listOf(FailureReason.DTR_REFERRAL_EXPIRED))

        assertThat(result.currentResult.action).isEqualTo(CaseAction(CaseActionType.SUBMIT_DTR_BEFORE_CAS3))
      }

      @Test
      fun `When DTR and CRS are outstanding - male`() {
        val result = updater.update(
          context(SexCode.M),
          listOf(FailureReason.DTR_REFERRAL_EXPIRED, FailureReason.CRS_NOT_SUBMITTED),
        )

        assertThat(result.currentResult.action).isEqualTo(CaseAction(CaseActionType.SUBMIT_DTR_AND_CRS_ACCOMMODATION_BEFORE_CAS3))
      }

      @ParameterizedTest
      @EnumSource(value = SexCode::class, names = ["M"], mode = EnumSource.Mode.EXCLUDE)
      fun `When DTR and CRS are outstanding - non-male`(sex: SexCode) {
        val result = updater.update(
          context(sex),
          listOf(FailureReason.DTR_REFERRAL_EXPIRED, FailureReason.CRS_NOT_SUBMITTED),
        )

        assertThat(result.currentResult.action).isEqualTo(CaseAction(CaseActionType.SUBMIT_DTR_AND_CRS_BEFORE_CAS3))
      }

      @Test
      fun `When only CRS is outstanding - male`() {
        val result = updater.update(context(SexCode.M), listOf(FailureReason.CRS_NOT_SUBMITTED))

        assertThat(result.currentResult.action).isEqualTo(CaseAction(CaseActionType.SUBMIT_CRS_ACCOMMODATION_BEFORE_CAS3))
      }

      @ParameterizedTest
      @EnumSource(value = SexCode::class, names = ["M"], mode = EnumSource.Mode.EXCLUDE)
      fun `When only CRS is outstanding - non-male`(sex: SexCode) {
        val result = updater.update(context(sex), listOf(FailureReason.CRS_NOT_SUBMITTED))

        assertThat(result.currentResult.action).isEqualTo(CaseAction(CaseActionType.SUBMIT_CRS_BEFORE_CAS3))
      }
    }
  }
}
