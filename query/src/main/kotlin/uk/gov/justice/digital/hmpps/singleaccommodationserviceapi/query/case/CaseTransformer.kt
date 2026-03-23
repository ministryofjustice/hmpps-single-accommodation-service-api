package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AssignedToDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpstreamFailureType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.UpstreamFailure
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationdelius.CaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.probationintegrationoasys.RoshDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.TierScore as TierScoreInfra

object CaseTransformer {
  fun toCaseDto(
    crn: String,
    cpr: CorePersonRecord?,
    roshDetails: RoshDetails?,
    tier: Tier?,
    caseSummaries: List<CaseSummary>?,
    upstreamFailures: List<UpstreamFailure> = emptyList(),
  ) = CaseDto(
    name = cpr?.let { toFullName(it) },
    dateOfBirth = cpr?.dateOfBirth,
    crn = crn,
    prisonNumber = cpr?.identifiers?.prisonNumbers?.firstOrNull(),
    tier = tier?.let { toTierScore(tier.tierScore) },
    riskLevel = roshDetails?.let { RiskLevelTransformer.determineOverallRiskLevel(roshDetails.rosh) },
    pncReference = cpr?.identifiers?.pncs?.firstOrNull(),
    assignedTo = caseSummaries?.firstOrNull()?.manager?.team?.name?.let {
      AssignedToDto(1L, name = it)
    },
    photoUrl = null,
    currentAccommodation = null,
    nextAccommodation = null,
    upstreamFailures = upstreamFailures.map { toUpstreamFailureDto(it) },
  )

  fun toUpstreamFailureDto(failure: UpstreamFailure) = UpstreamFailureDto(
    service = failure.callKey,
    type = UpstreamFailureType.valueOf(failure.type.name),
    httpStatus = failure.errorDetail.httpStatus,
    message = failure.errorDetail.message,
  )

  fun toFullName(cpr: CorePersonRecord) = listOfNotNull(
    cpr.firstName,
    cpr.middleNames?.takeIf { it.isNotBlank() },
    cpr.lastName,
  ).joinToString(" ")

  fun toTierScore(tierScoreInfra: TierScoreInfra) = TierScore.valueOf(tierScoreInfra.name)
}
