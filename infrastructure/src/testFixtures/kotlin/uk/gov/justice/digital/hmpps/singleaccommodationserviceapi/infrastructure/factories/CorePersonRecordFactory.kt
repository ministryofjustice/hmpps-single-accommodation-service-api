package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Identifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
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
  addresses: List<CanonicalAddress> = listOf(buildAddress()),
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
fun buildAddress(
  cprAddressId: UUID? = UUID.randomUUID(),
  noFixedAbode: Boolean? = false,
  startDate: LocalDate? = LocalDate.of(2025, 10, 17),
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
  status: CanonicalAddressStatus = CanonicalAddressStatus(
    code = null,
  ),
  usage: CanonicalAddressUsage = CanonicalAddressUsage(
    usageCode = CanonicalAddressUsageCode(
      code = null,
    ),
    isActive = true,
  ),
  uprn: String? = null,
) = CanonicalAddress(
  cprAddressId = cprAddressId.toString(),
  noFixedAbode = noFixedAbode,
  startDate = startDate?.toString(),
  endDate = endDate?.toString(),
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
  status = status,
  usages = listOf(usage),
  uprn = uprn,
)

fun buildSex(
  code: SexCode? = SexCode.M,
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
