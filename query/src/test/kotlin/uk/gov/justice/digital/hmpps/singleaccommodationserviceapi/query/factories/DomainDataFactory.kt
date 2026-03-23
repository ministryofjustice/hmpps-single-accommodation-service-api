package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationArrangementType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2CourtBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2HdcApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2PrisonBailApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DomainData
import java.time.LocalDate
import java.util.UUID

fun buildDomainData(
  crn: String = "CR12345N",
  tier: TierScore = TierScore.A1,
  sex: SexCode = SexCode.M,
  releaseDate: LocalDate? = LocalDate.now()
    .plusYears(1),
  currentAccommodationArrangementType: AccommodationArrangementType? = null,
  nextAccommodationId: UUID? = null,
  cas1Application: Cas1Application? = buildCas1Application(),
  cas2CourtBailApplication: Cas2CourtBailApplication? = null,
  cas2PrisonBailApplication: Cas2PrisonBailApplication? = null,
  cas2HdcApplication: Cas2HdcApplication? = null,
  cas3Application: Cas3Application? = null,
  dtrStatus: String? = null,
  crsStatus: String? = null,
) = DomainData(
  crn = crn,
  tier = tier,
  sex = sex,
  releaseDate = releaseDate,
  currentAccommodationArrangementType = currentAccommodationArrangementType,
  nextAccommodationId = nextAccommodationId,
  cas1Application = cas1Application,
  cas2CourtBailApplication = cas2CourtBailApplication,
  cas2PrisonBailApplication = cas2PrisonBailApplication,
  cas2HdcApplication = cas2HdcApplication,
  cas3Application = cas3Application,
  dtrStatus = dtrStatus,
  crsStatus = crsStatus,
)
