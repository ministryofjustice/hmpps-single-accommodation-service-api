package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.LocalDate

fun buildDomainData(
  crn: String = "CR12345N",
  tierScore: TierScore? = TierScore.A1,
  sex: SexCode? = SexCode.M,
  releaseDate: LocalDate? = LocalDate.now()
    .plusYears(1),
  currentAccommodationArrangementType: AccommodationArrangementType? = AccommodationArrangementType.NO_FIXED_ABODE,
  hasNextAccommodation: Boolean = false,
  cas1Application: Cas1Application? = buildCas1Application(),
  cas3Application: Cas3Application? = null,
  dtrStatus: String? = "submitted",
  crsStatus: String? = "submitted",
) = DomainData(
  crn = crn,
  tierScore = tierScore,
  sex = sex,
  releaseDate = releaseDate,
  currentAccommodationArrangementType = currentAccommodationArrangementType,
  hasNextAccommodation = hasNextAccommodation,
  cas1Application = cas1Application,
  cas3Application = cas3Application,
  dtrStatus = dtrStatus,
  crsStatus = crsStatus,
)
