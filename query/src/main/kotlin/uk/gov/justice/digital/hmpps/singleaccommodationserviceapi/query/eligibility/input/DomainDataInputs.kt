package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.RoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import java.time.LocalDate

interface IdentifiedData {
  val crn: String
}

interface TierData {
  val tierScore: TierScore?
}

interface DateOfBirthData {
  val dob: LocalDate?
}

interface SexData {
  val sex: SexCode?
}

interface RoshData {
  val roshLevel: RoshLevel?
}

interface CurrentAccommodationData {
  val currentAccommodation: AccommodationSummaryDto?
}

interface AccommodationTypeData {
  val currentAccommodationTypeEntity: AccommodationTypeEntity?
}

interface AccommodationData :
  CurrentAccommodationData,
  AccommodationTypeData

interface NextAccommodationData {
  val nextAccommodations: List<AccommodationSummaryDto>
}

interface Cas1ApplicationData {
  val cas1Application: Cas1ApplicationDomainData?
}

interface Cas2ApplicationData : IdentifiedData {
  val cas2Application: Cas2Application?
}

interface Cas3ApplicationData {
  val cas3Application: Cas3Application?
}

interface DtrReferralData {
  val dutyToRefer: DutyToReferDto?
}

interface CrsReferralData {
  val commissionedRehabilitativeServices: CommissionedRehabilitativeServices?
}

interface SexualOffenceData {
  val sexualOffenceRegistrations: SexualOffenceRegistrations?
}

interface Cas2PersonData :
  DateOfBirthData,
  TierData,
  SexualOffenceData

interface CrsRequirementData :
  SexData,
  CrsReferralData

interface Cas3PrerequisiteData :
  DtrReferralData,
  CrsRequirementData

interface Cas1AndCas3ReferralData :
  Cas1ApplicationData,
  Cas3ApplicationData

interface SexAndAccommodationData :
  SexData,
  CurrentAccommodationData
