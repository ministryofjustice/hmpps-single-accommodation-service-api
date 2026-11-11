package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config.MutableTestClock
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withIdentifier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.IdentifierType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.sar.SubjectAccessRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar.util.SasSarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelperConfig
import java.sql.Timestamp
import java.util.UUID

@Import(value = [SarIntegrationTestHelperConfig::class])
class SarTestBase : IntegrationTestBase() {

  @Autowired
  lateinit var sarService: SubjectAccessRequestService

  @Autowired
  lateinit var dutyToReferRepository: DutyToReferRepository

  @Autowired
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @Autowired
  lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @Autowired
  lateinit var accommodationStatusRepository: AccommodationStatusRepository

  @Autowired
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @Autowired
  lateinit var sarIntegrationTestHelper: SarIntegrationTestHelper

  @Autowired
  lateinit var sasSarIntegrationTestHelper: SasSarIntegrationTestHelper

  @Autowired
  lateinit var testClock: MutableTestClock

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var transactionTemplate: TransactionTemplate

  @Autowired
  lateinit var jdbcTemplate: JdbcTemplate

  @BeforeEach
  fun setup() {
    databaseUtils.truncate(*DatabaseUtils.SasTables.entries.toTypedArray())
  }

  // TODO: remove
  protected fun createCase(crn: String, nomsNumber: String, id: UUID = UUID.randomUUID()): CaseEntity {
    val case = buildCaseEntity(id = id)
    case.caseIdentifiers.clear()
    case.withIdentifier(crn, IdentifierType.CRN)
    case.withIdentifier(nomsNumber, IdentifierType.PRISON_NUMBER)
    return caseRepository.save(case)
  }

  // TODO: remove
  protected fun fixUpdatedAndCreatedTimestamps(table: DatabaseUtils.SasTables, id: UUID, timestamp: Timestamp) {
    jdbcTemplate.update(
      "update ${table.tableName} set created_at = ?, last_updated_at = ? where id = ?",
      timestamp,
      timestamp,
      id,
    )
  }
}
