package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.accommodation

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetailDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationStatusDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1SuitablePremisesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3SuitablePremisesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.Prisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationStatusEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import java.time.LocalDate
import java.time.ZoneId

private const val PRISON_ACCOMMODATION_TYPE_CODE = "HMP"

object AccommodationTransformer {

  fun getAccommodationStatus(currentAccommodation: AccommodationSummaryDto?): AccommodationStatusDto? = if (PRISON_ACCOMMODATION_TYPE_CODE == currentAccommodation?.type?.code) {
    AccommodationStatusDto(
      code = AddressStatusCode.PR1.name,
      description = AddressStatusCode.PR1.description,
    )
  } else {
    AccommodationStatusDto(
      code = AddressStatusCode.PR.name,
      description = AddressStatusCode.PR.description,
    )
  }

  fun toAccommodationSummary(
    crn: String,
    address: CanonicalAddress,
  ) = AccommodationSummaryDto(
    crn = crn,
    startDate = address.startDate?.let { LocalDate.parse(it) },
    endDate = address.endDate?.let { LocalDate.parse(it) },
    address = AccommodationAddressDetails(
      postcode = address.postcode,
      subBuildingName = address.subBuildingName,
      buildingName = address.buildingName,
      buildingNumber = address.buildingNumber,
      thoroughfareName = address.thoroughfareName,
      dependentLocality = address.dependentLocality,
      postTown = address.postTown,
      county = address.county,
      country = address.countryCode,
      uprn = address.uprn,
    ),
    status = address.status.code?.let {
      AccommodationStatusDto(
        code = it,
        description = address.status.description,
      )
    },
    type = address.usages
      .firstOrNull { it.isActive && it.usageCode.code != null }
      ?.let {
        AccommodationTypeDto(
          code = it.usageCode.code!!,
          description = it.usageCode.description,
        )
      },
  )

  fun toAccommodationSummary(
    crn: String,
    premises: Cas1SuitablePremisesDto,
    currentAccommodation: AccommodationSummaryDto?,
  ) = AccommodationSummaryDto(
    crn = crn,
    startDate = premises.startDate,
    endDate = premises.endDate,
    address = AccommodationAddressDetails(
      postcode = premises.postcode,
      subBuildingName = null,
      buildingName = null,
      buildingNumber = null,
      thoroughfareName = premises.addressLine1,
      dependentLocality = premises.addressLine2,
      postTown = premises.town,
      county = null,
      country = null,
      uprn = null,
    ),
    status = getAccommodationStatus(currentAccommodation),
    type = AccommodationTypeDto(
      code = AddressUsageCode.A02.name,
      description = AddressUsageCode.A02.description,
    ),
  )

  fun toAccommodationSummary(
    crn: String,
    prisoner: Prisoner,
  ) = AccommodationSummaryDto(
    crn = crn,
    startDate = null,
    endDate = prisoner.releaseDate,
    address = AccommodationAddressDetails(
      postcode = null,
      subBuildingName = null,
      buildingName = prisoner.prisonName,
      buildingNumber = null,
      thoroughfareName = null,
      dependentLocality = null,
      postTown = null,
      county = null,
      country = null,
      uprn = null,
    ),
    status = AccommodationStatusDto(
      code = "C",
      description = "Custody",
    ),
    type = AccommodationTypeDto(
      code = PRISON_ACCOMMODATION_TYPE_CODE,
      description = prisoner.prisonName,
    ),
  )

  fun toAccommodationSummary(
    crn: String,
    premises: Cas3SuitablePremisesDto,
    currentAccommodation: AccommodationSummaryDto?,
  ) = AccommodationSummaryDto(
    crn = crn,
    startDate = premises.startDate,
    endDate = premises.endDate,
    address = AccommodationAddressDetails(
      postcode = premises.postcode,
      subBuildingName = null,
      buildingName = premises.name,
      buildingNumber = null,
      thoroughfareName = premises.addressLine1,
      dependentLocality = premises.addressLine2,
      postTown = premises.town,
      county = null,
      country = null,
      uprn = null,
    ),
    status = getAccommodationStatus(currentAccommodation),
    type = AccommodationTypeDto(
      code = AddressUsageCode.A17.name,
      description = AddressUsageCode.A17.description,
    ),
  )

  fun toAccommodationSummary(
    crn: String,
    proposedAccommodationEntity: ProposedAccommodationEntity,
    accommodationTypeEntity: AccommodationTypeEntity,
    accommodationStatusEntity: AccommodationStatusEntity?,
  ) = AccommodationSummaryDto(
    crn = crn,
    startDate = proposedAccommodationEntity.createdAt?.atZone(ZoneId.systemDefault())?.toLocalDate(),
    endDate = null,
    address = AccommodationAddressDetails(
      postcode = proposedAccommodationEntity.postcode,
      subBuildingName = proposedAccommodationEntity.subBuildingName,
      buildingName = proposedAccommodationEntity.buildingName,
      buildingNumber = proposedAccommodationEntity.buildingNumber,
      thoroughfareName = proposedAccommodationEntity.throughfareName,
      dependentLocality = proposedAccommodationEntity.dependentLocality,
      postTown = proposedAccommodationEntity.postTown,
      county = proposedAccommodationEntity.county,
      country = proposedAccommodationEntity.country,
      uprn = proposedAccommodationEntity.uprn,
    ),
    status = accommodationStatusEntity?.let {
      AccommodationStatusDto(
        code = it.code,
        description = it.name,
      )
    },
    type = accommodationTypeEntity.let {
      AccommodationTypeDto(
        code = it.code,
        description = it.name,
      )
    },
  )

  fun toAccommodationDetail(
    crn: String,
    proposedAccommodationEntity: ProposedAccommodationEntity,
    accommodationTypeEntity: AccommodationTypeEntity,
    accommodationStatusEntity: AccommodationStatusEntity?,
  ) = AccommodationDetailDto(
    crn = crn,
    cprAddressId = proposedAccommodationEntity.cprAddressId,
    startDate = proposedAccommodationEntity.createdAt?.atZone(ZoneId.systemDefault())?.toLocalDate(),
    endDate = null,
    address = AccommodationAddressDetails(
      postcode = proposedAccommodationEntity.postcode,
      subBuildingName = proposedAccommodationEntity.subBuildingName,
      buildingName = proposedAccommodationEntity.buildingName,
      buildingNumber = proposedAccommodationEntity.buildingNumber,
      thoroughfareName = proposedAccommodationEntity.throughfareName,
      dependentLocality = proposedAccommodationEntity.dependentLocality,
      postTown = proposedAccommodationEntity.postTown,
      county = proposedAccommodationEntity.county,
      country = proposedAccommodationEntity.country,
      uprn = proposedAccommodationEntity.uprn,
    ),
    status = accommodationStatusEntity?.let {
      AccommodationStatusDto(
        code = it.code,
        description = it.name,
      )
    },
    type = accommodationTypeEntity.let {
      AccommodationTypeDto(
        code = it.code,
        description = it.name,
      )
    },
    typeVerified = proposedAccommodationEntity.typeVerified,
    noFixedAbode = proposedAccommodationEntity.noFixedAbode,
  )
}
