package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremisesanddelius.CaseSummaries
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationDto
import java.util.UUID

class WiremockStubber {
  fun setupCaseOrchestrationStubs(crn: String, prisonNumber: String): CaseMutationOrchestrationDto {
    val responses = CaseMutationOrchestrationDto(
      crn = crn,
      cpr = buildCorePersonRecord(
        firstName = UUID.randomUUID().toString(),
        lastName = UUID.randomUUID().toString(),
        identifiers = buildIdentifiers(crns = listOf(crn), prisonNumbers = listOf(prisonNumber)),
      ),
      tier = buildTier(tierScore = UUID.randomUUID().toString()),
      case = buildCase(crn = crn, nomsNumber = prisonNumber),
      prisoner = buildPrisoner(prisonNumber = prisonNumber),
      cas1CurrentPremises = buildCas1PremisesSummary(),
      cas3CurrentPremises = buildCas3PremisesSummary(),
      cas1Application = buildCas1Application(),
      cas3Application = buildCas3Application(),
    )
    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = responses.cpr!!)
    PrisonerSearchStubs.getPrisonerOKResponse(prisonNumber = prisonNumber, response = responses.prisoner!!)
    ProbationIntegrationDeliusStubs.postCaseSummariesOKResponse(response = CaseSummaries(listOf(buildCaseSummary(crn = crn, nomsId = prisonNumber))))
    ProbationIntegrationDeliusStubs.getCaseByCrn(crn = crn, response = responses.case!!)
    TierStubs.getTierOKResponse(crn = crn, response = responses.tier!!)
    ApprovedPremisesStubs.getCas1CurrentPremisesOKResponse(crn = crn, response = responses.cas1CurrentPremises!!)
    ApprovedPremisesStubs.getCas1SuitableApplicationOKResponse(crn = crn, response = responses.cas1Application!!)
    ApprovedPremisesStubs.getCas3CurrentPremisesOKResponse(crn = crn, response = responses.cas3CurrentPremises!!)
    ApprovedPremisesStubs.getCas3SuitableApplicationOKResponse(crn = crn, response = responses.cas3Application!!)
    return responses
  }
}
