package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationArrangementSubTypeDescriptionUnexpectedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationVerificationNotPassedException

class ProposedAccommodationAggregateTest {
  private val accommodationDetails = buildAccommodationDetail(
    verificationStatus = VerificationStatus.PASSED
  )

  @Test
  fun `should createProposedAccommodation and add AccommodationUpdatedDomainEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.name).isEqualTo(accommodationDetails.name)
    assertThat(aggregateSnapshot.arrangementType).isEqualTo(accommodationDetails.arrangementType)
    assertThat(aggregateSnapshot.arrangementSubType).isEqualTo(accommodationDetails.arrangementSubType)
    assertThat(aggregateSnapshot.arrangementSubTypeDescription).isEqualTo(accommodationDetails.arrangementSubTypeDescription)
    assertThat(aggregateSnapshot.settledType).isEqualTo(accommodationDetails.settledType)
    assertThat(aggregateSnapshot.verificationStatus).isEqualTo(accommodationDetails.verificationStatus)
    assertThat(aggregateSnapshot.nextAccommodationStatus).isEqualTo(accommodationDetails.nextAccommodationStatus)
    assertThat(aggregateSnapshot.address.postcode).isEqualTo(accommodationDetails.address.postcode)
    assertThat(aggregateSnapshot.address.subBuildingName).isEqualTo(accommodationDetails.address.subBuildingName)
    assertThat(aggregateSnapshot.address.buildingName).isEqualTo(accommodationDetails.address.buildingName)
    assertThat(aggregateSnapshot.address.buildingNumber).isEqualTo(accommodationDetails.address.buildingNumber)
    assertThat(aggregateSnapshot.address.thoroughfareName).isEqualTo(accommodationDetails.address.thoroughfareName)
    assertThat(aggregateSnapshot.address.dependentLocality).isEqualTo(accommodationDetails.address.dependentLocality)
    assertThat(aggregateSnapshot.address.postTown).isEqualTo(accommodationDetails.address.postTown)
    assertThat(aggregateSnapshot.address.county).isEqualTo(accommodationDetails.address.county)
    assertThat(aggregateSnapshot.address.country).isEqualTo(accommodationDetails.address.country)
    assertThat(aggregateSnapshot.address.uprn).isEqualTo(accommodationDetails.address.uprn)
    assertThat(aggregateSnapshot.offenderReleaseType).isEqualTo(accommodationDetails.offenderReleaseType)
    assertThat(aggregateSnapshot.startDate).isEqualTo(accommodationDetails.startDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(accommodationDetails.endDate)

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregateSnapshot.id)
  }


  @Test
  fun `should createProposedAccommodation and does not add AccommodationUpdatedDomainEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.NOT_CHECKED_YET,
      nextAccommodationStatus = NextAccommodationStatus.NO
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.verificationStatus).isEqualTo(VerificationStatus.NOT_CHECKED_YET)
    assertThat(aggregateSnapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.NO)
    assertThat(aggregateSnapshot.name).isEqualTo(accommodationDetails.name)
    assertThat(aggregateSnapshot.arrangementType).isEqualTo(accommodationDetails.arrangementType)
    assertThat(aggregateSnapshot.arrangementSubType).isEqualTo(accommodationDetails.arrangementSubType)
    assertThat(aggregateSnapshot.arrangementSubTypeDescription).isEqualTo(accommodationDetails.arrangementSubTypeDescription)
    assertThat(aggregateSnapshot.settledType).isEqualTo(accommodationDetails.settledType)
    assertThat(aggregateSnapshot.address.postcode).isEqualTo(accommodationDetails.address.postcode)
    assertThat(aggregateSnapshot.address.subBuildingName).isEqualTo(accommodationDetails.address.subBuildingName)
    assertThat(aggregateSnapshot.address.buildingName).isEqualTo(accommodationDetails.address.buildingName)
    assertThat(aggregateSnapshot.address.buildingNumber).isEqualTo(accommodationDetails.address.buildingNumber)
    assertThat(aggregateSnapshot.address.thoroughfareName).isEqualTo(accommodationDetails.address.thoroughfareName)
    assertThat(aggregateSnapshot.address.dependentLocality).isEqualTo(accommodationDetails.address.dependentLocality)
    assertThat(aggregateSnapshot.address.postTown).isEqualTo(accommodationDetails.address.postTown)
    assertThat(aggregateSnapshot.address.county).isEqualTo(accommodationDetails.address.county)
    assertThat(aggregateSnapshot.address.country).isEqualTo(accommodationDetails.address.country)
    assertThat(aggregateSnapshot.address.uprn).isEqualTo(accommodationDetails.address.uprn)
    assertThat(aggregateSnapshot.offenderReleaseType).isEqualTo(accommodationDetails.offenderReleaseType)
    assertThat(aggregateSnapshot.startDate).isEqualTo(accommodationDetails.startDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(accommodationDetails.endDate)

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should throw AccommodationArrangementSubTypeDescriptionUnexpectedException domain exception when sub-type is OTHER and description is null or empty`() {
    assertThrows<AccommodationArrangementSubTypeDescriptionUnexpectedException> {
      hydrateAndCreateProposedAccommodation(
        accommodationArrangementSubType = AccommodationArrangementSubType.OTHER,
        accommodationArrangementSubTypeDescription = "",
          verificationStatus = VerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = NextAccommodationStatus.NO
      )
    }
    assertThrows<AccommodationArrangementSubTypeDescriptionUnexpectedException> {
      hydrateAndCreateProposedAccommodation(
        accommodationArrangementSubType = AccommodationArrangementSubType.OTHER,
        accommodationArrangementSubTypeDescription = null,
        verificationStatus = VerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = NextAccommodationStatus.NO
      )
    }
  }

  @Test
  fun `should throw AccommodationArrangementSubTypeDescriptionUnexpectedException domain exception when sub-type is not OTHER and description in included`() {
    assertThrows<AccommodationArrangementSubTypeDescriptionUnexpectedException> {
      hydrateAndCreateProposedAccommodation(
        accommodationArrangementSubType = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
        accommodationArrangementSubTypeDescription = "value",
        verificationStatus = VerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = NextAccommodationStatus.NO
      )
    }
  }

  @Test
  fun `should throw AccommodationVerificationNotPassedException domain exception when verification not passes and trying to set as next accommodation`() {
    assertThrows<AccommodationVerificationNotPassedException> {
      hydrateAndCreateProposedAccommodation(
        verificationStatus = VerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = NextAccommodationStatus.YES
      )
    }
    assertThrows<AccommodationVerificationNotPassedException> {
      hydrateAndCreateProposedAccommodation(
        verificationStatus = VerificationStatus.FAILED,
        nextAccommodationStatus = NextAccommodationStatus.YES
      )
    }
  }

  private fun hydrateAndCreateProposedAccommodation(
    verificationStatus: VerificationStatus,
    nextAccommodationStatus: NextAccommodationStatus,
    accommodationArrangementSubType: AccommodationArrangementSubType? = accommodationDetails.arrangementSubType,
    accommodationArrangementSubTypeDescription: String? = accommodationDetails.arrangementSubTypeDescription
  ): ProposedAccommodationAggregate {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(crn = "ABC1234")
    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
      newArrangementType = accommodationDetails.arrangementType,
      newArrangementSubType = accommodationArrangementSubType,
      newArrangementSubTypeDescription = accommodationArrangementSubTypeDescription,
      newSettledType = accommodationDetails.settledType,
      newVerificationStatus = verificationStatus,
      newNextAccommodationStatus = nextAccommodationStatus,
      newAddress = AccommodationAddressDetails(
        postcode = accommodationDetails.address.postcode,
        subBuildingName = accommodationDetails.address.subBuildingName,
        buildingName = accommodationDetails.address.buildingName,
        buildingNumber = accommodationDetails.address.buildingNumber,
        thoroughfareName = accommodationDetails.address.thoroughfareName,
        dependentLocality = accommodationDetails.address.dependentLocality,
        postTown = accommodationDetails.address.postTown,
        county = accommodationDetails.address.county,
        country = accommodationDetails.address.country,
        uprn = accommodationDetails.address.uprn,
      ),
      newOffenderReleaseType = accommodationDetails.offenderReleaseType,
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
    )
    return aggregate
  }
}
