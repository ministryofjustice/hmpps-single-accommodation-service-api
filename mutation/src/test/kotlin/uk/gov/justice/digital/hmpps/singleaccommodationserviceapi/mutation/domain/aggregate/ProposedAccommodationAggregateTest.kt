package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementSubType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OffenderReleaseType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.ProposedAccommodationCreatedEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.ArrangementSubTypeDescriptionUnexpectedException
import java.time.LocalDate

class ProposedAccommodationAggregateTest {
  private val accommodationNameOne = "Mother's caravan"
  private val accommodationArrangementTypeOne = AccommodationArrangementType.PRIVATE
  private val accommodationArrangementSubTypeOne = AccommodationArrangementSubType.OTHER
  private val accommodationArrangementSubTypeDescriptionOne = "Caravan site"
  private val accommodationSettledTypeOne = AccommodationSettledType.SETTLED
  private val accommodationStatusOne = AccommodationStatus.NOT_CHECKED_YET
  private val accommodationOffenderReleaseTypeOne = OffenderReleaseType.REMAND
  private val accommodationPostcodeOne = "test postcode"
  private val accommodationSubBuildingNameOne = "test sub building name"
  private val accommodationBuildingNameOne = "test building name"
  private val accommodationBuildingNumberOne = "4"
  private val accommodationThoroughfareNameOne = "test thoroughfareName"
  private val accommodationDependentLocalityOne = "test dependent locality"
  private val accommodationPostTownOne = "test post town"
  private val accommodationCountyOne = "test county"
  private val accommodationCountryOne = "test country"
  private val accommodationUprnOne = "UP123454"
  private val accommodationStartDateOne = LocalDate.of(2026, 1, 5)
  private val accommodationEndDateOne = LocalDate.of(2026, 4, 25)

  @Test
  fun `should createProposedAccommodation and add ProposedAccommodationCreatedEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation()
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.name).isEqualTo(accommodationNameOne)
    assertThat(aggregateSnapshot.arrangementType).isEqualTo(accommodationArrangementTypeOne)
    assertThat(aggregateSnapshot.arrangementSubType).isEqualTo(accommodationArrangementSubTypeOne)
    assertThat(aggregateSnapshot.arrangementSubTypeDescription).isEqualTo(accommodationArrangementSubTypeDescriptionOne)
    assertThat(aggregateSnapshot.settledType).isEqualTo(accommodationSettledTypeOne)
    assertThat(aggregateSnapshot.status).isEqualTo(accommodationStatusOne)
    assertThat(aggregateSnapshot.address.postcode).isEqualTo(accommodationPostcodeOne)
    assertThat(aggregateSnapshot.address.subBuildingName).isEqualTo(accommodationSubBuildingNameOne)
    assertThat(aggregateSnapshot.address.buildingName).isEqualTo(accommodationBuildingNameOne)
    assertThat(aggregateSnapshot.address.buildingNumber).isEqualTo(accommodationBuildingNumberOne)
    assertThat(aggregateSnapshot.address.thoroughfareName).isEqualTo(accommodationThoroughfareNameOne)
    assertThat(aggregateSnapshot.address.dependentLocality).isEqualTo(accommodationDependentLocalityOne)
    assertThat(aggregateSnapshot.address.postTown).isEqualTo(accommodationPostTownOne)
    assertThat(aggregateSnapshot.address.county).isEqualTo(accommodationCountyOne)
    assertThat(aggregateSnapshot.address.country).isEqualTo(accommodationCountryOne)
    assertThat(aggregateSnapshot.address.uprn).isEqualTo(accommodationUprnOne)
    assertThat(aggregateSnapshot.offenderReleaseType).isEqualTo(accommodationOffenderReleaseTypeOne)
    assertThat(aggregateSnapshot.startDate).isEqualTo(accommodationStartDateOne)
    assertThat(aggregateSnapshot.endDate).isEqualTo(accommodationEndDateOne)

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(ProposedAccommodationCreatedEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregateSnapshot.id)
  }

  @Test
  fun `should throw ArrangementSubTypeDescriptionUnexpectedException domain exception`() {
    assertThrows<ArrangementSubTypeDescriptionUnexpectedException> {
      hydrateAndCreateProposedAccommodation(
        accommodationArrangementSubType = AccommodationArrangementSubType.FRIENDS_OR_FAMILY,
        accommodationArrangementSubTypeDescription = accommodationArrangementSubTypeDescriptionOne
      )
    }
  }

  private fun hydrateAndCreateProposedAccommodation(
    accommodationArrangementSubType: AccommodationArrangementSubType = accommodationArrangementSubTypeOne,
    accommodationArrangementSubTypeDescription: String = accommodationArrangementSubTypeDescriptionOne
  ): ProposedAccommodationAggregate {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(crn = "ABC1234")
    aggregate.createProposedAccommodation(
      newName = accommodationNameOne,
      newArrangementType = accommodationArrangementTypeOne,
      newArrangementSubType = accommodationArrangementSubType,
      newArrangementSubTypeDescription = accommodationArrangementSubTypeDescription,
      newSettledType = accommodationSettledTypeOne,
      newStatus = accommodationStatusOne,
      newAddress = AccommodationAddressDetails(
        postcode = accommodationPostcodeOne,
        subBuildingName = accommodationSubBuildingNameOne,
        buildingName = accommodationBuildingNameOne,
        buildingNumber = accommodationBuildingNumberOne,
        thoroughfareName = accommodationThoroughfareNameOne,
        dependentLocality = accommodationDependentLocalityOne,
        postTown = accommodationPostTownOne,
        county = accommodationCountyOne,
        country = accommodationCountryOne,
        uprn = accommodationUprnOne,
      ),
      newOffenderReleaseType = accommodationOffenderReleaseTypeOne,
      newStartDate = accommodationStartDateOne,
      newEndDate = accommodationEndDateOne,
    )
    return aggregate
  }
}
