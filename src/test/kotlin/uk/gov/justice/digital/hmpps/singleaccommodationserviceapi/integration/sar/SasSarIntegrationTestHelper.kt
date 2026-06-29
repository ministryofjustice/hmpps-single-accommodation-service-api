package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.USERNAME_OF_LOGGED_IN_DELIUS_USER
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SubjectAccessRequestResponse
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.LocalDate
import java.util.Optional
import kotlin.jvm.java

@Component
open class SasSarIntegrationTestHelper(
  val jwtAuthHelper: JwtAuthorisationHelper,
  val objectMapper: ObjectMapper = JsonMapper.builder().configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
    .build(),
) {

  fun requestSarData(prn: String?, crn: String?, fromDate: LocalDate?, toDate: LocalDate?, webTestClient: WebTestClient): SubjectAccessRequestResponse {
    val response = webTestClient.get().uri {
      it.path("/subject-access-request")
        .queryParamIfPresent("prn", Optional.ofNullable(prn))
        .queryParamIfPresent("crn", Optional.ofNullable(crn))
        .queryParamIfPresent("fromDate", Optional.ofNullable(fromDate))
        .queryParamIfPresent("toDate", Optional.ofNullable(toDate))
        .build()
    }
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java)
      .returnResult().responseBody!!
    return objectMapper.readValue(response, SubjectAccessRequestResponse::class.java)
  }

  fun requestSarTemplate(webTestClient: WebTestClient): String = webTestClient
    .get().uri {
      it.path("/subject-access-request/template")
        .build()
    }
    .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
    .exchange()
    .expectStatus().isOk
    .expectBody(String::class.java)
    .returnResult().responseBody!!

  internal fun setAuthorisation(
    username: String? = USERNAME_OF_LOGGED_IN_DELIUS_USER,
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles, authSource = AuthSource.DELIUS)
}
