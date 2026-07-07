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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory.ApplicationStatus
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

    assertThat(result).hasSize(3)
    assertThat(result.map { it.type }).containsExactlyInAnyOrder(
      AccommodationService.CAS1,
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
          assertThat(it.uiUrl).isNull()
        }
        AccommodationService.CAS1 -> {
          assertThat(it.referralRejectionReason).isEqualTo("Some reason")
          assertThat(it.localAuthorityArea).isEqualTo("Some area")
          assertThat(it.pdu).isEqualTo("Some pdu")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto(name = "Joe Bloggs"))
          assertThat(it.placementAddress).isEqualTo("Some address")
          assertThat(it.placementStatus).isEqualTo("arrived")
        }
        AccommodationService.CAS3 -> {
          assertThat(it.referralRejectionReason).isEqualTo("Some reason")
          assertThat(it.localAuthorityArea).isEqualTo("Some area")
          assertThat(it.pdu).isEqualTo("Some pdu")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto(name = "Joe Bloggs"))
          assertThat(it.placementAddress).isEqualTo("Some address")
          assertThat(it.placementStatus).isEqualTo("confirmed")
          assertThat(it.uiUrl).isEqualTo("https://example.com/referral")
        }
      }
    }
  }

  @ParameterizedTest
  @MethodSource("cas1StatusMappings")
  fun `should transform ApprovedPremisesApplicationStatus to CasReferralStatus`(
    input: ApprovedPremisesApplicationStatus,
    expected: AccommodationReferralStatus,
  ) {
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(input)).isEqualTo(expected)
  }

  @ParameterizedTest
  @MethodSource("cas3StatusMappings")
  fun `should transform ApplicationStatus to CasReferralStatus`(
    input: ApplicationStatus,
    expected: AccommodationReferralStatus,
  ) {
    assertThat(AccommodationReferralTransformer.toCasReferralStatus(input)).isEqualTo(expected)
  }

  @Test
  fun `all ApprovedPremisesApplicationStatus values map correctly`() {
    val expectedMapping = mapOf(
      ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED to AccommodationReferralStatus.ACCEPTED,
      ApprovedPremisesApplicationStatus.REJECTED to AccommodationReferralStatus.REJECTED,
      ApprovedPremisesApplicationStatus.INAPPLICABLE to AccommodationReferralStatus.REJECTED,
      ApprovedPremisesApplicationStatus.WITHDRAWN to AccommodationReferralStatus.REJECTED,
      ApprovedPremisesApplicationStatus.EXPIRED to AccommodationReferralStatus.REJECTED,
      ApprovedPremisesApplicationStatus.STARTED to AccommodationReferralStatus.PENDING,
      ApprovedPremisesApplicationStatus.AWAITING_ASSESSMENT to AccommodationReferralStatus.PENDING,
      ApprovedPremisesApplicationStatus.UNALLOCATED_ASSESMENT to AccommodationReferralStatus.PENDING,
      ApprovedPremisesApplicationStatus.ASSESSMENT_IN_PROGRESS to AccommodationReferralStatus.PENDING,
      ApprovedPremisesApplicationStatus.AWAITING_PLACEMENT to AccommodationReferralStatus.PENDING,
      ApprovedPremisesApplicationStatus.REQUESTED_FURTHER_INFORMATION to AccommodationReferralStatus.PENDING,
      ApprovedPremisesApplicationStatus.PENDING_PLACEMENT_REQUEST to AccommodationReferralStatus.PENDING,
    )

    ApprovedPremisesApplicationStatus.entries.forEach { status ->
      assertThat(AccommodationReferralTransformer.toCasReferralStatus(status)).isEqualTo(expectedMapping[status])
    }
  }

  @Test
  fun `all ApplicationStatus values map correctly`() {
    val expectations = mapOf(
      ApplicationStatus.REJECTED to AccommodationReferralStatus.REJECTED,
      ApplicationStatus.IN_PROGRESS to AccommodationReferralStatus.PENDING,
      ApplicationStatus.SUBMITTED to AccommodationReferralStatus.PENDING,
      ApplicationStatus.REQUESTED_FURTHER_INFORMATION to AccommodationReferralStatus.PENDING,
    )

    ApplicationStatus.entries.forEach { status ->
      assertThat(AccommodationReferralTransformer.toCasReferralStatus(status)).isEqualTo(expectations[status])
    }
  }

  private companion object {
    @JvmStatic
    fun cas1StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(ApprovedPremisesApplicationStatus.PLACEMENT_ALLOCATED, AccommodationReferralStatus.ACCEPTED),
      Arguments.of(ApprovedPremisesApplicationStatus.REJECTED, AccommodationReferralStatus.REJECTED),
      Arguments.of(ApprovedPremisesApplicationStatus.STARTED, AccommodationReferralStatus.PENDING),
    )

    @JvmStatic
    fun cas3StatusMappings(): Stream<Arguments> = Stream.of(
      Arguments.of(ApplicationStatus.REJECTED, AccommodationReferralStatus.REJECTED),
      Arguments.of(ApplicationStatus.SUBMITTED, AccommodationReferralStatus.PENDING),
    )
  }
}
