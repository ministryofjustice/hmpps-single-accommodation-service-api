package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.junit.jupiter.api.BeforeEach
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import java.util.UUID

open class SasSarTestBase : SubjectAccessRequestServiceTestBase() {

  @BeforeEach
  fun clearData() {
    databaseUtils.truncate()
  }

  protected fun createCase(crn: String, nomsNumber: String, id: UUID = UUID.randomUUID()): CaseEntity {
    val case = buildCaseEntity(id = id)
    case.caseIdentifiers.clear()
    case.withIdentifier(crn, IdentifierType.CRN)
    case.withIdentifier(nomsNumber, IdentifierType.NOMS)
    return caseRepository.save(case)
  }
}
