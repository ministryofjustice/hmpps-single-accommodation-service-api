package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.case

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.case.CaseService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.ProbationIntegrationOasysCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierCachingService

@ExtendWith(MockKExtension::class)
class CaseServiceTest {
  @MockK
  lateinit var aggregatorService: AggregatorService

  @MockK
  lateinit var probationIntegrationDeliusCachingService: ProbationIntegrationDeliusCachingService

  @MockK
  lateinit var corePersonRecordCachingService: CorePersonRecordCachingService

  @MockK
  lateinit var probationIntegrationOasysCachingService: ProbationIntegrationOasysCachingService

  @MockK
  lateinit var tierCachingService: TierCachingService

  @InjectMockKs
  lateinit var caseService: CaseService

  @Test
  fun `should format full name with middle names`() {
    val cpr = CorePersonRecord(firstName = "John", middleNames = "Paul Andrew", lastName = "Smith")

    val result = caseService.formatName(cpr)

    assertThat(result).isEqualTo("John Paul Andrew Smith")
  }

  @Test
  fun `should format name without middle names when null`() {
    val cpr = CorePersonRecord(firstName = "John", middleNames = null, lastName = "Smith")

    val result = caseService.formatName(cpr)

    assertThat(result).isEqualTo("John Smith")
  }

  @Test
  fun `should format name without middle names when empty`() {
    val cpr = CorePersonRecord(firstName = "John", middleNames = "", lastName = "Smith")

    val result = caseService.formatName(cpr)

    assertThat(result).isEqualTo("John Smith")
  }

  @Test
  fun `should format name without middle names when blank`() {
    val cpr = CorePersonRecord(firstName = "John", middleNames = "   ", lastName = "Smith")

    val result = caseService.formatName(cpr)

    assertThat(result).isEqualTo("John Smith")
  }

  @Test
  fun `should handle unusual but valid input`() {
    val cpr = CorePersonRecord(firstName = "Alice", middleNames = "X", lastName = "Wonderland")

    val result = caseService.formatName(cpr)

    assertThat(result).isEqualTo("Alice X Wonderland")
  }
}
