package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AddressUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationArrangementSubTypeDescriptionUnexpectedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationVerificationNotPassedException

class ProposedAccommodationAggregateTest {
  private val accommodationDetails = buildAccommodationDetail(
    verificationStatus = VerificationStatus.PASSED,
  )

  @Test
  fun `should createProposedAccommodation and add ProposedAccommodationCreatedEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
    )
    val aggregateSnapshot = aggregate.snapshot()
    Assertions.assertThat(aggregateSnapshot.name).isEqualTo(accommodationDetails.name)
    Assertions.assertThat(aggregateSnapshot.arrangementType).isEqualTo(accommodationDetails.arrangementType)
    Assertions.assertThat(aggregateSnapshot.arrangementSubType).isEqualTo(accommodationDetails.arrangementSubType)
    Assertions.assertThat(aggregateSnapshot.arrangementSubTypeDescription).isEqualTo(accommodationDetails.arrangementSubTypeDescription)
    Assertions.assertThat(aggregateSnapshot.settledType).isEqualTo(accommodationDetails.settledType)
    Assertions.assertThat(aggregateSnapshot.verificationStatus).isEqualTo(accommodationDetails.verificationStatus)
    Assertions.assertThat(aggregateSnapshot.address.postcode).isEqualTo(accommodationDetails.address.postcode)
    Assertions.assertThat(aggregateSnapshot.address.subBuildingName).isEqualTo(accommodationDetails.address.subBuildingName)
    Assertions.assertThat(aggregateSnapshot.address.buildingName).isEqualTo(accommodationDetails.address.buildingName)
    Assertions.assertThat(aggregateSnapshot.address.buildingNumber).isEqualTo(accommodationDetails.address.buildingNumber)
    Assertions.assertThat(aggregateSnapshot.address.thoroughfareName).isEqualTo(accommodationDetails.address.thoroughfareName)
    Assertions.assertThat(aggregateSnapshot.address.dependentLocality).isEqualTo(accommodationDetails.address.dependentLocality)
    Assertions.assertThat(aggregateSnapshot.address.postTown).isEqualTo(accommodationDetails.address.postTown)
    Assertions.assertThat(aggregateSnapshot.address.county).isEqualTo(accommodationDetails.address.county)
    Assertions.assertThat(aggregateSnapshot.address.country).isEqualTo(accommodationDetails.address.country)
    Assertions.assertThat(aggregateSnapshot.address.uprn).isEqualTo(accommodationDetails.address.uprn)
    Assertions.assertThat(aggregateSnapshot.offenderReleaseType).isEqualTo(accommodationDetails.offenderReleaseType)
    Assertions.assertThat(aggregateSnapshot.startDate).isEqualTo(accommodationDetails.startDate)
    Assertions.assertThat(aggregateSnapshot.endDate).isEqualTo(accommodationDetails.endDate)

    val domainEventsToPublish = aggregate.pullDomainEvents()
    Assertions.assertThat(domainEventsToPublish).hasSize(1)
    Assertions.assertThat(domainEventsToPublish.first()).isInstanceOf(AddressUpdatedDomainEvent::class.java)
    Assertions.assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregateSnapshot.id)
  }


  @Test
  fun `should createProposedAccommodation and does not add ProposedAccommodationCreatedEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.NOT_CHECKED_YET,
      nextAccommodationStatus = NextAccommodationStatus.NO,
    )
    val aggregateSnapshot = aggregate.snapshot()
    Assertions.assertThat(aggregateSnapshot.verificationStatus).isEqualTo(VerificationStatus.NOT_CHECKED_YET)
    Assertions.assertThat(aggregateSnapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.NO)
    Assertions.assertThat(aggregateSnapshot.name).isEqualTo(accommodationDetails.name)
    Assertions.assertThat(aggregateSnapshot.arrangementType).isEqualTo(accommodationDetails.arrangementType)
    Assertions.assertThat(aggregateSnapshot.arrangementSubType).isEqualTo(accommodationDetails.arrangementSubType)
    Assertions.assertThat(aggregateSnapshot.arrangementSubTypeDescription).isEqualTo(accommodationDetails.arrangementSubTypeDescription)
    Assertions.assertThat(aggregateSnapshot.settledType).isEqualTo(accommodationDetails.settledType)
    Assertions.assertThat(aggregateSnapshot.address.postcode).isEqualTo(accommodationDetails.address.postcode)
    Assertions.assertThat(aggregateSnapshot.address.subBuildingName).isEqualTo(accommodationDetails.address.subBuildingName)
    Assertions.assertThat(aggregateSnapshot.address.buildingName).isEqualTo(accommodationDetails.address.buildingName)
    Assertions.assertThat(aggregateSnapshot.address.buildingNumber).isEqualTo(accommodationDetails.address.buildingNumber)
    Assertions.assertThat(aggregateSnapshot.address.thoroughfareName).isEqualTo(accommodationDetails.address.thoroughfareName)
    Assertions.assertThat(aggregateSnapshot.address.dependentLocality).isEqualTo(accommodationDetails.address.dependentLocality)
    Assertions.assertThat(aggregateSnapshot.address.postTown).isEqualTo(accommodationDetails.address.postTown)
    Assertions.assertThat(aggregateSnapshot.address.county).isEqualTo(accommodationDetails.address.county)
    Assertions.assertThat(aggregateSnapshot.address.country).isEqualTo(accommodationDetails.address.country)
    Assertions.assertThat(aggregateSnapshot.address.uprn).isEqualTo(accommodationDetails.address.uprn)
    Assertions.assertThat(aggregateSnapshot.offenderReleaseType).isEqualTo(accommodationDetails.offenderReleaseType)
    Assertions.assertThat(aggregateSnapshot.startDate).isEqualTo(accommodationDetails.startDate)
    Assertions.assertThat(aggregateSnapshot.endDate).isEqualTo(accommodationDetails.endDate)

    val domainEventsToPublish = aggregate.pullDomainEvents()
    Assertions.assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should throw AccommodationArrangementSubTypeDescriptionUnexpectedException domain exception when sub-type is OTHER and description is null or empty`() {
    assertThrows<AccommodationArrangementSubTypeDescriptionUnexpectedException> {
      hydrateAndCreateProposedAccommodation(
        accommodationArrangementSubType = AccommodationArrangementSubType.OTHER,
        accommodationArrangementSubTypeDescription = "",
        verificationStatus = VerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = NextAccommodationStatus.NO,
      )
    }
    assertThrows<AccommodationArrangementSubTypeDescriptionUnexpectedException> {
      hydrateAndCreateProposedAccommodation(
        accommodationArrangementSubType = AccommodationArrangementSubType.OTHER,
        accommodationArrangementSubTypeDescription = null,
        verificationStatus = VerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = NextAccommodationStatus.NO,
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
        nextAccommodationStatus = NextAccommodationStatus.NO,
      )
    }
  }

  @Test
  fun `should throw AccommodationVerificationNotPassedException domain exception when verification not passes and trying to set as next accommodation`() {
    assertThrows<AccommodationVerificationNotPassedException> {
      hydrateAndCreateProposedAccommodation(
        verificationStatus = VerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = NextAccommodationStatus.YES,
      )
    }
    assertThrows<AccommodationVerificationNotPassedException> {
      hydrateAndCreateProposedAccommodation(
        verificationStatus = VerificationStatus.FAILED,
        nextAccommodationStatus = NextAccommodationStatus.YES,
      )
    }
  }

  private fun hydrateAndCreateProposedAccommodation(
    verificationStatus: VerificationStatus,
    nextAccommodationStatus: NextAccommodationStatus,
    accommodationArrangementSubType: AccommodationArrangementSubType? = accommodationDetails.arrangementSubType,
    accommodationArrangementSubTypeDescription: String? = accommodationDetails.arrangementSubTypeDescription,
  ): ProposedAccommodationAggregate {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(crn = "ABC1234")
    aggregate.createProposedAccommodation(
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