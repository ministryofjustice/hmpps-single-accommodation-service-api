package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.io.File

class ResourceSecurityIT : IntegrationTestBase() {
  @Autowired
  private lateinit var context: ApplicationContext

  private val unprotectedDefaultMethods = setOf(
    "GET /v3/api-docs.yaml",
    "GET /swagger-ui.html",
    "GET /v3/api-docs",
    "GET /v3/api-docs/swagger-config",
    " /error",
    "PUT /queue-admin/retry-all-dlqs",
    "GET /subject-access-request", // Ignore SAR as well.
    "GET /subject-access-request/template",
  )

  private val unprotectedDefaultClasses =
    setOf("HmppsQueueResource", "BasicErrorController", "OpenApiWebMvcResource")

  @Test
  fun `@PreAuthorise smoke test()`() {
    restTestClient
      .get()
      .uri("/case-list")
      .withClientCredentialsJwt(roles = emptyList())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `Ensure all endpoints protected with PreAuthorize`() {
    // need to exclude any that are forbidden in helm configuration
    val exclusions = File("helm_deploy").walk().filter { it.name.equals("values.yaml") }.flatMap { file ->
      file.readLines().map { line ->
        line.takeIf { it.contains("location") }?.substringAfter("location ")?.substringBefore(" {")
      }
    }.filterNotNull().flatMap { path -> listOf("GET", "POST", "PUT", "DELETE").map { "$it $path" } }
      .toMutableSet().also {
        it.addAll(unprotectedDefaultMethods)
      }

    val beans = context.getBeansOfType(RequestMappingHandlerMapping::class.java)
    beans.forEach { (_, mapping) ->
      mapping.handlerMethods.forEach { (mappingInfo, method) ->
        val classAnnotation = method.beanType.getAnnotation(PreAuthorize::class.java)
        val functionAnnotation = method.getMethodAnnotation(PreAuthorize::class.java)
        val mappings = mappingInfo.getMappings()

        if (classAnnotation == null && functionAnnotation == null) {
          mappings.forEach {
            assertThat(exclusions.contains(it)).withFailMessage {
              "Found $mappingInfo of type $method with no PreAuthorize annotation"
            }.isTrue()

            // if excluded, continue to next endpoint without further validation
            return@forEach
          }
        }
        validateRoles(
          controllerName = method.beanType.simpleName,
          mappings = mappings,
          functionAnnotation = functionAnnotation,
        )
      }
    }
  }

  private fun validateRoles(
    controllerName: String,
    mappings: List<String>,
    functionAnnotation: PreAuthorize?,
  ) {
    if (mappings.size != 1) error("There should be exactly 1 URL mapping")

    val url = mappings.first()

    if (url in unprotectedDefaultMethods || controllerName in unprotectedDefaultClasses) {
      return
    }

    requireNotNull(functionAnnotation) { "@PreAuthorize annotation missing: $controllerName:$url" }

    val functionRoles = extractRoles(functionAnnotation.value)
    val controllerMappings = controllerMap[controllerName] ?: error("Controller mapping not found for: $controllerName")
    val expectedRoles = controllerMappings[url] ?: error("Role mapping not found for: $url")
    assertThat(functionRoles).isEqualTo(expectedRoles)
  }

  private fun extractRoles(roleString: String): Set<String> {
    val regex = "'([^']*)'".toRegex()

    return regex.findAll(roleString)
      .map { it.groupValues[1] }
      .map { it.removePrefix("ROLE_") }
      .toSet()
  }

  private fun RequestMappingInfo.getMappings() = methodsCondition.methods
    .map { it.name }
    .ifEmpty { listOf("") } // if no methods defined then match all rather than none
    .flatMap { method ->
      pathPatternsCondition?.patternValues?.map { "$method $it" } ?: emptyList()
    }
}

private val defaultRoles = setOf("SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER")

private val userControllerMap: Map<String, Set<String>> = mapOf("GET /user/teams" to defaultRoles)

private val dutyToReferControllerMap: Map<String, Set<String>> =
  mapOf(
    "GET /cases/{crn}/dtr/{id}" to defaultRoles,
    "GET /cases/{crn}/dtr/{id}/timeline" to defaultRoles,
    "GET /duty-to-refers/{id}" to setOf("SINGLE_ACCOMMODATION_SERVICE__ACCOMMODATION_DATA_DOMAIN"),
    "GET /cases/{crn}/dtr" to defaultRoles,
    "PUT /cases/{crn}/dtr/{id}" to defaultRoles,
    "POST /cases/{crn}/dtr" to defaultRoles,
    "POST /cases/{crn}/dtr/{id}/notes" to defaultRoles,
  )

private val eligibilityControllerMap: Map<String, Set<String>> =
  mapOf("GET /cases/{crn}/eligibility" to defaultRoles)

private val proposedAccommodationControllerMap: Map<String, Set<String>> =
  mapOf(
    "GET /cases/{crn}/proposed-accommodations/{id}" to defaultRoles,
    "GET /cases/{crn}/proposed-accommodations" to defaultRoles,
    "GET /cases/{crn}/proposed-accommodations/{id}/timeline" to defaultRoles,
    "POST /cases/{crn}/proposed-accommodations" to defaultRoles,
    "PUT /cases/{crn}/proposed-accommodations/{id}" to defaultRoles,
    "POST /cases/{crn}/proposed-accommodations/{id}/notes" to defaultRoles,
    "POST /cases/{crn}/proposed-accommodations/{id}/arrival" to defaultRoles,
  )

private val accommodationControllerMap: Map<String, Set<String>> =
  mapOf(
    "GET /cases/{crn}/accommodations/current" to defaultRoles,
    "GET /cases/{crn}/accommodations/next" to defaultRoles,
    "GET /accommodations/{id}" to setOf("SINGLE_ACCOMMODATION_SERVICE__CORE_PERSON_RECORD"),
  )

private val accommodationReferralControllerMap: Map<String, Set<String>> =
  mapOf("GET /cases/{crn}/applications" to defaultRoles)

private val accommodationHistoryControllerMap: Map<String, Set<String>> =
  mapOf(
    "GET /cases/{crn}/accommodation-history" to defaultRoles,
    "GET /v2/cases/{crn}/accommodation-history" to defaultRoles,
  )

private val caseControllerMap: Map<String, Set<String>> =
  mapOf(
    "GET /case-list" to defaultRoles,
    "GET /cases/{crn}" to defaultRoles,
  )

private val accommodationDataDomainControllerMap: Map<String, Set<String>> =
  mapOf(
    "GET /accommodation-data-domain/health" to setOf(
      "SINGLE_ACCOMMODATION_SERVICE_PROBATION_PRACTITIONER",
      "ACCOMMODATION_DATA_DOMAIN__SINGLE_ACCOMMODATION_SERVICE",
    ),
  )

private val referenceDataControllerMap: Map<String, Set<String>> =
  mapOf(
    "GET /reference-data" to defaultRoles,
  )

private val controllerMap: Map<String, Map<String, Set<String>>> =
  mapOf(
    "UserController" to userControllerMap,
    "DutyToReferController" to dutyToReferControllerMap,
    "EligibilityController" to eligibilityControllerMap,
    "ProposedAccommodationController" to proposedAccommodationControllerMap,
    "AccommodationController" to accommodationControllerMap,
    "AccommodationReferralController" to accommodationReferralControllerMap,
    "AccommodationHistoryController" to accommodationHistoryControllerMap,
    "CaseController" to caseControllerMap,
    "AccommodationDataDomainController" to accommodationDataDomainControllerMap,
    "ReferenceDataController" to referenceDataControllerMap,
  )
