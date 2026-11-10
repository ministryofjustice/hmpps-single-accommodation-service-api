package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ExternalRequestForPlacementOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.RoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import java.time.LocalDate

data class DomainData(
  override val crn: String,
  override val tierScore: TierScore?,
  override val dob: LocalDate?,
  override val sex: SexCode?,
  override val roshLevel: RoshLevel?,
  override val currentAccommodation: AccommodationSummaryDto?,
  override val currentAccommodationTypeEntity: AccommodationTypeEntity?,
  override val nextAccommodations: List<AccommodationSummaryDto>,
  override val cas1Application: Cas1ApplicationDomainData?,
  override val cas2Application: Cas2Application?,
  override val cas3Application: Cas3Application?,
  override val dutyToRefer: DutyToReferDto?,
  override val commissionedRehabilitativeServices: CommissionedRehabilitativeServices?,
  override val sexualOffenceRegistrations: SexualOffenceRegistrations?,
) : IdentifiedData,
  Cas2PersonData,
  RoshData,
  SexAndAccommodationData,
  AccommodationData,
  NextAccommodationData,
  Cas1AndCas3ReferralData,
  Cas2ApplicationData,
  Cas3PrerequisiteData {

  fun toCas1DomainData() = Cas1DomainData(
    crn = crn,
    tierScore = tierScore,
    sex = sex,
    roshLevel = roshLevel,
    currentAccommodation = currentAccommodation,
    currentAccommodationTypeEntity = currentAccommodationTypeEntity,
    cas1Application = cas1Application,
  )

  fun toCas2DomainData() = Cas2DomainData(
    crn = crn,
    tierScore = tierScore,
    dob = dob,
    roshLevel = roshLevel,
    currentAccommodation = currentAccommodation,
    currentAccommodationTypeEntity = currentAccommodationTypeEntity,
    nextAccommodations = nextAccommodations,
    cas2Application = cas2Application,
    sexualOffenceRegistrations = sexualOffenceRegistrations,
  )

  fun toCas3DomainData() = Cas3DomainData(
    crn = crn,
    sex = sex,
    roshLevel = roshLevel,
    currentAccommodation = currentAccommodation,
    currentAccommodationTypeEntity = currentAccommodationTypeEntity,
    nextAccommodations = nextAccommodations,
    cas3Application = cas3Application,
    dutyToRefer = dutyToRefer,
    commissionedRehabilitativeServices = commissionedRehabilitativeServices,
  )

  fun toCrsDomainData() = CrsDomainData(
    crn = crn,
    sex = sex,
    currentAccommodation = currentAccommodation,
    currentAccommodationTypeEntity = currentAccommodationTypeEntity,
    nextAccommodations = nextAccommodations,
    commissionedRehabilitativeServices = commissionedRehabilitativeServices,
  )

  fun toDtrDomainData() = DtrDomainData(
    crn = crn,
    currentAccommodation = currentAccommodation,
    nextAccommodations = nextAccommodations,
    dutyToRefer = dutyToRefer,
  )

  fun toPaDomainData() = PaDomainData(
    crn = crn,
    nextAccommodations = nextAccommodations,
    cas1Application = cas1Application,
    cas3Application = cas3Application,
  )
}

data class Cas1DomainData(
  override val crn: String,
  override val tierScore: TierScore?,
  override val sex: SexCode?,
  override val roshLevel: RoshLevel?,
  override val currentAccommodation: AccommodationSummaryDto?,
  override val currentAccommodationTypeEntity: AccommodationTypeEntity?,
  override val cas1Application: Cas1ApplicationDomainData?,
) : IdentifiedData,
  TierData,
  SexData,
  RoshData,
  AccommodationData,
  Cas1ApplicationData

data class Cas2DomainData(
  override val crn: String,
  override val tierScore: TierScore?,
  override val dob: LocalDate?,
  override val roshLevel: RoshLevel?,
  override val currentAccommodation: AccommodationSummaryDto?,
  override val currentAccommodationTypeEntity: AccommodationTypeEntity?,
  override val nextAccommodations: List<AccommodationSummaryDto>,
  override val cas2Application: Cas2Application?,
  override val sexualOffenceRegistrations: SexualOffenceRegistrations?,
) : Cas2ApplicationData,
  Cas2PersonData,
  RoshData,
  AccommodationData,
  NextAccommodationData

data class Cas3DomainData(
  override val crn: String,
  override val sex: SexCode?,
  override val roshLevel: RoshLevel?,
  override val currentAccommodation: AccommodationSummaryDto?,
  override val currentAccommodationTypeEntity: AccommodationTypeEntity?,
  override val nextAccommodations: List<AccommodationSummaryDto>,
  override val cas3Application: Cas3Application?,
  override val dutyToRefer: DutyToReferDto?,
  override val commissionedRehabilitativeServices: CommissionedRehabilitativeServices?,
) : IdentifiedData,
  RoshData,
  AccommodationData,
  NextAccommodationData,
  Cas3ApplicationData,
  Cas3PrerequisiteData

data class CrsDomainData(
  override val crn: String,
  override val sex: SexCode?,
  override val currentAccommodation: AccommodationSummaryDto?,
  override val currentAccommodationTypeEntity: AccommodationTypeEntity?,
  override val nextAccommodations: List<AccommodationSummaryDto>,
  override val commissionedRehabilitativeServices: CommissionedRehabilitativeServices?,
) : IdentifiedData,
  SexAndAccommodationData,
  AccommodationTypeData,
  NextAccommodationData,
  CrsReferralData

data class DtrDomainData(
  override val crn: String,
  override val currentAccommodation: AccommodationSummaryDto?,
  override val nextAccommodations: List<AccommodationSummaryDto>,
  override val dutyToRefer: DutyToReferDto?,
) : IdentifiedData,
  CurrentAccommodationData,
  NextAccommodationData,
  DtrReferralData

data class PaDomainData(
  override val crn: String,
  override val nextAccommodations: List<AccommodationSummaryDto>,
  override val cas1Application: Cas1ApplicationDomainData?,
  override val cas3Application: Cas3Application?,
) : IdentifiedData,
  NextAccommodationData,
  Cas1AndCas3ReferralData

data class Cas1ApplicationDomainData(
  val application: Cas1ApplicationV2,
  val currentOutcome: Cas1ExternalRequestForPlacementOutcome?,
  val historyExcludingCurrentOutcome: List<Cas1ExternalRequestForPlacementOutcome>,
)
