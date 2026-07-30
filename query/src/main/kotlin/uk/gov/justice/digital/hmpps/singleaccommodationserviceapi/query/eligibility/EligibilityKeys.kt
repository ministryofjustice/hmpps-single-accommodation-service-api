package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility

// TODO - these drive the link text on the micro tracker cards currently - we should migrate to using linkType enum on EligibilityDto
object EligibilityKeys {
  // CAS1
  const val CREATE_NEW_PLACEMENT_REQUEST = "Create new placement request" // LINK TEXT
  const val CREATE_PLACEMENT_REQUEST = "Create placement request" // LINK TEXT
  const val START_NEW_APPLICATION = "Start new application" // LINK TEXT
  const val START_APPLICATION = "Start application" // LINK TEXT
  const val CONTINUE_APPLICATION = "Continue application" // LINK TEXT
  const val VIEW_APPLICATION = "View application" // LINK TEXT

  // CAS3
  const val START_REFERRAL = "Start referral" // LINK TEXT
  const val START_NEW_REFERRAL = "Start new referral" // LINK TEXT
  const val VIEW_REFERRAL = "View referral" // LINK TEXT

  // DTR
  const val ADD_REFERRAL_DETAILS = "Add referral details" // LINK TEXT
  const val ADD_OUTCOME = "Add outcome" // LINK TEXT

  // CRS
  const val VIEW_REFER_AND_MONITOR = "View refer and monitor" // LINK TEXT
}
