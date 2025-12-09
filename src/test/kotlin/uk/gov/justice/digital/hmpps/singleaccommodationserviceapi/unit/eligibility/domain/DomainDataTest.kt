package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.client.corepersonrecord.Sex
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.cas1.Cas1RuleSet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.eligibility.domain.enums.ServiceStatus
import java.time.OffsetDateTime
import java.util.stream.Stream

class DomainDataTest {

  private companion object {
    private val male = Sex(
      code = "M",
      description = "Male",
    )
    private val female = Sex(
      code = "F",
      description = "Female",
    )

    @JvmStatic
    fun provideDataInputsAndServiceResultOutputs(): Stream<Arguments?> = Stream.of(
      Arguments.of(
        "A1",
        male,
        OffsetDateTime.now().plusMonths(7),
        ServiceStatus.UPCOMING,
        listOf("Start approved premise referral in 31 days"),
      ),
      Arguments.of(
        "A1",
        female,
        OffsetDateTime.now().plusMonths(7),
        ServiceStatus.UPCOMING,
        listOf("Start approved premise referral in 31 days"),
      ),
      Arguments.of(
        "A1",
        female,
        OffsetDateTime.now().plusMonths(5),
        ServiceStatus.NOT_STARTED,
        listOf("Start approved premise referral"),
      ),
      Arguments.of(
        "A1",
        female,
        OffsetDateTime.now().plusMonths(6).plusDays(1),
        ServiceStatus.UPCOMING,
        listOf("Start approved premise referral in 1 day"),
      ),
      Arguments.of(
        "A1",
        female,
        OffsetDateTime.now().plusMonths(6).minusDays(1),
        ServiceStatus.NOT_STARTED,
        listOf("Start approved premise referral"),
      ),
      Arguments.of(
        "A1S",
        female,
        OffsetDateTime.now().plusMonths(6).minusDays(1),
        ServiceStatus.NOT_ELIGIBLE,
        listOf<String>(),
      ),
      Arguments.of(
        "D1",
        female,
        OffsetDateTime.now().plusMonths(6).minusDays(1),
        ServiceStatus.NOT_ELIGIBLE,
        listOf<String>(),
      ),
    )
  }

  @Nested
  inner class CalculateEligibility {

    private val cas1RuleSet = Cas1RuleSet()
    private val crn = "ABC234"

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.unit.eligibility.domain.DomainDataTest#provideDataInputsAndServiceResultOutputs")
    fun `calculate eligibility for cas1 for a range of people`(
      tier: String,
      sex: Sex,
      releaseDate: OffsetDateTime,
      serviceStatus: ServiceStatus,
      actions: List<String>,
    ) {
      val data = DomainData(
        crn = crn,
        tier = tier,
        sex = sex,
        releaseDate = releaseDate,
      )
      val result = data.calculateEligibility(cas1RuleSet)
      val expectedResult = ServiceResult(
        serviceStatus = serviceStatus,
        actions = actions,
      )
      Assertions.assertThat(result).isEqualTo(expectedResult)
    }
  }
}
