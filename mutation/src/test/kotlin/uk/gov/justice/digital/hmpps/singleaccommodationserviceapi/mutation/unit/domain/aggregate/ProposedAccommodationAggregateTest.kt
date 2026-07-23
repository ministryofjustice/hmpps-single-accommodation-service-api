package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.domain.aggregate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationDeletedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationPersonArrivedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.SyncType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationPersonCannotArriveException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationVerificationNotPassedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID

class ProposedAccommodationAggregateTest {
  private val prisonAccommodationTypeCode = "HMP"
  private val startDate = LocalDate.now().minusDays(10)
  private val endDate = LocalDate.now().minusDays(1)

  private val accommodationDetails = buildProposedAccommodationDto(
    verificationStatus = VerificationStatus.PASSED,
  )

  @Test
  fun `should createProposedAccommodation and not add AccommodationUpdatedDomainEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.cprAddressId).isNull()
    assertThat(aggregateSnapshot.accommodationType).isEqualTo(accommodationDetails.accommodationType)
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
    assertThat(aggregateSnapshot.startDate).isEqualTo(startDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(endDate)
    assertThat(aggregateSnapshot.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(aggregateSnapshot.typeVerified).isFalse
    assertThat(aggregateSnapshot.noFixedAbode).isFalse

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should createProposedAccommodation and does not add AccommodationUpdatedDomainEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.NOT_CHECKED_YET,
      nextAccommodationStatus = NextAccommodationStatus.NO,
      startDate = startDate,
      endDate = endDate,
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.cprAddressId).isNull()
    assertThat(aggregateSnapshot.verificationStatus).isEqualTo(VerificationStatus.NOT_CHECKED_YET)
    assertThat(aggregateSnapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.NO)
    assertThat(aggregateSnapshot.accommodationType).isEqualTo(accommodationDetails.accommodationType)
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
    assertThat(aggregateSnapshot.startDate).isEqualTo(startDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(endDate)
    assertThat(aggregateSnapshot.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(aggregateSnapshot.typeVerified).isFalse
    assertThat(aggregateSnapshot.noFixedAbode).isFalse

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should set accommodation status to PR1 when current accommodation is prison`() {
    val currentAccommodation = buildAccommodationSummaryDto(
      type = buildAccommodationTypeDto(
        code = prisonAccommodationTypeCode,
        description = "Prison",
      ),
    )
    val aggregate = hydrateAndCreateProposedAccommodation(
      currentAccommodation = currentAccommodation,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )
    val snapshot = aggregate.snapshot()
    assertThat(snapshot.accommodationStatus?.code)
      .isEqualTo(AddressStatusCode.PR1.name)
    assertThat(snapshot.accommodationStatus?.description)
      .isEqualTo(AddressStatusCode.PR1.description)
  }

  @Test
  fun `should set accommodation status to PR when current accommodation is not prison`() {
    val currentAccommodation = buildAccommodationSummaryDto(
      type = buildAccommodationTypeDto(
        code = "A02",
        description = "Approved Premises",
      ),
    )
    val aggregate = hydrateAndCreateProposedAccommodation(
      currentAccommodation = currentAccommodation,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )
    val snapshot = aggregate.snapshot()
    assertThat(snapshot.accommodationStatus?.code)
      .isEqualTo(AddressStatusCode.PR.name)
    assertThat(snapshot.accommodationStatus?.description)
      .isEqualTo(AddressStatusCode.PR.description)
  }

