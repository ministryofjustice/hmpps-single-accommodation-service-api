package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.accommodationreferral

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDtrSubmission
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildStaffDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory.ApprovedPremisesApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDeliusUserDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodationreferral.AccommodationReferralTransformer
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildAccommodationReferralOrchestrationDto
import java.time.LocalDate

class AccommodationReferralTransformerTest {
  @Test
  fun `should transform orchestration dto to list of accommodation referral dtos`() {
    val orchestrationDto = buildAccommodationReferralOrchestrationDto()

    val result = AccommodationReferralTransformer.transformReferrals(
      orchestrationDto,
      listOf(buildDutyToReferDto(submission = buildDtrSubmission(createdByUsername = "TEST_USER"))),
    )

    assertThat(result).hasSize(4)
    assertThat(result.map { it.type }).containsExactlyInAnyOrder(
      AccommodationService.CAS1,
      AccommodationService.CAS2,
      AccommodationService.CAS3,
      AccommodationService.DTR,
    )

    result.forEach {
      when (it.type) {
        AccommodationService.DTR -> {
          assertThat(it.referralRejectionReason).isEqualTo("NOT_ELIGIBLE")
          assertThat(it.withdrawalReason).isEqualTo("NOT_ELIGIBLE")
          assertThat(it.localAuthorityArea).isEqualTo("localAuthorityAreaName")
          assertThat(it.pdu).isEqualTo("localAuthorityAreaName")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto("Someone", "TEST_USER"))
          assertThat(it.placementAddress).isNull()
          assertThat(it.placementStatus).isEqualTo("NO_LOCAL_CONNECTION")
          assertThat(it.uiUrl).isNull()
        }

        AccommodationService.CAS1 -> {
          assertThat(it.referralRejectionReason).isEqualTo("Some reason")
          assertThat(it.withdrawalReason).isNull()
          assertThat(it.localAuthorityArea).isEqualTo("Some area")
          assertThat(it.pdu).isEqualTo("Some pdu")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto(name = "Joe Bloggs"))
          assertThat(it.placementAddress).isEqualTo("Some address")
          assertThat(it.placementStatus).isEqualTo("notArrived")
        }

        AccommodationService.CAS3 -> {
          assertThat(it.referralRejectionReason).isEqualTo("Some reason")
          assertThat(it.withdrawalReason).isNull()
          assertThat(it.localAuthorityArea).isEqualTo("Some area")
          assertThat(it.pdu).isEqualTo("Some pdu")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto(name = "Joe Bloggs"))
          assertThat(it.placementAddress).isEqualTo("Some address")
          assertThat(it.placementStatus).isEqualTo("departed")
          assertThat(it.uiUrl).isEqualTo("https://example.com/referral")
        }

        AccommodationService.CAS2 -> {
          assertThat(it.status).isEqualTo(AccommodationReferralStatus.CANCELLED)
          assertThat(it.referralRejectionReason).isEqualTo("Some reason")
          assertThat(it.referralRejectionReasonDetail).isNull()
          assertThat(it.withdrawalReason).isNull()
          assertThat(it.localAuthorityArea).isEqualTo("Some area")
          assertThat(it.pdu).isEqualTo("Some pdu")
          assertThat(it.referredBy).isEqualTo(buildStaffDetailDto(name = "Joe Bloggs", username = null))
          assertThat(it.placementAddress).isEqualTo("Some address")
          assertThat(it.placementStatus).isNull()
          assertThat(it.uiUrl).isEqualTo("https://example.com/referral")
          assertThat(it.applicationLastUpdatedDate).isEqualTo(LocalDate.now())
          assertThat(it.date).isEqualTo(LocalDate.now())
        }

        else -> {}
      }
    }
  }

  @Test
  fun `should transform CAS1 withdrawal reason`() {
    val referral = buildReferralHistory(
      applicationStatus = ApprovedPremisesApplicationStatus.WITHDRAWN,
      withdrawalReason = "DuplicatePlacementRequest",
      referredBy = buildDeliusUserDto(),
    )
    val orchestrationDto = buildAccommodationReferralOrchestrationDto(cas1Referrals = listOf(referral), cas2Referrals = emptyList(), cas3Referrals = emptyList())

    val result = AccommodationReferralTransformer.transformReferrals(
      orchestrationDto,
      emptyList(),
    )

    assertThat(result).hasSize(1)
    assertThat(result.first().withdrawalReason).isEqualTo("DuplicatePlacementRequest")
  }
}
