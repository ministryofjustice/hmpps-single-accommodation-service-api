package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun buildDomainData(
  crn: String = "CR12345N",
  tier: TierScore = TierScore.A1,
  sex: Sex = Sex(code = SexCode.M, description = "Male"),
  releaseDate: OffsetDateTime = LocalDate.now()
    .plusMonths(6)
    .atStartOfDay()
    .atOffset(ZoneOffset.UTC),
  cas1Application: Cas1Application? = buildCas1Application(),
) = DomainData(
  crn = crn,
  tier = tier,
  sex = sex,
  releaseDate = releaseDate,
  cas1Application = cas1Application,
)
