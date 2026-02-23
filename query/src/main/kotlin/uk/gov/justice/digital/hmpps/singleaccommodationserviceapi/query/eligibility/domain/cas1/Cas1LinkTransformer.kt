package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData

object Cas1LinkTransformer {
  fun buildCas1Link(data: DomainData, isWithinOneYear: Boolean) =
    if (isWithinOneYear) {
      when (data.cas1Application?.applicationStatus) {
        null,
          -> "Start application"

        else -> null
      }
    } else {
      null
    }
}
