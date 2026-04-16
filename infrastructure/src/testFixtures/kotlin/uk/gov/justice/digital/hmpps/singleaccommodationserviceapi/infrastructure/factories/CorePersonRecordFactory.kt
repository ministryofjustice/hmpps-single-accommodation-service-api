package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Address
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecordAddresses
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.TestData
import java.time.LocalDate
import java.util.UUID

@TestData
fun buildCorePersonRecord(
  cprUUID: UUID = UUID.randomUUID(),
  identifiers: Identifiers? = buildIdentifiers(),
  firstName: String? = "First",
  middleNames: String? = "Middle",
  lastName: String? = "Last",
  dateOfBirth: LocalDate = LocalDate.of(2000, 12, 3),
  sex: Sex = buildSex(),
  addresses: List<Address> = listOf(buildAddress()),
) = CorePersonRecord(
  cprUUID = cprUUID,
  identifiers = identifiers,
  firstName = firstName,
  lastName = lastName,
  middleNames = middleNames,
  dateOfBirth = dateOfBirth,
  sex = sex,
  addresses = addresses,
)

@TestData
fun buildCorePersonRecordAddresses(
  crn: String = "FAKECRN",
  addresses: List<Address> = listOf(buildAddress()),
) = CorePersonRecordAddresses(crn, addresses)

@TestData
fun buildAddress(
  cprAddressId: UUID? = UUID.randomUUID(),
  noFixedAbode: Boolean? = false,
  startDate: LocalDate? = LocalDate.now().minusMonths(6),
  endDate: LocalDate? = null,
  postcode: String? = "SW1A 1AA",
  subBuildingName: String? = null,
  buildingName: String? = null,
  buildingNumber: String? = "1",
  thoroughfareName: String? = "Some Street",
  dependentLocality: String? = null,
  postTown: String? = "London",
  county: String? = null,
  country: String? = null,
  countryCode: String? = null,
  addressStatus: AddressStatus? = null,
  addressUsage: AddressUsage? = null,
  uprn: String? = null,
) = Address(
  cprAddressId = cprAddressId,
  noFixedAbode = noFixedAbode,
  startDate = startDate,
  endDate = endDate,
  postcode = postcode,
  subBuildingName = subBuildingName,
  buildingName = buildingName,
  buildingNumber = buildingNumber,
  thoroughfareName = thoroughfareName,
  dependentLocality = dependentLocality,
  postTown = postTown,
  county = county,
  country = country,
  countryCode = countryCode,
  addressStatus = addressStatus,
  addressUsage = addressUsage,
  uprn = uprn,
)

fun buildAddressUsage(addressUsageCode: AddressUsageCode, addressUsageDescription: String) = AddressUsage(
  addressUsageCode = addressUsageCode,
  addressUsageDescription = addressUsageDescription,
)

fun buildSex(
  code: SexCode? = SexCode.F,
) = Sex(
  code = code,
  description = when (code) {
    SexCode.F -> "Female"
    SexCode.M -> "Male"
    SexCode.N -> "Not Known / Not Recorded"
    SexCode.NS -> "Not Specified"
    null -> throw IllegalArgumentException("Invalid sex code: $code")
  },
)

fun buildIdentifiers(
  crns: List<String> = listOf("XX12345X"),
  prisonNumbers: List<String> = listOf("PRI1"),
  pncs: List<String> = listOf("Some PNC Reference"),
) = Identifiers(crns = crns, prisonNumbers = prisonNumbers, pncs = pncs)
