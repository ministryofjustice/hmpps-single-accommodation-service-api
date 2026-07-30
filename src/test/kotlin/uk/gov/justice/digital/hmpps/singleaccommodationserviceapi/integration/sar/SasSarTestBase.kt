package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelperConfig
import java.util.UUID

@AutoConfigureWebTestClient
@Import(value = [SarIntegrationTestHelperConfig::class])
open class SasSarTestBase : SubjectAccessRequestServiceTestBase() {

  @BeforeEach
  fun clearData() {
    databaseUtils.truncate()
  }

  protected fun createCase(crn: String, nomsNumber: String, id: UUID = UUID.randomUUID()): CaseEntity {
    val case = buildCaseEntity(id = id)
    case.caseIdentifiers.clear()
    case.withIdentifier(crn, IdentifierType.CRN)
    case.withIdentifier(nomsNumber, IdentifierType.PRISON_NUMBER)
    return caseRepository.save(case)
  }
}
