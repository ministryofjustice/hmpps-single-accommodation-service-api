package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factory

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.LocalDate

fun buildDomainData(
  crn: String = "CR12345N",
  tier: TierScore = TierScore.A1,
  sex: SexCode = SexCode.M,
  releaseDate: LocalDate? = LocalDate.now()
    .plusYears(1),
  currentAccommodation: AccommodationDetail? = null,
  nextAccommodation: AccommodationDetail? = null,
  cas1Application: Cas1Application? = buildCas1Application(),
  cas3Application: Cas3Application? = null,
) = DomainData(
  crn = crn,
  tier = tier,
  sex = sex,
  releaseDate = releaseDate,
  currentAccommodation = currentAccommodation,
  nextAccommodation = nextAccommodation,
  cas1Application = cas1Application,
  cas3Application = cas3Application,
)
