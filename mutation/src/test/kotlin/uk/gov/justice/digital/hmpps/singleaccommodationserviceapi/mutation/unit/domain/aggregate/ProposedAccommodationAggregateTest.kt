package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationArrangementSubTypeDescriptionUnexpectedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationVerificationNotPassedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.util.UUID

class ProposedAccommodationAggregateTest {
  private val accommodationDetails = buildAccommodationDetail(
    verificationStatus = VerificationStatus.PASSED,
  )

  @Test
  fun `should createProposedAccommodation and add AccommodationUpdatedDomainEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
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
      nextAccommodationStatus = NextAccommodationStatus.NO,
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

  @Test
  fun `should addNote successfully`() {
    val aggregate = hydrateAggregate()
    val note = "note"
    aggregate.addNote(note)

    val aggregateSnapshot = aggregate.snapshot()

    assertThat(aggregateSnapshot.notes.first().id).isNotNull
    assertThat(aggregateSnapshot.notes.first().note).isEqualTo(note)
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
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `addNote should throw NoteIsEmptyException domain exception when note is blank`(note: String) {
    assertThrows<NoteIsEmptyException> {
      val aggregate = hydrateAggregate()
      aggregate.addNote(note)
    }
  }

  @Test
  fun `addNote should throw NoteIsGreaterThanMaxLengthException domain exception when note is greater than 4000 chars`() {
    assertThrows<NoteIsGreaterThanMaxLengthException> {
      val aggregate = hydrateAggregate()
      aggregate.addNote(note = "a".repeat(4001))
    }
  }

  @Test
  fun `addNote should not throw exception when the note length is within the min-length and max-length boundaries`() {
    shouldSuccessfullyAddNote(note = "a")
    shouldSuccessfullyAddNote(note = "a".repeat(10))
    shouldSuccessfullyAddNote(note = "a".repeat(100))
    shouldSuccessfullyAddNote(note = "a".repeat(1000))
    shouldSuccessfullyAddNote(note = "a".repeat(2000))
    shouldSuccessfullyAddNote(note = "a".repeat(3000))
    shouldSuccessfullyAddNote(note = "a".repeat(4000))
  }

  private fun shouldSuccessfullyAddNote(note: String) {
    val aggregate = hydrateAggregate()
    aggregate.addNote(note)
    assertThat(aggregate.snapshot().notes.first().note).isEqualTo(note)
  }

  private fun hydrateAggregate() = ProposedAccommodationAggregate.hydrateExisting(
    id = UUID.randomUUID(),
    caseId = UUID.randomUUID(),
    name = accommodationDetails.name,
    arrangementType = accommodationDetails.arrangementType,
    arrangementSubType = accommodationDetails.arrangementSubType,
    arrangementSubTypeDescription = accommodationDetails.arrangementSubTypeDescription,
    settledType = accommodationDetails.settledType,
    verificationStatus = accommodationDetails.verificationStatus!!,
    nextAccommodationStatus = accommodationDetails.nextAccommodationStatus!!,
    address = AccommodationAddressDetails(
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
    offenderReleaseType = accommodationDetails.offenderReleaseType,
    startDate = accommodationDetails.startDate,
    endDate = accommodationDetails.endDate,
    notes = emptyList(),
  )

  private fun hydrateAndCreateProposedAccommodation(
    verificationStatus: VerificationStatus,
    nextAccommodationStatus: NextAccommodationStatus,
    accommodationArrangementSubType: AccommodationArrangementSubType? = accommodationDetails.arrangementSubType,
    accommodationArrangementSubTypeDescription: String? = accommodationDetails.arrangementSubTypeDescription,
  ): ProposedAccommodationAggregate {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(caseId = UUID.randomUUID())
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
