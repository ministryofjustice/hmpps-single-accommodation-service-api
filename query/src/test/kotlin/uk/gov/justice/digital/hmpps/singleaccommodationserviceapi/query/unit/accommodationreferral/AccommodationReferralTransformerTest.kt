package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildStaffDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.Cas1AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory.Cas2Status
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildAccommodationReferralOrchestrationDto
import java.util.stream.Stream

class AccommodationReferralTransformerTest {
  @Test
  fun `should transform orchestration dto to list of accommodation referral dtos`() {
    val orchestrationDto = buildAccommodationReferralOrchestrationDto()

    val result = AccommodationReferralTransformer.transformReferrals(
      orchestrationDto,
      listOf(buildDutyToReferDto(submission = buildDtrSubmission(createdByUsername = "TEST_USER"))),
    )

    assertThat(result).hasSize(5)
    assertThat(result.map { it.type }).containsExactlyInAnyOrder(
      AccommodationService.CAS1,
      AccommodationService.CAS2HDC,
      AccommodationService.CAS2,
      AccommodationService.CAS3,
      AccommodationService.DTR,
    )

    result.forEach {
      when (it.type) {
        AccommodationService.DTR -> {
          assertThat(it.referralRejectionReason).isEqualTo("NOT_ELIGIBLE")
          assertThat(it.localAuthorityArea).isEqualTo("localAuthorityAreaName")
          assertThat(it.pdu).isEqualTo("localAuthorityAreaName")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto("Someone", "TEST_USER", null))
          assertThat(it.placementAddress).isNull()
          assertThat(it.placementStatus).isEqualTo("NO_LOCAL_CONNECTION")
        }
        else -> {
          assertThat(it.referralRejectionReason).isEqualTo("Some reason")
          assertThat(it.localAuthorityArea).isEqualTo("Some area")
          assertThat(it.pdu).isEqualTo("Some pdu")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto(name = "Joe Bloggs"))
          assertThat(it.placementAddress).isEqualTo("Some address")
          assertThat(it.placementStatus).isEqualTo("Some status")
        }
      }
    }
  }

  @ParameterizedTest
  @MethodSource("cas1StatusMappings")
  fun `should transform Cas1AssessmentStatus to CasReferralStatus`(
    input: Cas1AssessmentStatus,
    expected: AccommodationReferralStatus,
  ) {
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(input)).isEqualTo(expected)
  }

  @ParameterizedTest
  @MethodSource("cas2StatusMappings")
  fun `should transform Cas2Status to CasReferralStatus`(
    input: Cas2Status,
    expected: AccommodationReferralStatus,
  ) {
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(input)).isEqualTo(expected)
  }

  @ParameterizedTest
  @MethodSource("cas3StatusMappings")
  fun `should transform TemporaryAccommodationAssessmentStatus to CasReferralStatus`(
    input: TemporaryAccommodationAssessmentStatus,
    expected: AccommodationReferralStatus,
  ) {
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(input)).isEqualTo(expected)
  }

  @Test
  fun `all Cas1AssessmentStatus values map correctly`() {
    val expectedMapping = mapOf(
      Cas1AssessmentStatus.COMPLETED to AccommodationReferralStatus.ACCEPTED,
      Cas1AssessmentStatus.REALLOCATED to AccommodationReferralStatus.REJECTED,
      Cas1AssessmentStatus.AWAITING_RESPONSE to AccommodationReferralStatus.PENDING,
      Cas1AssessmentStatus.IN_PROGRESS to AccommodationReferralStatus.PENDING,
      Cas1AssessmentStatus.NOT_STARTED to AccommodationReferralStatus.PENDING,
    )

    Cas1AssessmentStatus.entries.forEach { status ->
      assertThat(AccommodationReferralTransformer.toCasReferralStatus(status)).isEqualTo(expectedMapping[status])
    }
  }

  @Test
  fun `all Cas2Status values map to correct CasReferralStatus`() {
    val expectations = mapOf(
      Cas2Status.PLACE_OFFERED to AccommodationReferralStatus.ACCEPTED,
      Cas2Status.OFFER_ACCEPTED to AccommodationReferralStatus.ACCEPTED,

      Cas2Status.OFFER_DECLINED_OR_WITHDRAWN to AccommodationReferralStatus.REJECTED,
      Cas2Status.REFERRAL_CANCELLED to AccommodationReferralStatus.REJECTED,
      Cas2Status.REFERRAL_WITHDRAWN to AccommodationReferralStatus.REJECTED,

      Cas2Status.MORE_INFORMATION_REQUESTED to AccommodationReferralStatus.PENDING,
      Cas2Status.AWAITING_ARRIVAL to AccommodationReferralStatus.PENDING,
      Cas2Status.ON_WAITING_LIST to AccommodationReferralStatus.PENDING,
      Cas2Status.AWAITING_DECISION to AccommodationReferralStatus.PENDING,
    )

    Cas2Status.entries.forEach { status ->
      assertThat(AccommodationReferralTransformer.toCasReferralStatus(status)).isEqualTo(expectations[status])
    }
  }

  @Test
  fun `TemporaryAccommodationAssessmentStatus maps to correct CasReferralStatus`() {
    val expectations = mapOf(
      TemporaryAccommodationAssessmentStatus.READY_TO_PLACE to AccommodationReferralStatus.ACCEPTED,

      TemporaryAccommodationAssessmentStatus.CLOSED to AccommodationReferralStatus.REJECTED,
      TemporaryAccommodationAssessmentStatus.REJECTED to AccommodationReferralStatus.REJECTED,

      TemporaryAccommodationAssessmentStatus.UNALLOCATED to AccommodationReferralStatus.PENDING,
      TemporaryAccommodationAssessmentStatus.IN_REVIEW to AccommodationReferralStatus.PENDING,
    )

    TemporaryAccommodationAssessmentStatus.entries.forEach { status ->
      assertThat(AccommodationReferralTransformer.toCasReferralStatus(status)).isEqualTo(expectations[status])
    }
  }

  private companion object {
    @JvmStatic
    fun cas1StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(Cas1AssessmentStatus.COMPLETED, AccommodationReferralStatus.ACCEPTED),
      Arguments.of(Cas1AssessmentStatus.REALLOCATED, AccommodationReferralStatus.REJECTED),
      Arguments.of(Cas1AssessmentStatus.AWAITING_RESPONSE, AccommodationReferralStatus.PENDING),
      Arguments.of(Cas1AssessmentStatus.IN_PROGRESS, AccommodationReferralStatus.PENDING),
      Arguments.of(Cas1AssessmentStatus.NOT_STARTED, AccommodationReferralStatus.PENDING),
    )

    @JvmStatic
    fun cas2StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(Cas2Status.PLACE_OFFERED, AccommodationReferralStatus.ACCEPTED),
      Arguments.of(Cas2Status.OFFER_ACCEPTED, AccommodationReferralStatus.ACCEPTED),
      Arguments.of(Cas2Status.OFFER_DECLINED_OR_WITHDRAWN, AccommodationReferralStatus.REJECTED),
      Arguments.of(Cas2Status.REFERRAL_CANCELLED, AccommodationReferralStatus.REJECTED),
      Arguments.of(Cas2Status.REFERRAL_WITHDRAWN, AccommodationReferralStatus.REJECTED),
      Arguments.of(Cas2Status.MORE_INFORMATION_REQUESTED, AccommodationReferralStatus.PENDING),
      Arguments.of(Cas2Status.AWAITING_ARRIVAL, AccommodationReferralStatus.PENDING),
      Arguments.of(Cas2Status.ON_WAITING_LIST, AccommodationReferralStatus.PENDING),
      Arguments.of(Cas2Status.AWAITING_DECISION, AccommodationReferralStatus.PENDING),
    )

    @JvmStatic
    fun cas3StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(TemporaryAccommodationAssessmentStatus.READY_TO_PLACE, AccommodationReferralStatus.ACCEPTED),
      Arguments.of(TemporaryAccommodationAssessmentStatus.CLOSED, AccommodationReferralStatus.REJECTED),
      Arguments.of(TemporaryAccommodationAssessmentStatus.REJECTED, AccommodationReferralStatus.REJECTED),
      Arguments.of(TemporaryAccommodationAssessmentStatus.UNALLOCATED, AccommodationReferralStatus.PENDING),
      Arguments.of(TemporaryAccommodationAssessmentStatus.IN_REVIEW, AccommodationReferralStatus.PENDING),
    )
  }
}
