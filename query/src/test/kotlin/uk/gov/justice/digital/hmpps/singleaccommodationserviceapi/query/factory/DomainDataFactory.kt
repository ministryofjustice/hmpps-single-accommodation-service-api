package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.LocalDate

fun buildDomainData(
  crn: String = "CR12345N",
  tier: TierScore = TierScore.A1,
  sex: SexCode = SexCode.M,
  releaseDate: LocalDate? = LocalDate.now()
    .plusYears(1),
  cas1Application: Cas1Application? = buildCas1Application(),
) = DomainData(
  crn = crn,
  tier = tier,
  sex = sex,
  releaseDate = releaseDate,
  cas1Application = cas1Application,
)
