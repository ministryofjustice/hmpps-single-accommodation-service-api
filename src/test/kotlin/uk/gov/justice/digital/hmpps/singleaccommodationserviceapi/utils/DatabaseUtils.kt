package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DatabaseUtils(
  private val jdbcTemplate: JdbcTemplate,
) {

  @Transactional
  fun truncate(vararg tables: String = arrayOf("sas_user, sas_case")) {
    val tableList = tables.asList().joinToString(", ")

    jdbcTemplate.execute(
      """
      TRUNCATE TABLE $tableList CASCADE
      """.trimIndent(),
    )
  }
}
