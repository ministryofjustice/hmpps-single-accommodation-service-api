package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import java.time.OffsetDateTime

data class DomainData(
  val crn: String,
  val tier: String,
  val sex: Sex,
  val releaseDate: OffsetDateTime,
  val cas1Application: Cas1Application? = null,
)
