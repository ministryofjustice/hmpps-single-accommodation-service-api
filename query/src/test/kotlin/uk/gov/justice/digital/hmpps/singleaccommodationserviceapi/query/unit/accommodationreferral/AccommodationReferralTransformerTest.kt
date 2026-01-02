package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CasReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CasService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.toCasReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.transformReferrals
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory.buildAccommodationReferralOrchestrationDto
import java.util.stream.Stream

class AccommodationReferralTransformerTest {
  @Test
  fun `should transform orchestration dto to list of accommodation referral dtos`() {
    val orchestrationDto = buildAccommodationReferralOrchestrationDto()

    val result = transformReferrals(orchestrationDto)

    assertThat(result).hasSize(4)
    assertThat(result.map { it.type }).containsExactlyInAnyOrder(
      CasService.CAS1,
      CasService.CAS2,
      CasService.CAS2v2,
      CasService.CAS3,
    )
  }

  @ParameterizedTest
  @MethodSource("cas1StatusMappings")
  fun `should transform Cas1AssessmentStatus to CasReferralStatus`(
    input: Cas1AssessmentStatus,
    expected: CasReferralStatus,
  ) {
    assertThat(toCasReferralStatus(input)).isEqualTo(expected)
  }

  @ParameterizedTest
  @MethodSource("cas2StatusMappings")
  fun `should transform Cas2Status to CasReferralStatus`(
    input: Cas2Status,
    expected: CasReferralStatus,
  ) {
    assertThat(toCasReferralStatus(input)).isEqualTo(expected)
  }

  @ParameterizedTest
  @MethodSource("cas3StatusMappings")
  fun `should transform TemporaryAccommodationAssessmentStatus to CasReferralStatus`(
    input: TemporaryAccommodationAssessmentStatus,
    expected: CasReferralStatus,
  ) {
    assertThat(toCasReferralStatus(input)).isEqualTo(expected)
  }

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
      assertThat(toCasReferralStatus(status)).isEqualTo(expectedMapping[status])
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
      assertThat(toCasReferralStatus(status)).isEqualTo(expectations[status])
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
      assertThat(toCasReferralStatus(status)).isEqualTo(expectations[status])
    }
  }

  private companion object {
    @JvmStatic
    fun cas1StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(Cas1AssessmentStatus.COMPLETED, CasReferralStatus.ACCEPTED),
      Arguments.of(Cas1AssessmentStatus.REALLOCATED, CasReferralStatus.REJECTED),
      Arguments.of(Cas1AssessmentStatus.AWAITING_RESPONSE, CasReferralStatus.PENDING),
      Arguments.of(Cas1AssessmentStatus.IN_PROGRESS, CasReferralStatus.PENDING),
      Arguments.of(Cas1AssessmentStatus.NOT_STARTED, CasReferralStatus.PENDING),
    )

    @JvmStatic
    fun cas2StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(Cas2Status.PLACE_OFFERED, CasReferralStatus.ACCEPTED),
      Arguments.of(Cas2Status.OFFER_ACCEPTED, CasReferralStatus.ACCEPTED),
      Arguments.of(Cas2Status.OFFER_DECLINED_OR_WITHDRAWN, CasReferralStatus.REJECTED),
      Arguments.of(Cas2Status.REFERRAL_CANCELLED, CasReferralStatus.REJECTED),
      Arguments.of(Cas2Status.REFERRAL_WITHDRAWN, CasReferralStatus.REJECTED),
      Arguments.of(Cas2Status.MORE_INFORMATION_REQUESTED, CasReferralStatus.PENDING),
      Arguments.of(Cas2Status.AWAITING_ARRIVAL, CasReferralStatus.PENDING),
      Arguments.of(Cas2Status.ON_WAITING_LIST, CasReferralStatus.PENDING),
      Arguments.of(Cas2Status.AWAITING_DECISION, CasReferralStatus.PENDING),
    )

    @JvmStatic
    fun cas3StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(TemporaryAccommodationAssessmentStatus.READY_TO_PLACE, CasReferralStatus.ACCEPTED),
      Arguments.of(TemporaryAccommodationAssessmentStatus.CLOSED, CasReferralStatus.REJECTED),
      Arguments.of(TemporaryAccommodationAssessmentStatus.REJECTED, CasReferralStatus.REJECTED),
      Arguments.of(TemporaryAccommodationAssessmentStatus.UNALLOCATED, CasReferralStatus.PENDING),
      Arguments.of(TemporaryAccommodationAssessmentStatus.IN_REVIEW, CasReferralStatus.PENDING),
    )
  }
}
