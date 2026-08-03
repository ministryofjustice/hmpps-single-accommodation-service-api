package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.utils

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode

fun String?.isProposedAccommodationStatus(): Boolean = this == AddressStatusCode.PR.name || this == AddressStatusCode.PR1.name
