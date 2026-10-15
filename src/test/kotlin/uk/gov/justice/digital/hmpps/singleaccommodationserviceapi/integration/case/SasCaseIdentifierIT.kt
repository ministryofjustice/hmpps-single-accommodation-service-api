package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase

class SasCaseIdentifierIT : IntegrationTestBase() {

  @Test
  fun `should not allow a second active CRN for the same case`() {
    val case = caseRepository.saveAndFlush(buildCaseEntity { withCrn("CRN-1") })
    case.withCrn("CRN-2")

    assertThrows<DataIntegrityViolationException> {
      caseRepository.saveAndFlush(case)
    }
  }

  @Test
  fun `should not allow a second active prison number for the same case`() {
    val case = caseRepository.saveAndFlush(
      buildCaseEntity {
        withCrn("CRN-1")
        archiveCrn("CRN-1")
        withCrn("CRN-2")
        withPrisonNumber("PN-1")
      },
    )
    case.withPrisonNumber("PN-2")

    assertThrows<DataIntegrityViolationException> {
      caseRepository.saveAndFlush(case)
    }
  }

  @Test
  fun `should allow an archived CRN alongside an active CRN for the same case`() {
    val case = caseRepository.saveAndFlush(
      buildCaseEntity(
        customise = {
          withCrn("CRN-1")
          withPrisonNumber("PN-1")
        },
      ),
    )
    case.caseIdentifiers.add(
      buildCaseIdentifier(
        caseEntity = case,
        identifier = "CRN-ARCHIVED",
        identifierType = IdentifierType.CRN,
        archived = true,
      ),
    )

    assertDoesNotThrow {
      caseRepository.saveAndFlush(case)
    }
    assertThat(caseRepository.findByCrn("CRN-1")).isNotNull
  }
}
