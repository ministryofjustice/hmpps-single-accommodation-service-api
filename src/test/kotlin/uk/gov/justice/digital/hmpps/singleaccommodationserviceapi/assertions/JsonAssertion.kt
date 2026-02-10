package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.JsonHelper.jsonMapper

class JsonAssertion(actual: String) : AbstractAssert<JsonAssertion, String>(actual, JsonAssertion::class.java) {

  fun matchesExpectedJson(expectedJson: String): JsonAssertion {
    val actualJson = jsonMapper.readTree(actual)
    val expected = jsonMapper.readTree(expectedJson)

    Assertions.assertThat(actualJson).isEqualTo(expected)

    return this
  }
}

fun assertThatJson(actual: String): JsonAssertion = JsonAssertion(actual)
