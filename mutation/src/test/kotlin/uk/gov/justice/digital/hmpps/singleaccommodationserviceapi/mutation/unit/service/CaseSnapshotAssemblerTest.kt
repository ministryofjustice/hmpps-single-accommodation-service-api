package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.unit.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.accommodation.AccommodationSummaryCalculator
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseMutationOrchestrationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service.CaseSnapshotAssembler
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@ExtendWith(MockKExtension::class)
class CaseSnapshotAssemblerTest {

  @MockK
  lateinit var accommodationSummaryCalculator: AccommodationSummaryCalculator

  @InjectMockKs
  lateinit var caseSnapshotAssembler: CaseSnapshotAssembler

  @Test
  fun `upsertCase() calculates accommodation summaries from the DTO and applies them onto the aggregate`() {
    val crn = "X12345"
    val cpr = buildCorePersonRecord()
    val prisoner = buildPrisoner()
    val tier = buildTier(tierScore = "A1")
    val currentAccommodation = buildAccommodationSummaryDto(crn = crn)
    val nextAccommodation = buildAccommodationSummaryDto(crn = crn)

    val dto = CaseMutationOrchestrationDto(
      crn = crn,
      cpr = cpr,
      tier = tier,
      prisoner = prisoner,
      cas1CurrentPremises = null,
      cas3CurrentPremises = null,
      cas1Application = null,
      cas3Application = null,
    )

    every {
      accommodationSummaryCalculator.calculateAccommodationSummaries(
        crn = crn,
        addresses = cpr.addresses,
        prisoner = prisoner,
        cas1CurrentPremises = null,
        cas3CurrentPremises = null,
        cas1Application = null,
        cas3Application = null,
      )
    } returns AccommodationSummariesDto(
      currentAccommodation = currentAccommodation,
      nextAccommodation = nextAccommodation,
      caseAccommodationStatus = CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE,
    )

    val aggregate = CaseAggregate.hydrateNew()

    val result = caseSnapshotAssembler.upsertCase(aggregate, dto)

    verify(exactly = 1) {
      accommodationSummaryCalculator.calculateAccommodationSummaries(
        crn = crn,
        addresses = cpr.addresses,
        prisoner = prisoner,
        cas1CurrentPremises = null,
        cas3CurrentPremises = null,
        cas1Application = null,
        cas3Application = null,
      )
    }

    val snapshot = result.snapshot()
    assertThat(snapshot.tierScore).isEqualTo(tier.tierScore)
    assertThat(snapshot.firstName).isEqualTo(cpr.firstName)
    assertThat(snapshot.lastName).isEqualTo(cpr.lastName)
    assertThat(snapshot.dateOfBirth).isEqualTo(cpr.dateOfBirth)
    assertThat(snapshot.currentAccommodation).isEqualTo(currentAccommodation)
    assertThat(snapshot.nextAccommodation).isEqualTo(nextAccommodation)
    assertThat(snapshot.accommodationStatus).isEqualTo(CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)
  }

  @Test
  fun `upsertCase() sets null accommodation fields when nothing calculated`() {
    val crn = "X12345"
    val dto = CaseMutationOrchestrationDto(
      crn = crn,
      cpr = null,
      tier = null,
      prisoner = null,
      cas1CurrentPremises = null,
      cas3CurrentPremises = null,
      cas1Application = null,
      cas3Application = null,
    )

    every {
      accommodationSummaryCalculator.calculateAccommodationSummaries(
        crn = crn,
        addresses = null,
        prisoner = null,
        cas1CurrentPremises = null,
        cas3CurrentPremises = null,
        cas1Application = null,
        cas3Application = null,
      )
    } returns AccommodationSummariesDto()

    val aggregate = CaseAggregate.hydrateNew()

    val snapshot = caseSnapshotAssembler.upsertCase(aggregate, dto).snapshot()

    assertThat(snapshot.currentAccommodation).isNull()
    assertThat(snapshot.nextAccommodation).isNull()
    assertThat(snapshot.accommodationStatus).isNull()
  }
}
