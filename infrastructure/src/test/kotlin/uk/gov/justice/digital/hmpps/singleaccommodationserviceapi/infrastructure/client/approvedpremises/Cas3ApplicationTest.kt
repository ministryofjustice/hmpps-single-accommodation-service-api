package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplicationDto

class Cas3ApplicationTest {

  @Nested
  inner class TopLevelApplicationTest {
    @CsvSource(
      "REJECTED,false,false",
      "REJECTED,true,true",
      "IN_PROGRESS,false,true",
      "IN_PROGRESS,true,false",
      "SUBMITTED,false,false",
      "SUBMITTED,true,true",
      "REQUESTED_FURTHER_INFORMATION,false,false",
      "REQUESTED_FURTHER_INFORMATION,true,true",
    )
    @ParameterizedTest(name = "status {0} has submitted app {1} valid {2}")
    fun `A submitted application is required for any status other than 'inProgress'`(
      status: Cas3ApplicationStatus,
      hasSubmittedApplication: Boolean,
      valid: Boolean,
    ) {
      val thrown = catchThrowable {
        buildCas3Application(
          applicationStatus = status,
          submittedApplication = if (hasSubmittedApplication) {
            buildCas3SubmittedApplicationDto()
          } else {
            null
          },
        )
      }

      if (valid) {
        assertThat(thrown).isNull()
      } else {
        assertThat(thrown).hasMessage("A submitted application is required for any status other than 'inProgress'")
      }
    }
  }

  @Nested
  inner class Cas3SubmittedApplicationDtoTest {

    @CsvSource(
      "UNALLOCATED,false,true",
      "UNALLOCATED,true,false",
      "IN_REVIEW,false,true",
      "IN_REVIEW,true,false",
      "READY_TO_PLACE,false,true",
      "READY_TO_PLACE,true,false",
      "CLOSED,false,true",
      "CLOSED,true,false",
      "REJECTED,true,true",
      "REJECTED,false,false",
    )
    @ParameterizedTest(name = "status {0} has rejection reason {1} valid {2}")
    fun `can only have rejection if status is rejected`(
      status: Cas3AssessmentStatus,
      hasRejectionReason: Boolean,
      valid: Boolean,
    ) {
      val thrown = catchThrowable {
        buildCas3SubmittedApplicationDto(
          assessmentStatus = status,
          assessmentRejectionReason = if (hasRejectionReason) {
            "the rejection reason"
          } else {
            null
          },
        )
      }

      if (valid) {
        assertThat(thrown).isNull()
      } else {
        assertThat(thrown).hasMessage("Assessment rejection reason can only be provided if status is `rejected`")
      }
    }
  }
}
