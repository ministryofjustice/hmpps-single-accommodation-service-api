package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CasReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralService

@ExtendWith(MockKExtension::class)
class AccommodationReferralDtoTest {
  @MockK
  lateinit var accommodationReferralOrchestrationService: AccommodationReferralOrchestrationService
  @InjectMockKs
  lateinit var accommodationReferralService: AccommodationReferralService

  @Test
  fun `all Cas1AssessmentStatus values map correctly`() {
    val expectedMapping = mapOf(
      Cas1AssessmentStatus.COMPLETED to CasReferralStatus.ACCEPTED,
      Cas1AssessmentStatus.REALLOCATED to CasReferralStatus.REJECTED,
      Cas1AssessmentStatus.AWAITING_RESPONSE to CasReferralStatus.PENDING,
      Cas1AssessmentStatus.IN_PROGRESS to CasReferralStatus.PENDING,
      Cas1AssessmentStatus.NOT_STARTED to CasReferralStatus.PENDING,
    )

    Cas1AssessmentStatus.entries.forEach { status ->
      with(accommodationReferralService) {
        assertThat(CasReferralStatus.from(status)).isEqualTo(expectedMapping[status])
      }
    }
  }

  @Test
  fun `all Cas2Status values map to correct CasReferralStatus`() {
    val expectations = mapOf(
      Cas2Status.PLACE_OFFERED to CasReferralStatus.ACCEPTED,
      Cas2Status.OFFER_ACCEPTED to CasReferralStatus.ACCEPTED,

      Cas2Status.OFFER_DECLINED_OR_WITHDRAWN to CasReferralStatus.REJECTED,
      Cas2Status.REFERRAL_CANCELLED to CasReferralStatus.REJECTED,
      Cas2Status.REFERRAL_WITHDRAWN to CasReferralStatus.REJECTED,

      Cas2Status.MORE_INFORMATION_REQUESTED to CasReferralStatus.PENDING,
      Cas2Status.AWAITING_ARRIVAL to CasReferralStatus.PENDING,
      Cas2Status.ON_WAITING_LIST to CasReferralStatus.PENDING,
      Cas2Status.AWAITING_DECISION to CasReferralStatus.PENDING,
    )

    Cas2Status.entries.forEach { status ->
      with(accommodationReferralService) {
        assertThat(CasReferralStatus.from(status)).isEqualTo(expectations[status])
      }
    }
  }

  @Test
  fun `TemporaryAccommodationAssessmentStatus maps to correct CasReferralStatus`() {
    val expectations = mapOf(
      TemporaryAccommodationAssessmentStatus.READY_TO_PLACE to CasReferralStatus.ACCEPTED,

      TemporaryAccommodationAssessmentStatus.CLOSED to CasReferralStatus.REJECTED,
      TemporaryAccommodationAssessmentStatus.REJECTED to CasReferralStatus.REJECTED,

      TemporaryAccommodationAssessmentStatus.UNALLOCATED to CasReferralStatus.PENDING,
      TemporaryAccommodationAssessmentStatus.IN_REVIEW to CasReferralStatus.PENDING,
    )

    TemporaryAccommodationAssessmentStatus.entries.forEach { status ->
      with(accommodationReferralService) {
        assertThat(CasReferralStatus.from(status)).isEqualTo(expectations[status])
      }
    }
  }
}
