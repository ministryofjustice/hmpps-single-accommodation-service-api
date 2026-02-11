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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationDetail
import java.time.Instant
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AddressUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationArrangementSubTypeDescriptionUnexpectedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationVerificationNotPassedException

class ProposedAccommodationAggregateTest {
  private val accommodationDetails = buildAccommodationDetail(
    verificationStatus = VerificationStatus.PASSED
  )

  @Test
  fun `should createProposedAccommodation and add ProposedAccommodationCreatedEvent domain event to list`() {
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
    assertThat(domainEventsToPublish.first()).isInstanceOf(AddressUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregateSnapshot.id)
  }


  @Test
  fun `should createProposedAccommodation and does not add ProposedAccommodationCreatedEvent domain event to list`() {
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

  @Test
  fun `should updateProposedAccommodation preserving id and createdAt and applying new values`() {
    val fixedId = UUID.randomUUID()
    val fixedCreatedAt = Instant.parse("2025-01-01T10:00:00Z")
    val aggregate = buildExistingAggregate(id = fixedId, createdAt = fixedCreatedAt)

    val newAddress = buildAccommodationAddressDetails(postcode = "NEW 1AA")
    aggregate.updateProposedAccommodation(
      newName = "Updated Name",
      newArrangementType = AccommodationArrangementType.PRIVATE,
      newArrangementSubType = AccommodationArrangementSubType.OWNED,
      newArrangementSubTypeDescription = null,
      newSettledType = AccommodationSettledType.SETTLED,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
      newAddress = newAddress,
      newOffenderReleaseType = OffenderReleaseType.LICENCE,
      newStartDate = null,
      newEndDate = null,
    )

    val snapshot = aggregate.snapshot()
    assertThat(snapshot.id).isEqualTo(fixedId)
    assertThat(snapshot.createdAt).isEqualTo(fixedCreatedAt)
    assertThat(snapshot.name).isEqualTo("Updated Name")
    assertThat(snapshot.arrangementType).isEqualTo(AccommodationArrangementType.PRIVATE)
    assertThat(snapshot.arrangementSubType).isEqualTo(AccommodationArrangementSubType.OWNED)
    assertThat(snapshot.settledType).isEqualTo(AccommodationSettledType.SETTLED)
    assertThat(snapshot.verificationStatus).isEqualTo(VerificationStatus.PASSED)
    assertThat(snapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.YES)
    assertThat(snapshot.offenderReleaseType).isEqualTo(OffenderReleaseType.LICENCE)
    assertThat(snapshot.address.postcode).isEqualTo("NEW 1AA")

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AddressUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(fixedId)
  }

  @Test
  fun `should updateProposedAccommodation and not raise event when nextAccommodationStatus is not YES`() {
    val aggregate = buildExistingAggregate()

    aggregate.updateProposedAccommodation(
      newName = "Updated Name",
      newArrangementType = AccommodationArrangementType.PRIVATE,
      newArrangementSubType = AccommodationArrangementSubType.OWNED,
      newArrangementSubTypeDescription = null,
      newSettledType = AccommodationSettledType.SETTLED,
      newVerificationStatus = VerificationStatus.NOT_CHECKED_YET,
      newNextAccommodationStatus = NextAccommodationStatus.NO,
      newAddress = buildAccommodationAddressDetails(),
      newOffenderReleaseType = null,
      newStartDate = null,
      newEndDate = null,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should throw AccommodationVerificationNotPassedException on updateProposedAccommodation when verification not passed and nextAccommodationStatus is YES`() {
    val aggregate = buildExistingAggregate()

    assertThrows<AccommodationVerificationNotPassedException> {
      aggregate.updateProposedAccommodation(
        newName = "Updated",
        newArrangementType = AccommodationArrangementType.PRIVATE,
        newArrangementSubType = AccommodationArrangementSubType.OWNED,
        newArrangementSubTypeDescription = null,
        newSettledType = AccommodationSettledType.SETTLED,
        newVerificationStatus = VerificationStatus.NOT_CHECKED_YET,
        newNextAccommodationStatus = NextAccommodationStatus.YES,
        newAddress = buildAccommodationAddressDetails(),
        newOffenderReleaseType = null,
        newStartDate = null,
        newEndDate = null,
      )
    }
  }

  @Test
  fun `should throw AccommodationArrangementSubTypeDescriptionUnexpectedException on updateProposedAccommodation with invalid arrangement`() {
    val aggregate = buildExistingAggregate()

    assertThrows<AccommodationArrangementSubTypeDescriptionUnexpectedException> {
      aggregate.updateProposedAccommodation(
        newName = "Updated",
        newArrangementType = AccommodationArrangementType.PRIVATE,
        newArrangementSubType = AccommodationArrangementSubType.OTHER,
        newArrangementSubTypeDescription = null,
        newSettledType = AccommodationSettledType.SETTLED,
        newVerificationStatus = VerificationStatus.NOT_CHECKED_YET,
        newNextAccommodationStatus = NextAccommodationStatus.NO,
        newAddress = buildAccommodationAddressDetails(),
        newOffenderReleaseType = null,
        newStartDate = null,
        newEndDate = null,
      )
    }
  }

  private fun buildExistingAggregate(
    id: UUID = UUID.randomUUID(),
    createdAt: Instant = Instant.now(),
  ) = ProposedAccommodationAggregate.hydrateExisting(
    id = id,
    crn = "ABC1234",
    createdAt = createdAt,
    name = "Old Name",
    arrangementType = AccommodationArrangementType.PRISON,
    arrangementSubType = null,
    arrangementSubTypeDescription = null,
    settledType = AccommodationSettledType.TRANSIENT,
    verificationStatus = VerificationStatus.NOT_CHECKED_YET,
    nextAccommodationStatus = NextAccommodationStatus.NO,
    offenderReleaseType = null,
    address = buildAccommodationAddressDetails(),
    startDate = null,
    endDate = null,
    lastUpdatedAt = null,
  )

  private fun hydrateAndCreateProposedAccommodation(
    verificationStatus: VerificationStatus,
    nextAccommodationStatus: NextAccommodationStatus,
    accommodationArrangementSubType: AccommodationArrangementSubType? = accommodationDetails.arrangementSubType,
    accommodationArrangementSubTypeDescription: String? = accommodationDetails.arrangementSubTypeDescription
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
