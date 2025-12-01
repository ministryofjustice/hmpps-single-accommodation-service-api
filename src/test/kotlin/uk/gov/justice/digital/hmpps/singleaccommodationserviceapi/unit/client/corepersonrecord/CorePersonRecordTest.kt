package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.client.corepersonrecord

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.aggregator.AggregatorService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.case.orchestration.CaseOrchestrationService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.CorePersonRecordCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationdelius.ProbationIntegrationDeliusCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.probationintegrationoasys.ProbationIntegrationOasysCachingService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.tier.TierCachingService

@ExtendWith(MockKExtension::class)
class CorePersonRecordTest {
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
  lateinit var caseOrchestrationService: CaseOrchestrationService

  @ParameterizedTest
  @CsvSource(
    value = [
      "John,Paul Andrew, Smith, John Paul Andrew Smith",
      "John, null, Smith, John Smith",
      "John, '', Smith, John Smith",
      "John, '       ', Smith, John Smith",
      "Firstname, MiddleName, null, Firstname MiddleName",
      "null, MiddleName, Lastname, MiddleName Lastname",
      "Alice, X, Wonderland, Alice X Wonderland",
      "Alice, X Y Z 1, Wonder land, Alice X Y Z 1 Wonder land",
      "null, null, null, ''",
    ],
    nullValues = ["null"],
  )
  fun `should format name correctly`(
    firstname: String?,
    middleNames: String?,
    lastName: String?,
    expected: String,
  ) {
    val cpr = CorePersonRecord(firstName = firstname, middleNames = middleNames, lastName = lastName)

    assertThat(cpr.fullName).isEqualTo(expected)
  }
}
