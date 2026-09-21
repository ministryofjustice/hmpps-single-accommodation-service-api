package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.time.LocalDate
import java.util.UUID

@ExtendWith(PactConsumerTestExt::class)
class CorePersonRecordClientPactTest {
  private val crn = "X123456"
  @Pact(consumer = "hmpps-single-accommodation-service-api", provider = "hmpps-person-record")
  fun getProbationPerson(builder: PactDslWithProvider): RequestResponsePact =
    builder
      .given("A probation person exists for the requested CRN")
      .uponReceiving("a request for a probation person by CRN")
      .pathFromProviderState("/person/probation/\${crn}","/person/probation/$crn")
      .method("GET")
      .willRespondWith()
      .status(200)
      .headers(mapOf("Content-Type" to "application/json"))
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
              address.stringType("cprAddressId", "123e4567-e89b-12d3-a456-426614174001")
              address.booleanType("noFixedAbode", false)
              address.date("startDate", "yyyy-MM-dd", LocalDate.of(2025, 1, 1))
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

    val person = client.getByCrn("X123456")

    assertEquals("John", person.firstName)
    assertEquals("Smith", person.lastName)
    assertEquals(LocalDate.of(1980, 1, 1), person.dateOfBirth)
    assertEquals("PNC123", person.identifiers?.pncs?.firstOrNull())
    assertEquals(1, person.addresses.size)
  }
}