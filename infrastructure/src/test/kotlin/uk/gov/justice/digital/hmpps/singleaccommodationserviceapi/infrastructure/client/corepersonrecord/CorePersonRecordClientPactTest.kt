package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
import au.com.dius.pact.consumer.dsl.LambdaDslObject
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressContact
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.ContactType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.ProbationCreateAddress
import java.net.URI
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

@ExtendWith(PactConsumerTestExt::class)
class CorePersonRecordClientPactTest {
  private val crn = "X123456"
  private val cprAddressId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001")
  private val startDate = LocalDate.of(2025, 1, 1)
  private val endDate = LocalDate.of(2025, 12, 31)
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
  private val jsonHeaders = mapOf("Content-Type" to "application/json")

  @Pact(consumer = "hmpps-single-accommodation-service-api", provider = "hmpps-person-record")
  fun getProbationPerson(builder: PactDslWithProvider): RequestResponsePact = builder
    .given("A probation person exists for the requested CRN")
    .uponReceiving("a request for a probation person by CRN")
    .pathFromProviderState("/person/probation/\${crn}", "/person/probation/$crn")
    .method("GET")
    .willRespondWith()
    .status(200)
    .headers(jsonHeaders)
    .body(
      newJsonBody { body ->
        body.stringType("firstName", "John")
        body.stringType("lastName", "Smith")
        body.date(
          "dateOfBirth",
          "yyyy-MM-dd",
          LocalDate.of(1980, 1, 1),
        )
        body.`object`("identifiers") { identifiers ->
          identifiers.array("pncs") { pncs ->
            pncs.stringType("PNC123")
          }
        }
        body.array("addresses") { addresses ->
          addresses.`object` { address ->
            address.uuid("cprAddressId", cprAddressId)
            address.booleanType("noFixedAbode", false)
            address.date("startDate", "yyyy-MM-dd", startDate)
            address.stringType("postcode", "SW1A 1AA")
            address.stringType("buildingNumber", "1")
            address.stringType("thoroughfareName", "Some Street")
            address.stringType("postTown", "London")
            address.`object`("status") { status ->
              status.stringType("code", "M")
              status.stringType("description", "Main")
            }
            address.booleanType("typeVerified", false)
            address.array("usages") { usages ->
              usages.`object` { usage ->
                usage.stringType("code", "A01A")
                usage.stringType("description", "Main residence")
                usage.booleanType("isActive", true)
              }
            }
          }
        }
      }.build(),
    )
    .toPact()

  @Pact(consumer = "hmpps-single-accommodation-service-api", provider = "hmpps-person-record")
  fun getProbationAddress(builder: PactDslWithProvider): RequestResponsePact = builder
    .given("An address exists for the requested CRN and address ID")
    .uponReceiving("a request for a probation address by CRN and CPR address ID")
    .pathFromProviderState(
      "/person/probation/\${crn}/address/\${cprAddressId}",
      "/person/probation/$crn/address/$cprAddressId",
    )
    .method("GET")
    .willRespondWith()
    .status(200)
    .headers(jsonHeaders)
    .body(
      newJsonBody { address ->
        address.uuid("cprAddressId", cprAddressId)
        address.booleanType("typeVerified", true)
        address.booleanType("noFixedAbode", false)
        address.date("startDate", "yyyy-MM-dd", startDate)
        address.date("endDate", "yyyy-MM-dd", endDate)
        addAddressLocationMatchers(address)
        address.`object`("status") { status ->
          status.stringType("code", "PR")
          status.stringType("description", "Proposed")
        }
        address.array("usages") { usages ->
          usages.`object` { usage ->
            usage.stringType("code", "A01A")
            usage.stringType("description", "Main residence")
            usage.booleanType("isActive", true)
          }
        }
      }.build(),
    )
    .toPact()

  @Pact(consumer = "hmpps-single-accommodation-service-api", provider = "hmpps-person-record")
  fun createProbationAddress(builder: PactDslWithProvider): RequestResponsePact = builder
    .given("A probation address can be created for the requested CRN")
    .uponReceiving("a request to create a probation address for a CRN")
    .pathFromProviderState(
      "/person/probation/\${crn}/address",
      "/person/probation/$crn/address",
    )
    .method("POST")
    .headers(jsonHeaders)
    .body(
      newJsonBody { address ->
        address.booleanType("noFixedAbode", false)
        address.booleanType("typeVerified", false)
        address.stringMatcher(
          "startDate",
          "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
          startDate.atStartOfDay(ZoneOffset.UTC).format(dateTimeFormatter),
        )
        address.stringMatcher(
          "endDate",
          "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z",
          endDate.atStartOfDay(ZoneOffset.UTC).format(dateTimeFormatter),
        )
        addAddressLocationMatchers(address)
        address.stringType("comment", "Created by SAS")
        address.stringType("statusCode", "PR")
        address.array("usages") { usages ->
          usages.`object` { usage ->
            usage.stringType("usageCode", "A01A")
            usage.booleanType("isActive", true)
          }
        }
        address.array("contacts") { contacts ->
          contacts.`object` { contact ->
            contact.stringType("typeCode", "MOBILE")
            contact.stringType("value", "07123456789")
            contact.stringType("extension", "123")
          }
        }
      }.build(),
    )
    .willRespondWith()
    .status(201)
    .headers(jsonHeaders)
    .body(
      newJsonBody { response ->
        response.stringType("crn", crn)
        response.uuid("cprAddressId", cprAddressId)
      }.build(),
    )
    .toPact()

