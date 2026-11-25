package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class UpdateUserPreferencesCommand(
  val tourDismissed: Boolean? = null,
)
