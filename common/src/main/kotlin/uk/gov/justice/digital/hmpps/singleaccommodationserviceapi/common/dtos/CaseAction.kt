package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos

import java.time.LocalDate

data class CaseAction(
  val type: CaseActionType,
  val startDate: LocalDate? = null,
)

enum class CaseActionType {
  // CAS1
  CREATE_PLACEMENT,
  PROVIDE_INFORMATION,
  START_APPROVED_PREMISE_APPLICATION,
  CONTINUE_APPROVED_PREMISE_APPLICATION,

  // CAS3
  START_CAS3_REFERRAL,
  REPLY_TO_CAS3_BEDSPACE_OFFER,

  // DTR
  SUBMIT_DTR_REFERRAL,
  ADD_DTR_REFERRAL_DETAILS,
  ADD_DTR_OUTCOME,

  // CRS
  SUBMIT_CRS_ACCOMMODATION_REFERRAL,
  SUBMIT_CRS_REFERRAL,

  // PA
  ADD_AND_CONFIRM_PROPOSED_ADDRESS,
}
