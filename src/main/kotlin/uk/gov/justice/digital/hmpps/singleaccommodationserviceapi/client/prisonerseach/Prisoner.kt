package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.prisonerseach

import java.time.LocalDate
import java.time.LocalDateTime

data class Prisoner(
  var prisonerNumber: String? = null,
  var pncNumber: String? = null,
  var pncNumberCanonicalShort: String? = null,
  var pncNumberCanonicalLong: String? = null,
  var croNumber: String? = null,
  var bookingId: String? = null,
  var bookNumber: String? = null,
  var title: String? = null,
  var firstName: String? = null,
  var middleNames: String? = null,
  var lastName: String? = null,
  var dateOfBirth: LocalDate? = null,
  var gender: String? = null,
  var ethnicity: String? = null,
  var raceCode: String? = null,
  var youthOffender: Boolean? = null,
  var maritalStatus: String? = null,
  var religion: String? = null,
  var nationality: String? = null,
  var smoker: String? = null,
  var personalCareNeeds: List<PersonalCareNeed>? = null,
  var languages: List<Language>? = null,
  var currentFacialImageId: Long? = null,
  var militaryRecord: Boolean? = null,
  var status: String? = null,
  var lastMovementTypeCode: String? = null,
  var lastMovementReasonCode: String? = null,
  var inOutStatus: String? = null,
  var prisonId: String? = null,
  var prisonName: String? = null,
  var lastPrisonId: String? = null,
  var previousPrisonId: String? = null,
  var previousPrisonLeavingDate: LocalDate? = null,
  var cellLocation: String? = null,
  var aliases: List<PrisonerAlias>? = null,
  var alerts: List<PrisonerAlert>? = null,
  var csra: String? = null,
  var category: String? = null,
  var complexityOfNeedLevel: String? = null,
  var legalStatus: String? = null,
  var imprisonmentStatus: String? = null,
  var imprisonmentStatusDescription: String? = null,
  var convictedStatus: String? = null,
  var mostSeriousOffence: String? = null,
  var recall: Boolean? = null,
  var indeterminateSentence: Boolean? = null,
  var sentenceStartDate: LocalDate? = null,
  var releaseDate: LocalDate? = null,
  var confirmedReleaseDate: LocalDate? = null,
  var sentenceExpiryDate: LocalDate? = null,
  var licenceExpiryDate: LocalDate? = null,
  var homeDetentionCurfewEligibilityDate: LocalDate? = null,
  var homeDetentionCurfewActualDate: LocalDate? = null,
  var homeDetentionCurfewEndDate: LocalDate? = null,
  var topupSupervisionStartDate: LocalDate? = null,
  var topupSupervisionExpiryDate: LocalDate? = null,
  var additionalDaysAwarded: Int? = null,
  var nonDtoReleaseDate: LocalDate? = null,
  var nonDtoReleaseDateType: String? = null,
  var receptionDate: LocalDate? = null,
  var lastAdmissionDate: LocalDate? = null,
  var paroleEligibilityDate: LocalDate? = null,
  var automaticReleaseDate: LocalDate? = null,
  var postRecallReleaseDate: LocalDate? = null,
  var conditionalReleaseDate: LocalDate? = null,
  var nonParoleDate: LocalDate? = null,
  var actualParoleDate: LocalDate? = null,
  var tariffDate: LocalDate? = null,
  var releaseOnTemporaryLicenceDate: LocalDate? = null,
  var locationDescription: String? = null,
  var restrictedPatient: Boolean? = null,
  var supportingPrisonId: String? = null,
  var dischargedHospitalId: String? = null,
  var dischargedHospitalDescription: String? = null,
  var dischargeDate: LocalDate? = null,
  var dischargeDetails: String? = null,
  var currentIncentive: CurrentIncentive? = null,
  var heightCentimetres: Int? = null,
  var weightKilograms: Int? = null,
  var hairColour: String? = null,
  var rightEyeColour: String? = null,
  var leftEyeColour: String? = null,
  var facialHair: String? = null,
  var shapeOfFace: String? = null,
  var build: String? = null,
  var shoeSize: Int? = null,
  var tattoos: List<BodyPartDetail>? = null,
  var scars: List<BodyPartDetail>? = null,
  var marks: List<BodyPartDetail>? = null,
  var addresses: List<Address>? = null,
  var emailAddresses: List<EmailAddress>? = null,
  var phoneNumbers: List<PhoneNumber>? = null,
  var identifiers: List<Identifier>? = null,
  var allConvictedOffences: List<Offence>? = null,
)

data class PersonalCareNeed(
  val problemType: String? = null,
  val problemCode: String? = null,
  val problemStatus: String? = null,
  val problemDescription: String? = null,
  val commentText: String? = null,
  val startDate: LocalDate? = null,
  val endDate: LocalDate? = null,
)

data class Language(
  val type: String? = null,
  val code: String? = null,
  val readSkill: String? = null,
  val writeSkill: String? = null,
  val speakSkill: String? = null,
  val interpreterRequested: Boolean? = null,
)

data class PrisonerAlias(
  var title: String? = null,
  val firstName: String? = null,
  val middleNames: String? = null,
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
  val raceCode: String? = null,
)

data class CurrentIncentive(
  val level: IncentiveLevel? = null,
  val dateTime: LocalDateTime? = null,
  val nextReviewDate: LocalDate? = null,
)

data class IncentiveLevel(
  val code: String? = null,
  val description: String? = null,
)

data class PrisonerAlert(
  val alertType: String? = null,
  val alertCode: String? = null,
  val active: Boolean? = null,
  val expired: Boolean? = null,
)

data class BodyPartDetail(
  var bodyPart: String? = null,
  var comment: String? = null,
)

data class Address(
  val addressId: Long,
  val flat: String?,
  val premise: String?,
  val street: String?,
  val locality: String?,
  val town: String?,
  val postalCode: String?,
  val county: String?,
  val country: String?,
  val primary: Boolean,
  val startDate: LocalDate?,
  val phones: List<Telephone>?,
  val noFixedAddress: Boolean = false,
)

data class Telephone(
  val number: String,
  val type: String,
)

data class EmailAddress(
  val email: String,
)

data class PhoneNumber(
  val type: String? = null,
  val number: String? = null,
)

data class Identifier(
  val type: String? = null,
  val value: String? = null,
  val issuedDate: LocalDate? = null,
  val issuedAuthorityText: String? = null,
  val createdDateTime: LocalDateTime? = null,
)

data class Offence(
  val statuteCode: String? = null,
  val offenceCode: String? = null,
  val offenceDescription: String? = null,
  val offenceDate: LocalDate? = null,
  val latestBooking: Boolean? = null,
  val sentenceStartDate: LocalDate? = null,
  val primarySentence: Boolean? = null,
)
