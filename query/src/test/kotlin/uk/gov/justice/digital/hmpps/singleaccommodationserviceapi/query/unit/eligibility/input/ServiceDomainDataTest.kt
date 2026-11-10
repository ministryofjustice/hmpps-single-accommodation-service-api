package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.eligibility.input

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildAccommodationSummaryDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildDutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.SexCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ExternalRequestForPlacementOutcomeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildRoshLevel
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildSexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas1DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas2DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.Cas3DomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.CrsDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.DtrDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.input.PaDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildCas1ApplicationDomainData
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.factories.buildDomainData
import java.time.LocalDate

class ServiceDomainDataTest {

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `service projections preserve populated and missing inputs`(populated: Boolean) {
    val data = if (populated) {
      buildDomainData(
        tierScore = TierScore.B,
        dob = LocalDate.of(1990, 1, 1),
        sex = SexCode.M,
        roshLevel = buildRoshLevel(),
        currentAccommodation = buildAccommodationSummaryDto(),
        currentAccommodationTypeEntity = buildAccommodationTypeEntity(),
        nextAccommodations = listOf(buildAccommodationSummaryDto()),
        cas1Application = buildCas1ApplicationDomainData(
          currentOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(),
          historyExcludingCurrentOutcome = listOf(buildCas1ExternalRequestForPlacementOutcomeDto()),
        ),
        cas2Application = buildCas2Application(),
        cas3Application = buildCas3Application(),
        dutyToRefer = buildDutyToReferDto(),
        commissionedRehabilitativeServices = buildCommissionedRehabilitativeServices(),
        sexualOffenceRegistrations = buildSexualOffenceRegistrations(),
      )
    } else {
      buildDomainData()
    }

    assertThat(data.toCas1DomainData()).isEqualTo(
      Cas1DomainData(
        crn = data.crn,
        tierScore = data.tierScore,
        sex = data.sex,
        roshLevel = data.roshLevel,
        currentAccommodation = data.currentAccommodation,
        currentAccommodationTypeEntity = data.currentAccommodationTypeEntity,
        cas1Application = data.cas1Application,
      ),
    )
    assertThat(data.toCas2DomainData()).isEqualTo(
      Cas2DomainData(
        crn = data.crn,
        tierScore = data.tierScore,
        dob = data.dob,
        roshLevel = data.roshLevel,
        currentAccommodation = data.currentAccommodation,
        currentAccommodationTypeEntity = data.currentAccommodationTypeEntity,
        nextAccommodations = data.nextAccommodations,
        cas2Application = data.cas2Application,
        sexualOffenceRegistrations = data.sexualOffenceRegistrations,
      ),
    )
    assertThat(data.toCas3DomainData()).isEqualTo(
      Cas3DomainData(
        crn = data.crn,
        sex = data.sex,
        roshLevel = data.roshLevel,
        currentAccommodation = data.currentAccommodation,
        currentAccommodationTypeEntity = data.currentAccommodationTypeEntity,
        nextAccommodations = data.nextAccommodations,
        cas3Application = data.cas3Application,
        dutyToRefer = data.dutyToRefer,
        commissionedRehabilitativeServices = data.commissionedRehabilitativeServices,
      ),
    )
    assertThat(data.toCrsDomainData()).isEqualTo(
      CrsDomainData(
        crn = data.crn,
        sex = data.sex,
        currentAccommodation = data.currentAccommodation,
        currentAccommodationTypeEntity = data.currentAccommodationTypeEntity,
        nextAccommodations = data.nextAccommodations,
        commissionedRehabilitativeServices = data.commissionedRehabilitativeServices,
      ),
    )
    assertThat(data.toDtrDomainData()).isEqualTo(
      DtrDomainData(
        crn = data.crn,
        currentAccommodation = data.currentAccommodation,
        nextAccommodations = data.nextAccommodations,
        dutyToRefer = data.dutyToRefer,
      ),
    )
    assertThat(data.toPaDomainData()).isEqualTo(
      PaDomainData(
        crn = data.crn,
        nextAccommodations = data.nextAccommodations,
        cas1Application = data.cas1Application,
        cas3Application = data.cas3Application,
      ),
    )
  }
}
