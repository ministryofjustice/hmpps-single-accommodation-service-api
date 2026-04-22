package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.CurrentAccommodation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.LocalDate

fun buildDomainData(
  crn: String = "CR12345N",
  tierScore: TierScore? = TierScore.A1,
  sex: SexCode? = SexCode.M,
  currentAccommodation: CurrentAccommodation? = buildCurrentAccommodation(),
  hasNextAccommodation: Boolean = false,
  cas1Application: Cas1Application? = buildCas1Application(),
  cas3Application: Cas3Application? = null,
  dtrStatus: String? = "submitted",
  crsStatus: String? = "submitted",
) = DomainData(
  crn = crn,
  tierScore = tierScore,
  sex = sex,
  currentAccommodation = currentAccommodation,
  hasNextAccommodation = hasNextAccommodation,
  cas1Application = cas1Application,
  cas3Application = cas3Application,
  dtrStatus = dtrStatus,
  crsStatus = crsStatus,
)

fun buildCurrentAccommodation(
  endDate: LocalDate? = LocalDate.now().plusDays(1),
  isPrisonCas1Cas2orCas2v2: Boolean = true,
) = CurrentAccommodation(
  endDate = endDate,
  isPrisonCas1Cas2OrCas2v2 = isPrisonCas1Cas2orCas2v2,
)
