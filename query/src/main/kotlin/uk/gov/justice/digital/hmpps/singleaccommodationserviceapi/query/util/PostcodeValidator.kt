package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.util

object PostcodeValidator {
  private val regex = (
    "^(GIR 0AA|(?:(?:[A-PR-UWYZ][0-9]{1,2}|" +
      "[A-PR-UWYZ][A-HK-Y][0-9]{1,2}|" +
      "[A-PR-UWYZ][0-9][A-HJKPSTUW]?|" +
      "[A-PR-UWYZ][A-HK-Y][0-9][ABEHMNPRVWXY]?)) ?[0-9][ABD-HJLNP-UW-Z]{2})$"
    ).toRegex(RegexOption.IGNORE_CASE)

  fun normalise(input: String) = input.trim().replace(Regex("\\s+"), " ").uppercase()

  fun isValid(input: String) = regex.matches(normalise(input))
}
