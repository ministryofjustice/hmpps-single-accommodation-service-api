package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.input

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.aggregator.OrchestrationResultDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.tier.Tier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.dutytorefer.DutyToReferQueryService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.currentOutcome
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DomainDataFactory
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.EligibilityOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID

@ExtendWith(MockKExtension::class)
class DomainDataFactoryTest {

  @MockK
  lateinit var accommodationSummaryCalculator: AccommodationSummaryCalculator

  @MockK
  lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @MockK
  lateinit var dutyToReferQueryService: DutyToReferQueryService

  @InjectMockKs
  lateinit var factory: DomainDataFactory

  @Nested
  inner class BuildDomainData {
    val crn = "ABC123"

    @Test
    fun `returns correct DomainData`() {
      val endDate = LocalDate.now().plusDays(1)
      val expectedTier = "A1"
      val caseId = UUID.randomUUID()
      val cas1Application = buildCas1ApplicationV2()
      val cas2Application = buildCas2Application()
      val cas1CurrentPremises = buildCas1PremisesSummary()
      val cas3CurrentPremises = buildCas3PremisesSummary()
      val cas3Application = buildCas3Application()
      val cpr = buildCorePersonRecord(
        dateOfBirth = LocalDate.of(1990, 1, 1),
        addresses = listOf(
          buildCanonicalAddress(
            status = CanonicalAddressStatus(
              code = AddressStatusCode.M.name,
              description = AddressStatusCode.M.description,
            ),
            endDate = endDate,
            usages = listOf(
              CanonicalAddressUsage(
                usageCode = CanonicalAddressUsageCode(
                  code = AddressUsageCode.A02.name,
                  description = AddressUsageCode.A02.description,
                ),
                isActive = true,
              ),
            ),
          ),
        ),
      )
      val tier = Tier(expectedTier, UUID.randomUUID(), LocalDateTime.now(), null)
      val roshLevel = buildRoshLevel()
      val dutyToRefer = buildDutyToReferDto(crn, UUID.randomUUID(), DtrStatus.SUBMITTED, submission = null)
      val crs = buildCommissionedRehabilitativeServices(status = CrsReferralStatus.LIVE)
      val prisoner = buildPrisoner()
      val registrations = buildSexualOffenceRegistrations(crn)
      val orchestrationDto = OrchestrationResultDto(
        data = EligibilityOrchestrationDto(
          crn = crn,
          cpr = cpr,
          tier = tier,
          cas1Application = cas1Application,
          cas2Application = cas2Application,
          cas3Application = cas3Application,
          commissionedRehabilitativeServices = listOf(crs),
          prisoner = prisoner,
          cas1CurrentPremises = cas1CurrentPremises,
          cas3CurrentPremises = cas3CurrentPremises,
          sexualOffenceRegistrations = registrations,
          case = buildCase(crn = crn, roshLevel = roshLevel),
        ),
      )
      val caseEntity = buildCaseEntity(id = caseId)
      val currentAccommodation = buildAccommodationSummaryDto(endDate = endDate, type = buildAccommodationTypeDto(code = "A02"))
      val accommodationTypeEntity = buildAccommodationTypeEntity(isPrison = true, code = "A02")

      every { accommodationTypeRepository.findAll() } returns listOf(accommodationTypeEntity)
      every { accommodationSummaryCalculator.calculateNextAccommodationsV2(crn, cpr.addresses, cas1Application, cas3Application, currentAccommodation) } returns emptyList()
      every { dutyToReferQueryService.getDutyToRefer(caseEntity, crn) } returns dutyToRefer
      every { accommodationSummaryCalculator.calculateCurrentAccommodation(crn, cpr.addresses, prisoner, cas1CurrentPremises, cas3CurrentPremises) } returns currentAccommodation
      val result = factory.buildDomainData(crn, orchestrationDto.data, caseEntity)

      val expected = buildDomainData(
        crn = crn,
        tierScore = expectedTier,
        sex = cpr.sex!!.code,
        dob = cpr.dateOfBirth,
        currentAccommodation = currentAccommodation,
        currentAccommodationTypeEntity = accommodationTypeEntity,
        nextAccommodations = emptyList(),
        cas1Application = buildCas1ApplicationDomainData(cas1Application, currentOutcome = cas1Application.currentOutcome),
        cas2Application = cas2Application,
        cas3Application = cas3Application,
        commissionedRehabilitativeServices = crs,
        dutyToRefer = dutyToRefer,
        roshLevel = roshLevel,
        sexualOffenceRegistrations = registrations,
      )

      assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `selects the most recent live referral and ignores the rest`() {
      val cpr = buildCorePersonRecord()
      val currentAccommodation = buildAccommodationSummaryDto()
      val latestLiveReferral = buildCommissionedRehabilitativeServices(
        status = CrsReferralStatus.LIVE,
        sentAt = OffsetDateTime.now().minusDays(30),
      )
      val orchestrationDto = EligibilityOrchestrationDto(
        crn = crn,
        cpr = cpr,
        tier = null,
        cas1Application = null,
        cas2Application = null,
        cas3Application = null,
        commissionedRehabilitativeServices = listOf(
          buildCommissionedRehabilitativeServices(status = CrsReferralStatus.COMPLETED, sentAt = OffsetDateTime.now()),
          buildCommissionedRehabilitativeServices(status = CrsReferralStatus.WITHDRAWN, sentAt = OffsetDateTime.now()),
          latestLiveReferral,
          buildCommissionedRehabilitativeServices(status = CrsReferralStatus.LIVE, sentAt = OffsetDateTime.now().minusDays(60)),
        ),
        cas1CurrentPremises = null,
        cas3CurrentPremises = null,
        prisoner = null,
        sexualOffenceRegistrations = null,
        case = null,
      )

      every { accommodationTypeRepository.findAll() } returns emptyList()
      every { accommodationSummaryCalculator.calculateCurrentAccommodation(crn, cpr.addresses, null, null, null) } returns currentAccommodation
      every { accommodationSummaryCalculator.calculateNextAccommodationsV2(crn, cpr.addresses, null, null, currentAccommodation) } returns emptyList()

      val result = factory.buildDomainData(crn, orchestrationDto, caseEntity = null)

      assertThat(result.commissionedRehabilitativeServices).isEqualTo(latestLiveReferral)
      assertThat(result.sexualOffenceRegistrations).isNull()
    }
  }
}
