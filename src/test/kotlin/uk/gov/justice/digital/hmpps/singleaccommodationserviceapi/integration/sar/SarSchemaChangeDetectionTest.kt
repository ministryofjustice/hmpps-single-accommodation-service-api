package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarFlywaySchemaTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarJpaEntitiesTest
import javax.sql.DataSource

/**
 * Tests that detect a change in the flyway schema or entity model to act as aa
 * prompt to developers to consider updating the SAR report
 */
class SarSchemaChangeDetectionTest : IntegrationTestBase() {

  @Autowired
  lateinit var dataSource: DataSource

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var sarIntegrationTestHelper: SarIntegrationTestHelper

  /**
   * Fails if the latest Flyway schema version doesn't match the 'expected-flyway-schema-version'
   * value in application-test-sar.yml
   *
   * The intention of this test is to make you think about whether an update to the SAR template
   * is required when the database schema changes
   *
   * Once that decision has been made, fix this test by running `./gradlew updateSarSchemaTrackers`
   *
   * For more information see docs/subject_access_requests.md
   */
  @Nested
  inner class FlywaySchemaTest : SarFlywaySchemaTest {
    override fun getDataSourceInstance() = dataSource
    override fun getSarHelper() = sarIntegrationTestHelper
  }

  /**
   * Fails if the generated snapshot of the JPA entity model doesn't match sar/sas-entities-schema.json
   *
   * The intention of this test is to make you think about whether an update to the SAR template
   * is required when the entity model changes
   *
   * Once that decision has been made, fix this test by running `./gradlew updateSarSchemaTrackers`
   *
   * For more information see docs/subject_access_requests.md
   */
  @Nested
  inner class JpaEntitiesTest : SarJpaEntitiesTest {
    override fun getSarHelper() = sarIntegrationTestHelper
    override fun getEntityManagerInstance() = entityManager
  }
}
