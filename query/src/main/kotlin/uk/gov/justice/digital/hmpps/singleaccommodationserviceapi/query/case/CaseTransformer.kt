package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore as TierScoreInfra

fun toCaseDto(
  crn: String,
  cpr: CorePersonRecord?,
  roshDetails: RoshDetails?,
  tier: Tier?,
  caseSummaries: List<CaseSummary>?,
) = CaseDto(
  name = cpr?.let { toFullName(it) },
  dateOfBirth = cpr?.dateOfBirth,
  crn = crn,
  prisonNumber = cpr?.identifiers?.prisonNumbers?.firstOrNull(),
  tier = tier?.let { toTierScore(tier.tierScore) },
  riskLevel = roshDetails?.let { determineOverallRiskLevel(roshDetails.rosh) },
  pncReference = cpr?.identifiers?.pncs?.firstOrNull(),
  assignedTo = caseSummaries?.firstOrNull()?.manager?.team?.name?.let {
    AssignedToDto(1L, name = it)
  },
  photoUrl = null,
  currentAccommodation = null,
  nextAccommodation = null,
)

fun toFullName(cpr: CorePersonRecord) = listOfNotNull(
  cpr.firstName,
  cpr.middleNames?.takeIf { it.isNotBlank() },
  cpr.lastName,
).joinToString(" ")

fun toTierScore(tierScoreInfra: TierScoreInfra) = TierScore.valueOf(tierScoreInfra.name)
