package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.domain.cas1.completion

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAction
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseActionType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LinkType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.EligibilityKeys
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1.completion.Cas1CompletionContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildServiceResult
import java.util.UUID
import java.util.stream.Stream

class Cas1CompletionContextUpdaterTest {
  private val updater = Cas1CompletionContextUpdater()

  @Nested
  inner class UpdateTests {
    @Test
    fun `update builds service result using toServiceResult`() {
      val applicationId = UUID.randomUUID()
      val data = buildDomainData(
        cas1Application = buildCas1Application(
          id = applicationId,
          applicationStatus = Cas1ApplicationStatus.AWAITING_ASSESSMENT,
        ),
      )
      val context = EvaluationContext(
        data = data,
        currentResult = buildServiceResult(),
      )

      val result = updater.update(context)

      assertThat(result.currentResult.serviceStatus).isEqualTo(ServiceStatus.SUBMITTED)
      assertThat(result.currentResult.action).isNull()
      assertThat(result.currentResult.link).isEqualTo(EligibilityKeys.VIEW_APPLICATION)
      assertThat(result.currentResult.linkType).isEqualTo(LinkType.CAS1_VIEW_APPLICATION)
      assertThat(result.currentResult.url).isNull()
    }
  }

  @ParameterizedTest
  @MethodSource("placementAllocatedWithoutLivePlacement")
  fun `PLACEMENT_ALLOCATED with no live placement maps on the next placement request`(
    requestForPlacementStatus: Cas1RequestForPlacementStatus,
    expectedServiceStatus: ServiceStatus,
    expectedAction: CaseAction?,
    expectedLink: String,
  ) {
    val data = buildDomainData(
      cas1Application = buildCas1Application(
        applicationStatus = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
        requestForPlacementStatus = requestForPlacementStatus,
        placementStatus = null,
      ),
    )
    val context = EvaluationContext(
      data = data,
      currentResult = buildServiceResult(),
    )

    val result = updater.update(context)

    assertThat(result.currentResult.serviceStatus).isEqualTo(expectedServiceStatus)
    assertThat(result.currentResult.action).isEqualTo(expectedAction)
    assertThat(result.currentResult.link).isEqualTo(expectedLink)
    assertThat(result.currentResult.linkType).isEqualTo(LinkType.CAS1_VIEW_APPLICATION)
    assertThat(result.currentResult.url).isNull()
  }

  private companion object {
    @JvmStatic
    fun placementAllocatedWithoutLivePlacement(): Stream<Arguments> = Stream.of(
      Arguments.of(
        Cas1RequestForPlacementStatus.REQUEST_REJECTED,
        ServiceStatus.PLACEMENT_REQUEST_REJECTED,
        CaseAction(type = CaseActionType.CREATE_PLACEMENT),
        EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      ),
      Arguments.of(
        Cas1RequestForPlacementStatus.REQUEST_WITHDRAWN,
        ServiceStatus.PLACEMENT_REQUEST_WITHDRAWN,
        CaseAction(type = CaseActionType.CREATE_PLACEMENT),
        EligibilityKeys.CREATE_NEW_PLACEMENT_REQUEST,
      ),
      Arguments.of(
        Cas1RequestForPlacementStatus.REQUEST_UNSUBMITTED,
        ServiceStatus.PLACEMENT_REQUEST_NOT_STARTED,
        CaseAction(type = CaseActionType.CREATE_PLACEMENT),
        EligibilityKeys.CREATE_PLACEMENT_REQUEST,
      ),
      Arguments.of(
        Cas1RequestForPlacementStatus.AWAITING_MATCH,
        ServiceStatus.PLACEMENT_REQUEST_SUBMITTED,
        null,
        EligibilityKeys.VIEW_APPLICATION,
      ),
      Arguments.of(
        Cas1RequestForPlacementStatus.REQUEST_SUBMITTED,
        ServiceStatus.PLACEMENT_REQUEST_SUBMITTED,
        null,
        EligibilityKeys.VIEW_APPLICATION,
      ),
    )
  }
}
