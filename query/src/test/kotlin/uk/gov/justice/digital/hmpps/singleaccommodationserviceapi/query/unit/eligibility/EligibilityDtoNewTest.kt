package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatusNew
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResultNew
import java.time.LocalDate

class EligibilityDtoNewTest {

  @ParameterizedTest
  @EnumSource(
    value = ServiceStatusNew::class,
    names = [
      "CAS1_UPCOMING",
      "CAS2_UPCOMING",
      "DTR_UPCOMING",
      "CRS_UPCOMING_REFERRAL",
      "CRS_UPCOMING_ACCOMMODATION_REFERRAL",
    ],
  )
  fun `Upcoming service status should always have an action start date - valid`(
    serviceStatus: ServiceStatusNew,
  ) {
    val thrown = catchThrowable {
      buildServiceResultNew(
        serviceStatus = serviceStatus,
        actionStartDate = LocalDate.now(),
      )
    }
    assertThat(thrown).isNull()
  }

  @ParameterizedTest
  @EnumSource(
    value = ServiceStatusNew::class,
    names = [
      "CAS1_UPCOMING",
      "CAS2_UPCOMING",
      "DTR_UPCOMING",
      "CRS_UPCOMING_REFERRAL",
      "CRS_UPCOMING_ACCOMMODATION_REFERRAL",
    ],
  )
  fun `Upcoming service status should always have an action start date - invalid`(
    serviceStatus: ServiceStatusNew,
  ) {
    val thrown = catchThrowable {
      buildServiceResultNew(
        serviceStatus = serviceStatus,
        actionStartDate = null,
      )
    }
    assertThat(thrown).hasMessage("Action start date is required for upcoming statuses and forbidden otherwise")
  }

  @ParameterizedTest
  @EnumSource(
    value = ServiceStatusNew::class,
    mode = EnumSource.Mode.EXCLUDE,
    names = [
      "CAS1_UPCOMING",
      "CAS2_UPCOMING",
      "DTR_UPCOMING",
      "CRS_UPCOMING_REFERRAL",
      "CRS_UPCOMING_ACCOMMODATION_REFERRAL",
    ],
  )
  fun `Non-Upcoming service status should never have an action start date - valid`(
    serviceStatus: ServiceStatusNew,
  ) {
    val thrown = catchThrowable {
      buildServiceResultNew(
        serviceStatus = serviceStatus,
        actionStartDate = null,
      )
    }
    assertThat(thrown).isNull()
  }

  @ParameterizedTest
  @EnumSource(
    value = ServiceStatusNew::class,
    mode = EnumSource.Mode.EXCLUDE,
    names = [
      "CAS1_UPCOMING",
      "CAS2_UPCOMING",
      "DTR_UPCOMING",
      "CRS_UPCOMING_REFERRAL",
      "CRS_UPCOMING_ACCOMMODATION_REFERRAL",
    ],
  )
  fun `Non-Upcoming service status should never have an action start date - invalid`(
    serviceStatus: ServiceStatusNew,
  ) {
    val thrown = catchThrowable {
      buildServiceResultNew(
        serviceStatus = serviceStatus,
        actionStartDate = LocalDate.now(),
      )
    }
    assertThat(thrown).hasMessage("Action start date is required for upcoming statuses and forbidden otherwise")
  }
}
