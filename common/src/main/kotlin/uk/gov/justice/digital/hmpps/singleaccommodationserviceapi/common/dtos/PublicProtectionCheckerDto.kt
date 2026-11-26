package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

data class PpcCommand(
  val supervisionRequired: Boolean,
  val controlMeasuresSuitable: Boolean,
  val practicalSupportSufficient: Boolean,
)

data class PpcCommandResponse(
  val publicProtectionRequired: Boolean,
)