  @Test
  fun `should set accommodation status to PR when current accommodation is null`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      currentAccommodation = null,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )
    val snapshot = aggregate.snapshot()
    assertThat(snapshot.accommodationStatus?.code)
      .isEqualTo(AddressStatusCode.PR.name)
    assertThat(snapshot.accommodationStatus?.description)
      .isEqualTo(AddressStatusCode.PR.description)
  }

  @Test
  fun `should throw AccommodationVerificationNotPassedException domain exception when verification not passes and trying to set as next accommodation`() {
    assertThrows<AccommodationVerificationNotPassedException> {
      hydrateAndCreateProposedAccommodation(
        verificationStatus = VerificationStatus.NOT_CHECKED_YET,
        nextAccommodationStatus = NextAccommodationStatus.YES,
        startDate = startDate,
        endDate = endDate,
      )
    }
  }

  @Test
  fun `should downgrade next accommodation status to NO when verification failed and trying to set as next accommodation xxx`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.FAILED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.verificationStatus).isEqualTo(VerificationStatus.FAILED)
    assertThat(snapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.NO)
    assertThat(snapshot.accommodationStatus).isNull()
  }

  @Test
  fun `should require CPR registration when next accommodation is YES and not registered with CPR`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )

    assertThat(aggregate.requiresCprRegistration()).isTrue
  }

  @Test
  fun `should not require CPR registration when next accommodation is NO`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.NOT_CHECKED_YET,
      nextAccommodationStatus = NextAccommodationStatus.NO,
      startDate = startDate,
      endDate = endDate,
    )

    assertThat(aggregate.requiresCprRegistration()).isFalse
  }

  @Test
  fun `should not require CPR registration when already registered with CPR`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.markRegisteredWithCpr(UUID.randomUUID())

    assertThat(aggregate.requiresCprRegistration()).isFalse
  }

  @Test
  fun `should mark proposed accommodation as registered with CPR`() {
    val aggregate = hydrateAggregate(startDate = startDate, endDate = endDate)
    val cprAddressId = UUID.randomUUID()

    aggregate.markRegisteredWithCpr(cprAddressId)

    assertThat(aggregate.snapshot().cprAddressId).isEqualTo(cprAddressId)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and SAS update to address data`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
      newAddress = AccommodationAddressDetails(
        postcode = "NEW POSTCODE",
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and SAS update to address startDate`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      startDate = startDate,
      endDate = endDate,
    )
    val newStartDate = startDate?.plusDays(1) ?: LocalDate.now().plusDays(1)

    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = newStartDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and SAS update to accommodationType data`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      accommodationType = buildAccommodationTypeDto(
        code = "A02",
      ),
      startDate = startDate,
      endDate = endDate,
    )
    val newAccommodationType = buildAccommodationTypeDto(
      code = "A07B",
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = newAccommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and SAS update to endDate data`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      startDate = startDate,
      endDate = endDate,
    )
    val newEndDate = endDate?.plusDays(1) ?: LocalDate.now().plusDays(1)

    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = startDate,
      newEndDate = newEndDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @Test
  fun `should not add AccommodationUpdatedDomainEvent when registered with CPR and SAS update but no relevant CPR fields change`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).isEmpty()
  }

  @Test
  fun `should not add AccommodationUpdatedDomainEvent when not registered with CPR and SAS update to relevant CPR fields`() {
    val aggregate = hydrateAggregate(
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
      newAddress = AccommodationAddressDetails(
        postcode = "NEW POSTCODE",
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).isEmpty()
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when accommodation source is DELIUS and SAS updates the address data`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      accommodationSource = AccommodationSource.DELIUS,
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
      newAddress = AccommodationAddressDetails(
        postcode = "NEW POSTCODE",
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @ParameterizedTest
  @EnumSource(value = NextAccommodationStatus::class, names = ["NO", "TO_BE_DECIDED"])
  fun `should add AccommodationDeletedDomainEvent when registered with CPR and next accommodation changes`(nextAccommodationStatus: NextAccommodationStatus) {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationDeletedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
    assertThat(aggregate.snapshot().cprAddressId).isNull()
  }

  @Test
  fun `should add AccommodationDeletedDomainEvent when registered with CPR and verification changes from passed to failed`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
      newVerificationStatus = VerificationStatus.FAILED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.verificationStatus).isEqualTo(VerificationStatus.FAILED)
    assertThat(aggregateSnapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.NO)

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationDeletedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregateSnapshot.id)
    assertThat(aggregate.snapshot().cprAddressId).isNull()
  }

  @Test
  fun `should not add AccommodationDeletedDomainEvent when not registered with CPR and next accommodation changes to NO`() {
    val aggregate = hydrateAggregate(
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.NO,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @ParameterizedTest
  @EnumSource(value = AddressStatusCode::class, names = ["PR", "PR1"])
  fun `should arrive person at proposed accommodation when registered with CPR and proposed address status is PR`(addressStatusCode: AddressStatusCode) {
    val cprAddressId = UUID.randomUUID()
    val aggregate = hydrateAggregate(
      typeVerified = false,
      cprAddressId = cprAddressId,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      accommodationStatus = AccommodationStatusDto(
        code = addressStatusCode.name,
        description = addressStatusCode.description,
      ),
      startDate = startDate,
      endDate = endDate,
    )
    val arrivalDate = LocalDate.of(2026, 2, 5)

    aggregate.arrivePersonAtProposedAccommodation(arrivalDate)

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.accommodationStatus?.code).isEqualTo(AddressStatusCode.M.name)
    assertThat(aggregateSnapshot.accommodationStatus?.description).isEqualTo(AddressStatusCode.M.description)
    assertThat(aggregateSnapshot.typeVerified).isTrue
    assertThat(aggregateSnapshot.startDate).isEqualTo(arrivalDate)
    assertThat(aggregateSnapshot.endDate).isNull()
    assertThat(aggregateSnapshot.cprAddressId).isEqualTo(cprAddressId)

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationPersonArrivedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregateSnapshot.id)
  }

  @Test
  fun `should throw AccommodationPersonCannotArriveException when proposed accommodation is not registered with CPR`() {
    val aggregate = hydrateAggregate(
      cprAddressId = null,
      typeVerified = false,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      accommodationStatus = AccommodationStatusDto(
        code = AddressStatusCode.PR.name,
        description = AddressStatusCode.PR.description,
      ),
      startDate = startDate,
      endDate = endDate,
    )

    assertThrows<AccommodationPersonCannotArriveException> {
      aggregate.arrivePersonAtProposedAccommodation(LocalDate.now())
    }

    assertThat(aggregate.pullDomainEvents()).isEmpty()
  }

  @Test
  fun `should throw AccommodationPersonCannotArriveException when proposed accommodation is not next accommodation`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      typeVerified = false,
      accommodationStatus = AccommodationStatusDto(
        code = AddressStatusCode.PR.name,
        description = AddressStatusCode.PR.description,
      ),
      nextAccommodationStatus = NextAccommodationStatus.NO,
      startDate = startDate,
      endDate = endDate,
    )

    assertThrows<AccommodationPersonCannotArriveException> {
      aggregate.arrivePersonAtProposedAccommodation(LocalDate.now())
    }

    assertThat(aggregate.pullDomainEvents()).isEmpty()
  }

  @Test
  fun `should throw AccommodationPersonCannotArriveException when accommodation status is not proposed`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      typeVerified = false,
      accommodationStatus = AccommodationStatusDto(
        code = AddressStatusCode.M.name,
        description = AddressStatusCode.M.description,
      ),
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )

    assertThrows<AccommodationPersonCannotArriveException> {
      aggregate.arrivePersonAtProposedAccommodation(LocalDate.now())
    }

    assertThat(aggregate.pullDomainEvents()).isEmpty()
  }

  @Test
  fun `should throw AccommodationPersonCannotArriveException when accommodation status is null`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      typeVerified = false,
      accommodationStatus = null,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )

    assertThrows<AccommodationPersonCannotArriveException> {
      aggregate.arrivePersonAtProposedAccommodation(LocalDate.now())
    }

    assertThat(aggregate.pullDomainEvents()).isEmpty()
  }

  @Test
  fun `should addNote successfully`() {
    val aggregate = hydrateAggregate(
      startDate = startDate,
      endDate = endDate,
    )
    val note = "note"
    aggregate.addNote(note)

    val aggregateSnapshot = aggregate.snapshot()

    assertThat(aggregateSnapshot.notes.first().id).isNotNull
    assertThat(aggregateSnapshot.notes.first().note).isEqualTo(note)
    assertThat(aggregateSnapshot.accommodationType).isEqualTo(accommodationDetails.accommodationType)
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
    assertThat(aggregateSnapshot.startDate).isEqualTo(startDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(endDate)

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @ParameterizedTest
  @ValueSource(strings = ["", " ", "   ", "\t", "\n"])
  fun `addNote should throw NoteIsEmptyException domain exception when note is blank`(note: String) {
    assertThrows<NoteIsEmptyException> {
      val aggregate = hydrateAggregate(
        startDate = startDate,
        endDate = endDate,
      )
      aggregate.addNote(note)
    }
  }

  @Test
  fun `addNote should throw NoteIsGreaterThanMaxLengthException domain exception when note is greater than 4000 chars`() {
    assertThrows<NoteIsGreaterThanMaxLengthException> {
      val aggregate = hydrateAggregate(
        startDate = startDate,
        endDate = endDate,
      )
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

  @Test
  fun `should set typeVerified to false always for Proposed Accommodation as represents them living in the property`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      accommodationSource = AccommodationSource.SAS,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      startDate = startDate,
      endDate = endDate,
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.typeVerified).isFalse
  }

  @Test
  fun `should syncProposedAccommodation for create scenario (where exists in nDelius and not SAS) and not add AccommodationUpdatedDomainEvent domain event to list`() {
    val caseId = UUID.randomUUID()
    val cprAddressId = UUID.randomUUID()
    val aggregate = ProposedAccommodationAggregate.hydrateNew(
      caseId = caseId,
      cprAddressId = cprAddressId,
      currentAccommodation = null,
      accommodationSource = AccommodationSource.DELIUS,
    )
    aggregate.syncProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = true,
      newTypeVerified = true,
      newAccommodationStatus = AccommodationStatusDto(
        code = AddressStatusCode.PR.name,
        description = AddressStatusCode.PR.description,
      ),
      syncType = SyncType.CREATE,
    )

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.caseId).isEqualTo(caseId)
    assertThat(aggregateSnapshot.cprAddressId).isEqualTo(cprAddressId)
    assertThat(aggregateSnapshot.accommodationType).isEqualTo(accommodationDetails.accommodationType)
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
    assertThat(aggregateSnapshot.startDate).isEqualTo(startDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(endDate)
    assertThat(aggregateSnapshot.accommodationSource).isEqualTo(AccommodationSource.DELIUS)
    assertThat(aggregateSnapshot.typeVerified).isTrue
    assertThat(aggregateSnapshot.noFixedAbode).isTrue

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should syncProposedAccommodation for update scenario (where exists in both nDelius and SAS) and not add AccommodationUpdatedDomainEvent domain event to list`() {
    val caseId = UUID.randomUUID()
    val commonCprAddressId = UUID.randomUUID()
    val aggregate = ProposedAccommodationAggregate.hydrateExisting(
      id = UUID.randomUUID(),
      caseId = caseId,
      accommodationSource = AccommodationSource.SAS,
      currentAccommodation = null,
      cprAddressId = commonCprAddressId,
      accommodationType = buildAccommodationTypeDto(
        code = AddressUsageCode.A07B.name,
      ),
      accommodationStatus = buildAccommodationStatusDto(
        code = AddressStatusCode.PR1.name,
      ),
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      address = AccommodationAddressDetails(
        postcode = "initial postcode",
        subBuildingName = "initial subBuildingName",
        buildingName = "initial buildingName",
        buildingNumber = "initial buildingNumber",
        thoroughfareName = "initial thoroughfareName",
        dependentLocality = "initial dependentLocality",
        postTown = "initial postTown",
        county = "initial county",
        country = "initial country",
        uprn = "initial uprn",
      ),
      startDate = LocalDate.now().minusDays(10),
      endDate = null,
      typeVerified = false,
      noFixedAbode = false,
      notes = emptyList(),
    )
    val newAccommodationType = buildAccommodationTypeDto(
      code = AddressUsageCode.A07A.name,
    )
    val newAccommodationStatus = buildAccommodationStatusDto(
      code = AddressStatusCode.PR1.name,
    )
    val newStartDate = LocalDate.now().minusDays(5)
    val newEndDate = LocalDate.now()
    val newAddress = AccommodationAddressDetails(
      postcode = "new postcode",
      subBuildingName = "new subBuildingName",
      buildingName = "new buildingName",
      buildingNumber = "new buildingNumber",
      thoroughfareName = "new thoroughfareName",
      dependentLocality = "new dependentLocality",
      postTown = "new postTown",
      county = "new county",
      country = "new country",
      uprn = "new uprn",
    )
    aggregate.syncProposedAccommodation(
      newAccommodationType = newAccommodationType,
      newAccommodationStatus = newAccommodationStatus,
      newAddress = newAddress,
      newStartDate = newStartDate,
      newEndDate = newEndDate,
      newTypeVerified = true,
      newNoFixedAbode = true,
      syncType = SyncType.UPDATE,
    )

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.caseId).isEqualTo(caseId)
    assertThat(aggregateSnapshot.cprAddressId).isEqualTo(commonCprAddressId)
    assertThat(aggregateSnapshot.accommodationType).isEqualTo(newAccommodationType)
    assertThat(aggregateSnapshot.accommodationStatus).isEqualTo(newAccommodationStatus)
    assertThat(aggregateSnapshot.verificationStatus).isEqualTo(VerificationStatus.PASSED)
    assertThat(aggregateSnapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.YES)
    assertThat(aggregateSnapshot.address).isEqualTo(newAddress)
    assertThat(aggregateSnapshot.startDate).isEqualTo(newStartDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(newEndDate)
    assertThat(aggregateSnapshot.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(aggregateSnapshot.typeVerified).isTrue
    assertThat(aggregateSnapshot.noFixedAbode).isTrue

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should createProposedAccommodation when accommodationType is null`() {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(
      caseId = UUID.randomUUID(),
      cprAddressId = null,
      currentAccommodation = null,
      accommodationSource = AccommodationSource.SAS,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = null,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.accommodationType).isNull()

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should update proposed accommodation when accommodationType changes from non-null to null`() {
    val aggregate = hydrateAggregate(
      accommodationType = buildAccommodationTypeDto(
        code = "A02",
      ),
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = null,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.accommodationType).isNull()

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and accommodationType changes from non-null to null`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      accommodationType = buildAccommodationTypeDto(
        code = "A02",
      ),
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = null,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and accommodationType changes from null to non-null`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      accommodationType = null,
      startDate = startDate,
      endDate = endDate,
    )
    val newAccommodationType = buildAccommodationTypeDto(
      code = "A07B",
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = newAccommodationType,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.accommodationType).isEqualTo(newAccommodationType)

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregateSnapshot.id)
  }

  @Test
  fun `should not add AccommodationUpdatedDomainEvent when registered with CPR and accommodationType remains null`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      accommodationType = null,
      startDate = startDate,
      endDate = endDate,
    )

    aggregate.updateProposedAccommodation(
      newAccommodationType = null,
      newVerificationStatus = VerificationStatus.PASSED,
      newNextAccommodationStatus = NextAccommodationStatus.YES,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = false,
    )

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.accommodationType).isNull()

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).isEmpty()
  }

  @Test
  fun `should syncProposedAccommodation for create scenario when accommodationType is null`() {
    val caseId = UUID.randomUUID()
    val cprAddressId = UUID.randomUUID()
    val aggregate = ProposedAccommodationAggregate.hydrateNew(
      caseId = caseId,
      cprAddressId = cprAddressId,
      currentAccommodation = null,
      accommodationSource = AccommodationSource.DELIUS,
    )

    aggregate.syncProposedAccommodation(
      newAccommodationType = null,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = true,
      newTypeVerified = true,
      newAccommodationStatus = AccommodationStatusDto(
        code = AddressStatusCode.PR.name,
        description = AddressStatusCode.PR.description,
      ),
      syncType = SyncType.CREATE,
    )

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.caseId).isEqualTo(caseId)
    assertThat(aggregateSnapshot.cprAddressId).isEqualTo(cprAddressId)
    assertThat(aggregateSnapshot.accommodationType).isNull()
    assertThat(aggregateSnapshot.verificationStatus).isEqualTo(VerificationStatus.PASSED)
    assertThat(aggregateSnapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.YES)
    assertThat(aggregateSnapshot.accommodationSource).isEqualTo(AccommodationSource.DELIUS)
    assertThat(aggregateSnapshot.typeVerified).isTrue
    assertThat(aggregateSnapshot.noFixedAbode).isTrue

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should syncProposedAccommodation for update scenario when accommodationType is null`() {
    val caseId = UUID.randomUUID()
    val commonCprAddressId = UUID.randomUUID()
    val aggregate = ProposedAccommodationAggregate.hydrateExisting(
      id = UUID.randomUUID(),
      caseId = caseId,
      accommodationSource = AccommodationSource.SAS,
      currentAccommodation = null,
      cprAddressId = commonCprAddressId,
      accommodationType = buildAccommodationTypeDto(
        code = AddressUsageCode.A07B.name,
      ),
      accommodationStatus = buildAccommodationStatusDto(
        code = AddressStatusCode.PR1.name,
      ),
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      address = AccommodationAddressDetails(
        postcode = "initial postcode",
        subBuildingName = "initial subBuildingName",
        buildingName = "initial buildingName",
        buildingNumber = "initial buildingNumber",
        thoroughfareName = "initial thoroughfareName",
        dependentLocality = "initial dependentLocality",
        postTown = "initial postTown",
        county = "initial county",
        country = "initial country",
        uprn = "initial uprn",
      ),
      startDate = LocalDate.now().minusDays(10),
      endDate = null,
      typeVerified = false,
      noFixedAbode = false,
      notes = emptyList(),
    )
    val newAccommodationStatus = buildAccommodationStatusDto(
      code = AddressStatusCode.PR1.name,
    )
    val newStartDate = LocalDate.now().minusDays(5)
    val newEndDate = LocalDate.now()
    val newAddress = AccommodationAddressDetails(
      postcode = "new postcode",
      subBuildingName = "new subBuildingName",
      buildingName = "new buildingName",
      buildingNumber = "new buildingNumber",
      thoroughfareName = "new thoroughfareName",
      dependentLocality = "new dependentLocality",
      postTown = "new postTown",
      county = "new county",
      country = "new country",
      uprn = "new uprn",
    )

    aggregate.syncProposedAccommodation(
      newAccommodationType = null,
      newAccommodationStatus = newAccommodationStatus,
      newAddress = newAddress,
      newStartDate = newStartDate,
      newEndDate = newEndDate,
      newTypeVerified = true,
      newNoFixedAbode = true,
      syncType = SyncType.UPDATE,
    )

    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.caseId).isEqualTo(caseId)
    assertThat(aggregateSnapshot.cprAddressId).isEqualTo(commonCprAddressId)
    assertThat(aggregateSnapshot.accommodationType).isNull()
    assertThat(aggregateSnapshot.accommodationStatus).isEqualTo(newAccommodationStatus)
    assertThat(aggregateSnapshot.verificationStatus).isEqualTo(VerificationStatus.PASSED)
    assertThat(aggregateSnapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.YES)
    assertThat(aggregateSnapshot.address).isEqualTo(newAddress)
    assertThat(aggregateSnapshot.startDate).isEqualTo(newStartDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(newEndDate)
    assertThat(aggregateSnapshot.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(aggregateSnapshot.typeVerified).isTrue
    assertThat(aggregateSnapshot.noFixedAbode).isTrue

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  private fun shouldSuccessfullyAddNote(note: String) {
    val aggregate = hydrateAggregate(
      startDate = startDate,
      endDate = endDate,
    )
    aggregate.addNote(note)
    assertThat(aggregate.snapshot().notes.first().note).isEqualTo(note)
  }

  private fun hydrateAggregate(
    currentAccommodation: AccommodationSummaryDto? = null,
    accommodationStatus: AccommodationStatusDto? = null,
    cprAddressId: UUID? = null,
    accommodationSource: AccommodationSource = AccommodationSource.SAS,
    verificationStatus: VerificationStatus = accommodationDetails.verificationStatus!!,
    nextAccommodationStatus: NextAccommodationStatus = accommodationDetails.nextAccommodationStatus!!,
    noFixedAbode: Boolean? = false,
    typeVerified: Boolean = false,
    accommodationType: AccommodationTypeDto? = buildAccommodationTypeDto(),
    startDate: LocalDate,
    endDate: LocalDate,
  ) = ProposedAccommodationAggregate.hydrateExisting(
    id = UUID.randomUUID(),
    caseId = UUID.randomUUID(),
    accommodationSource = accommodationSource,
    currentAccommodation = currentAccommodation,
    cprAddressId = cprAddressId,
    accommodationType = accommodationType,
    accommodationStatus = accommodationStatus,
    verificationStatus = verificationStatus,
    nextAccommodationStatus = nextAccommodationStatus,
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
    startDate = startDate,
    endDate = endDate,
    typeVerified = typeVerified,
    noFixedAbode = noFixedAbode,
    notes = emptyList(),
  )

  private fun hydrateAndCreateProposedAccommodation(
    cprAddressId: UUID? = null,
    currentAccommodation: AccommodationSummaryDto? = null,
    accommodationSource: AccommodationSource = AccommodationSource.SAS,
    verificationStatus: VerificationStatus,
    nextAccommodationStatus: NextAccommodationStatus,
    noFixedAbode: Boolean = false,
    startDate: LocalDate,
    endDate: LocalDate,
  ): ProposedAccommodationAggregate {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(
      caseId = UUID.randomUUID(),
      cprAddressId = cprAddressId,
      currentAccommodation = currentAccommodation,
      accommodationSource = accommodationSource,
    )
    aggregate.updateProposedAccommodation(
      newAccommodationType = accommodationDetails.accommodationType,
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
      newStartDate = startDate,
      newEndDate = endDate,
      newNoFixedAbode = noFixedAbode,
    )
    return aggregate
  }
}
