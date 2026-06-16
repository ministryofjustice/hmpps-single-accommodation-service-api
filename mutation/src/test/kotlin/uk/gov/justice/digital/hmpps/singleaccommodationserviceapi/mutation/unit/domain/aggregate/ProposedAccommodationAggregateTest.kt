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
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationDeletedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.AccommodationUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.ProposedAccommodationAggregate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.AccommodationVerificationNotPassedException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsEmptyException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.NoteIsGreaterThanMaxLengthException
import java.time.LocalDate
import java.util.UUID

class ProposedAccommodationAggregateTest {
  private val prisonAccommodationTypeCode = "HMP"

  private val accommodationDetails = buildProposedAccommodationDto(
    verificationStatus = VerificationStatus.PASSED,
  )

  @Test
  fun `should createProposedAccommodation and not add AccommodationUpdatedDomainEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.cprAddressId).isNull()
    assertThat(aggregateSnapshot.name).isEqualTo(accommodationDetails.name)
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
    assertThat(aggregateSnapshot.startDate).isEqualTo(accommodationDetails.startDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(accommodationDetails.endDate)
    assertThat(aggregateSnapshot.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(aggregateSnapshot.typeVerified).isTrue
    assertThat(aggregateSnapshot.noFixedAbode).isFalse

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should hydrate and createProposedAccommodation for Delius use case where we have CPR address ID`() {
    val cprAddressId = UUID.randomUUID()
    val aggregate = hydrateAndCreateProposedAccommodation(
      cprAddressId = cprAddressId,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.cprAddressId).isEqualTo(cprAddressId)
    assertThat(aggregateSnapshot.name).isEqualTo(accommodationDetails.name)
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
    assertThat(aggregateSnapshot.startDate).isEqualTo(accommodationDetails.startDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(accommodationDetails.endDate)
    assertThat(aggregateSnapshot.accommodationSource).isEqualTo(AccommodationSource.SAS)
    assertThat(aggregateSnapshot.typeVerified).isTrue
    assertThat(aggregateSnapshot.noFixedAbode).isFalse

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
  }

  @Test
  fun `should createProposedAccommodation and does not add AccommodationUpdatedDomainEvent domain event to list`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.NOT_CHECKED_YET,
      nextAccommodationStatus = NextAccommodationStatus.NO,
    )
    val aggregateSnapshot = aggregate.snapshot()
    assertThat(aggregateSnapshot.cprAddressId).isNull()
    assertThat(aggregateSnapshot.verificationStatus).isEqualTo(VerificationStatus.NOT_CHECKED_YET)
    assertThat(aggregateSnapshot.nextAccommodationStatus).isEqualTo(NextAccommodationStatus.NO)
    assertThat(aggregateSnapshot.name).isEqualTo(accommodationDetails.name)
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
    assertThat(aggregateSnapshot.startDate).isEqualTo(accommodationDetails.startDate)
    assertThat(aggregateSnapshot.endDate).isEqualTo(accommodationDetails.endDate)
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
      )
    }
  }

  @Test
  fun `should downgrade next accommodation status to NO when verification failed and trying to set as next accommodation xxx`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.FAILED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
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
    )

    assertThat(aggregate.requiresCprRegistration()).isTrue
  }

  @Test
  fun `should not require CPR registration when next accommodation is NO`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.NOT_CHECKED_YET,
      nextAccommodationStatus = NextAccommodationStatus.NO,
    )

    assertThat(aggregate.requiresCprRegistration()).isFalse
  }

  @Test
  fun `should not require CPR registration when already registered with CPR`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
    )

    aggregate.markRegisteredWithCpr(UUID.randomUUID())

    assertThat(aggregate.requiresCprRegistration()).isFalse
  }

  @Test
  fun `should mark proposed accommodation as registered with CPR`() {
    val aggregate = hydrateAggregate()
    val cprAddressId = UUID.randomUUID()

    aggregate.markRegisteredWithCpr(cprAddressId)

    assertThat(aggregate.snapshot().cprAddressId).isEqualTo(cprAddressId)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and SAS source record and address changes`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
    )

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and SAS source record and start date changes`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
    )
    val newStartDate = accommodationDetails.startDate?.plusDays(1) ?: LocalDate.now().plusDays(1)

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and SAS source record and accommodationType changes`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      accommodationType = buildAccommodationTypeDto(
        code = "A02",
      ),
    )
    val newAccommodationType = buildAccommodationTypeDto(
      code = "A07B",
    )

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when registered with CPR and SAS source record and end date changes`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
    )
    val newEndDate = accommodationDetails.endDate?.plusDays(1) ?: LocalDate.now().plusDays(1)

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = newEndDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(1)
    assertThat(domainEventsToPublish.first()).isInstanceOf(AccommodationUpdatedDomainEvent::class.java)
    assertThat(domainEventsToPublish.first().aggregateId).isEqualTo(aggregate.snapshot().id)
  }

  @Test
  fun `should not add AccommodationUpdatedDomainEvent when registered with CPR and SAS source record and no relevant CPR fields change`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
    )

    aggregate.updateProposedAccommodation(
      newName = "Updated name",
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).isEmpty()
  }

  @Test
  fun `should not add AccommodationUpdatedDomainEvent when not registered with CPR and SAS source record and relevant CPR fields change`() {
    val aggregate = hydrateAggregate()

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).isEmpty()
  }

  @Test
  fun `should not add AccommodationUpdatedDomainEvent when accommodation source is DELIUS and address changes`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      accommodationSource = AccommodationSource.DELIUS,
    )

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = true,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.DELIUS,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).isEmpty()
  }

  @Test
  fun `should add AccommodationUpdatedDomainEvent when accommodation source is DELIUS but changes to SAS and has address changes`() {
    val aggregate = hydrateAggregate(
      cprAddressId = UUID.randomUUID(),
      accommodationSource = AccommodationSource.DELIUS,
    )

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = true,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
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
    )

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
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
    )

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
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
    val aggregate = hydrateAggregate()

    aggregate.updateProposedAccommodation(
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = null,
      newNoFixedAbode = false,
      newAccommodationSource = AccommodationSource.SAS,
    )

    val domainEventsToPublish = aggregate.pullDomainEvents()
    assertThat(domainEventsToPublish).hasSize(0)
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

  @Test
  fun `should set typeVerified to true when accommodation source is SAS and next accommodation is YES`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      accommodationSource = AccommodationSource.SAS,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      typeVerified = false,
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.typeVerified).isTrue
  }

  @Test
  fun `should set typeVerified to false when accommodation source is SAS and next accommodation is NO`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      accommodationSource = AccommodationSource.SAS,
      verificationStatus = VerificationStatus.NOT_CHECKED_YET,
      nextAccommodationStatus = NextAccommodationStatus.NO,
      typeVerified = true,
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.typeVerified).isFalse
  }

  @Test
  fun `should set typeVerified to provided value when accommodation source is DELIUS`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      accommodationSource = AccommodationSource.DELIUS,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      typeVerified = true,
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.typeVerified).isTrue
  }

  @Test
  fun `should set typeVerified to false when accommodation source is DELIUS and provided value is null`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      accommodationSource = AccommodationSource.DELIUS,
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      typeVerified = null,
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.typeVerified).isFalse
  }

  @Test
  fun `should set noFixedAbode in snapshot`() {
    val aggregate = hydrateAndCreateProposedAccommodation(
      verificationStatus = VerificationStatus.PASSED,
      nextAccommodationStatus = NextAccommodationStatus.YES,
      noFixedAbode = true,
    )

    val snapshot = aggregate.snapshot()

    assertThat(snapshot.noFixedAbode).isTrue
  }

  private fun shouldSuccessfullyAddNote(note: String) {
    val aggregate = hydrateAggregate()
    aggregate.addNote(note)
    assertThat(aggregate.snapshot().notes.first().note).isEqualTo(note)
  }

  private fun hydrateAggregate(
    currentAccommodation: AccommodationSummaryDto? = null,
    accommodationStatus: AccommodationStatusDto? = null,
    cprAddressId: UUID? = null,
    accommodationSource: AccommodationSource = AccommodationSource.SAS,
    noFixedAbode: Boolean? = false,
    accommodationType: AccommodationTypeDto = buildAccommodationTypeDto(),
  ) = ProposedAccommodationAggregate.hydrateExisting(
    id = UUID.randomUUID(),
    caseId = UUID.randomUUID(),
    accommodationSource = accommodationSource,
    currentAccommodation = currentAccommodation,
    cprAddressId = cprAddressId,
    name = accommodationDetails.name,
    accommodationType = accommodationType,
    accommodationStatus = accommodationStatus,
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
    startDate = accommodationDetails.startDate,
    endDate = accommodationDetails.endDate,
    typeVerified = NextAccommodationStatus.YES == accommodationDetails.nextAccommodationStatus,
    noFixedAbode = noFixedAbode,
    notes = emptyList(),
  )

  private fun hydrateAndCreateProposedAccommodation(
    cprAddressId: UUID? = null,
    currentAccommodation: AccommodationSummaryDto? = null,
    accommodationSource: AccommodationSource = AccommodationSource.SAS,
    verificationStatus: VerificationStatus,
    nextAccommodationStatus: NextAccommodationStatus,
    typeVerified: Boolean? = true,
    noFixedAbode: Boolean = false,
  ): ProposedAccommodationAggregate {
    val aggregate = ProposedAccommodationAggregate.hydrateNew(
      caseId = UUID.randomUUID(),
      cprAddressId = cprAddressId,
      currentAccommodation = currentAccommodation,
    )
    aggregate.updateProposedAccommodation(
      newAccommodationSource = accommodationSource,
      newName = accommodationDetails.name,
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
      newStartDate = accommodationDetails.startDate,
      newEndDate = accommodationDetails.endDate,
      newTypeVerified = typeVerified,
      newNoFixedAbode = noFixedAbode,
    )
    return aggregate
  }
}
