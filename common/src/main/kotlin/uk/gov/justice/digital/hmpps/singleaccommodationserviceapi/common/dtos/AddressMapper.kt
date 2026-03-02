package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

class AddressMapper {
  fun Address.toAccommodationAddressDetails() = AccommodationAddressDetails(
    uprn = uprn,
    subBuildingName = subBuildingName,
    buildingName = buildingName,
    buildingNumber = buildingNumber,
    thoroughfareName = street,
    dependentLocality = locality,
    postTown = town,
    county = county,
    country = country,
    postcode = postcode,
  )
}