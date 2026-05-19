package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.client.approvedpremises

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ReferralHistory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildReferralHistory

class ReferralHistoryTest {

  @Nested
  inner class Cas1 {

    @Test
    fun `new fields default to null when not provided`() {
      val referralHistory = buildReferralHistory(status = Cas1ReferralHistory.Cas1AssessmentStatus.NOT_STARTED)

      assertThat(referralHistory.referralRejectionReason).isNull()
      assertThat(referralHistory.localAuthorityArea).isNull()
      assertThat(referralHistory.pdu).isNull()
      assertThat(referralHistory.referredBy).isNull()
      assertThat(referralHistory.placementAddress).isNull()
      assertThat(referralHistory.placementStatus).isNull()
    }

    @Test
    fun `new fields are set correctly when provided`() {
      val referralHistory = buildReferralHistory(
        status = Cas1ReferralHistory.Cas1AssessmentStatus.COMPLETED,
        referralRejectionReason = "Not suitable",
        localAuthorityArea = "Greater Manchester",
        pdu = "PDU-001",
        referredBy = "John Smith",
        placementAddress = "123 Main Street",
        placementStatus = "active",
      )

      assertThat(referralHistory.referralRejectionReason).isEqualTo("Not suitable")
      assertThat(referralHistory.localAuthorityArea).isEqualTo("Greater Manchester")
      assertThat(referralHistory.pdu).isEqualTo("PDU-001")
      assertThat(referralHistory.referredBy).isEqualTo("John Smith")
      assertThat(referralHistory.placementAddress).isEqualTo("123 Main Street")
      assertThat(referralHistory.placementStatus).isEqualTo("active")
    }
  }

  @Nested
  inner class Cas2 {

    @Test
    fun `new fields default to null when not provided`() {
      val referralHistory = buildReferralHistory(status = Cas2ReferralHistory.Cas2Status.AWAITING_DECISION)

      assertThat(referralHistory.referralRejectionReason).isNull()
      assertThat(referralHistory.localAuthorityArea).isNull()
      assertThat(referralHistory.pdu).isNull()
      assertThat(referralHistory.referredBy).isNull()
      assertThat(referralHistory.placementAddress).isNull()
      assertThat(referralHistory.placementStatus).isNull()
    }

    @Test
    fun `new fields are set correctly when provided`() {
      val referralHistory = buildReferralHistory(
        status = Cas2ReferralHistory.Cas2Status.PLACE_OFFERED,
        referralRejectionReason = "Not suitable",
        localAuthorityArea = "Greater Manchester",
        pdu = "PDU-001",
        referredBy = "John Smith",
        placementAddress = "123 Main Street",
        placementStatus = "active",
      )

      assertThat(referralHistory.referralRejectionReason).isEqualTo("Not suitable")
      assertThat(referralHistory.localAuthorityArea).isEqualTo("Greater Manchester")
      assertThat(referralHistory.pdu).isEqualTo("PDU-001")
      assertThat(referralHistory.referredBy).isEqualTo("John Smith")
      assertThat(referralHistory.placementAddress).isEqualTo("123 Main Street")
      assertThat(referralHistory.placementStatus).isEqualTo("active")
    }
  }

  @Nested
  inner class Cas3 {

    @Test
    fun `new fields default to null when not provided`() {
      val referralHistory = buildReferralHistory(status = Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus.UNALLOCATED)

      assertThat(referralHistory.referralRejectionReason).isNull()
      assertThat(referralHistory.localAuthorityArea).isNull()
      assertThat(referralHistory.pdu).isNull()
      assertThat(referralHistory.referredBy).isNull()
      assertThat(referralHistory.placementAddress).isNull()
      assertThat(referralHistory.placementStatus).isNull()
    }

    @Test
    fun `new fields are set correctly when provided`() {
      val referralHistory = buildReferralHistory(
        status = Cas3ReferralHistory.TemporaryAccommodationAssessmentStatus.READY_TO_PLACE,
        referralRejectionReason = "Not suitable",
        localAuthorityArea = "Greater Manchester",
        pdu = "PDU-001",
        referredBy = "John Smith",
        placementAddress = "123 Main Street",
        placementStatus = "active",
      )

      assertThat(referralHistory.referralRejectionReason).isEqualTo("Not suitable")
      assertThat(referralHistory.localAuthorityArea).isEqualTo("Greater Manchester")
      assertThat(referralHistory.pdu).isEqualTo("PDU-001")
      assertThat(referralHistory.referredBy).isEqualTo("John Smith")
      assertThat(referralHistory.placementAddress).isEqualTo("123 Main Street")
      assertThat(referralHistory.placementStatus).isEqualTo("active")
    }
  }
}