  private fun addAddressLocationMatchers(address: LambdaDslObject) {
    address.stringType("postcode", "SW1A 1AA")
    address.stringType("uprn", "100023336956")
    address.stringType("subBuildingName", "Flat 2")
    address.stringType("buildingName", "Example House")
    address.stringType("buildingNumber", "1")
    address.stringType("thoroughfareName", "Some Street")
    address.stringType("dependentLocality", "Westminster")
    address.stringType("postTown", "London")
    address.stringType("county", "Greater London")
  }

  private fun probationCreateAddress() = ProbationCreateAddress(
    noFixedAbode = false,
    typeVerified = false,
    startDate = startDate.atStartOfDay(ZoneOffset.UTC),
    endDate = endDate.atStartOfDay(ZoneOffset.UTC),
    postcode = "SW1A 1AA",
    uprn = "100023336956",
    subBuildingName = "Flat 2",
    buildingName = "Example House",
    buildingNumber = "1",
    thoroughfareName = "Some Street",
    dependentLocality = "Westminster",
    postTown = "London",
    county = "Greater London",
    comment = "Created by SAS",
    statusCode = AddressStatusCode.PR,
    usages = listOf(
      AddressUsage(
        usageCode = AddressUsageCode.A01A,
        isActive = true,
      ),
    ),
    contacts = listOf(
      AddressContact(
        typeCode = ContactType.MOBILE,
        value = "07123456789",
        extension = "123",
      ),
    ),
  )

  private fun createCorePersonRecordClient(baseUrl: String): CorePersonRecordClient {
    val restClient = RestClient.builder()
      .baseUrl(baseUrl)
      .build()

    val proxyFactory = HttpServiceProxyFactory
      .builderFor(RestClientAdapter.create(restClient))
      .build()

    return proxyFactory.createClient(CorePersonRecordClient::class.java)
  }

  @Test
  @PactTestFor(
    pactMethod = "getProbationPerson",
    pactVersion = PactSpecVersion.V3,
  )
  fun `get probation person by CRN`(mockServer: MockServer) {
    val client = createCorePersonRecordClient(mockServer.getUrl())

    val person = client.getByCrn(crn)

    assertEquals("John", person.firstName)
    assertEquals("Smith", person.lastName)
    assertEquals(LocalDate.of(1980, 1, 1), person.dateOfBirth)
    assertEquals("PNC123", person.identifiers?.pncs?.firstOrNull())
    assertEquals(1, person.addresses.size)
  }

  @Test
  @PactTestFor(
    pactMethod = "getProbationAddress",
    pactVersion = PactSpecVersion.V3,
  )
  fun `get probation address by CRN and CPR address ID`(mockServer: MockServer) {
    val client = createCorePersonRecordClient(mockServer.getUrl())

    val address = client.getProbationAddress(
      mockServer.getUrl().let { URI.create("$it/person/probation/$crn/address/$cprAddressId") },
    )
    val usage = address.usages.single()

    assertAll(
      { assertEquals(cprAddressId.toString(), address.cprAddressId) },
      { assertEquals(true, address.typeVerified) },
      { assertEquals(false, address.noFixedAbode) },
      { assertEquals(startDate.toString(), address.startDate) },
      { assertEquals(endDate.toString(), address.endDate) },
      { assertEquals("SW1A 1AA", address.postcode) },
      { assertEquals("Flat 2", address.subBuildingName) },
      { assertEquals("Example House", address.buildingName) },
      { assertEquals("1", address.buildingNumber) },
      { assertEquals("Some Street", address.thoroughfareName) },
      { assertEquals("Westminster", address.dependentLocality) },
      { assertEquals("London", address.postTown) },
      { assertEquals("Greater London", address.county) },
      { assertEquals("100023336956", address.uprn) },
      { assertEquals("PR", address.status.code) },
      { assertEquals("Proposed", address.status.description) },
      { assertEquals("A01A", usage.usageCode.code) },
      { assertEquals("Main residence", usage.usageCode.description) },
      { assertEquals(true, usage.isActive) },
    )
  }

  @Test
  @PactTestFor(
    pactMethod = "createProbationAddress",
    pactVersion = PactSpecVersion.V3,
  )
  fun `create probation address for CRN`(mockServer: MockServer) {
    val client = createCorePersonRecordClient(mockServer.getUrl())

    val response = client.createProbationAddress(crn, probationCreateAddress())

    assertEquals(crn, response.crn)
    assertEquals(cprAddressId, response.cprAddressId)
  }
}
