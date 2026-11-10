package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.RiskLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataField

data class DomainDataFieldInfo(
  val path: String,
  val label: String,
  val options: List<String>,
)

internal object DomainDataFieldCatalog {
  // Probation accommodation codes are A-prefixed; prison is represented separately in SAS.
  private val currentAccommodationTypes = listOf("Not available") + AddressUsageCode.entries
    .filter { it.name.matches(Regex("A\\d+.*")) }
    .sortedBy { it.name }
    .map { "${it.name} — ${it.description}" } + "HMP — Prison"

  private val sexOptions = listOf("Not available") + SexCode.entries.map {
    "${it.name} — ${it.gender}"
  }

  fun fieldsFor(inputs: Set<DomainDataField>): List<DomainDataFieldInfo> = DomainDataField.entries
    // Include availability rows for nullable parents of the declared inputs.
    .filter { field -> inputs.any { it == field || it.path.startsWith("${field.path}.") } }
    .map(::fieldInfo)

  private fun fieldInfo(input: DomainDataField): DomainDataFieldInfo {
    val (label, options) = when (input) {
      DomainDataField.CURRENT_ACCOMMODATION_END_DATE -> "Current accommodation end date" to listOf("Not available", "A date")
      DomainDataField.CURRENT_ACCOMMODATION_TYPE -> "Current accommodation type" to currentAccommodationTypes
      DomainDataField.ACCOMMODATION_SETTLED_TYPE -> "Accommodation settled type" to enumOptions<AccommodationSettledType>()
      DomainDataField.SEX -> "Sex" to sexOptions
      DomainDataField.TIER_SCORE -> "Tier score" to (
        listOf("Not available") + TierScore.entries.map {
          if (it.name.length == 1) it.name else "${it.name} — ${it.name.toReadableWords()}"
        }
        )
      DomainDataField.ROSH_LEVEL -> "RoSH level" to enumOptions<RiskLevel>()
      DomainDataField.NEXT_ACCOMMODATIONS -> "Next accommodation" to listOf("None", "Confirmed")
      DomainDataField.DATE_OF_BIRTH -> "Date of birth" to listOf("Not available", "Before 18th birthday", "On or after 18th birthday")
      DomainDataField.SEXUAL_OFFENCE_REGISTRATIONS -> "Sexual offence registrations" to listOf("Not available or none", "Sexual offence registrations present")
      DomainDataField.CAS1_APPLICATION -> "CAS1 application" to listOf("Not available", "Available")
      DomainDataField.CAS1_APPLICATION_STATUS -> "CAS1 application status" to enumOptions<Cas1ApplicationStatus>()
      DomainDataField.CAS1_CURRENT_OUTCOME -> "Current CAS1 application outcome" to listOf("Not available", "Available")
      DomainDataField.CAS1_PLACEMENT -> "Current placement" to listOf("Not available", "Available")
      DomainDataField.CAS1_PLACEMENT_STATUS -> "Placement status" to enumOptions<Cas1PlacementStatus>()
      DomainDataField.CAS1_REQUEST_FOR_PLACEMENT_STATUS -> "Request for placement status" to enumOptions<Cas1RequestForPlacementStatus>()
      DomainDataField.CAS2_APPLICATION -> "CAS2 application" to listOf("Not available", "Available")
      DomainDataField.CAS2_SUBMITTED_APPLICATION -> "Submitted CAS2 application" to listOf("Not available", "Available")
      DomainDataField.CAS2_SUBMITTED_AT -> "Application submission date" to listOf("Not available", "A timestamp")
      DomainDataField.CAS2_ASSESSMENT_STATUS -> "Latest CAS2 assessment status" to enumOptions<Cas2AssessmentStatus>()
      DomainDataField.CAS2_MARKED_AS_ARRIVED -> "Marked as arrived" to listOf("Not available", "Marked as arrived")
      DomainDataField.CAS3_APPLICATION -> "CAS3 application" to listOf("Not available", "Available")
      DomainDataField.CAS3_APPLICATION_STATUS -> "CAS3 application status" to enumOptions<Cas3ApplicationStatus>()
      DomainDataField.CAS3_ASSESSMENT_STATUS -> "CAS3 assessment status" to enumOptions<Cas3AssessmentStatus>()
      DomainDataField.CAS3_LATEST_BOOKING -> "Latest CAS3 booking" to listOf("Not available", "Available")
      DomainDataField.CAS3_BOOKING_STATUS -> "CAS3 booking status" to enumOptions<Cas3BookingStatus>()
      DomainDataField.DTR_STATUS -> "Duty to Refer status" to enumOptions<DtrStatus>()
      DomainDataField.DTR_SUBMISSION_DATE -> "Duty to Refer submission date" to listOf("Not available", "A date")
      DomainDataField.CRS_STATUS -> "CRS referral status" to enumOptions<CrsReferralStatus>()
    }
    return DomainDataFieldInfo(input.path, label, options)
  }

  private inline fun <reified T : Enum<T>> enumOptions(): List<String> = listOf("Not available") + enumValues<T>().map { it.name.toReadableWords() }

  private fun String.toReadableWords(): String = lowercase()
    .replace('_', ' ')
    .replace(Regex("([a-z])([A-Z])"), "$1 $2")
    .replaceFirstChar { it.uppercase() }
}
