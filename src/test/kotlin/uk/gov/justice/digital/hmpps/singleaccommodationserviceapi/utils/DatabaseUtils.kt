package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_CASE
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.SAS_USER

@Component
class DatabaseUtils(
  private val jdbcTemplate: JdbcTemplate,
) {
  enum class SasTables(val tableName: String) {
    SAS_CASE("sas_case"),
    SAS_USER("sas_user"),
    DUTY_TO_REFER("duty_to_refer"),
    INBOX_EVENT("inbox_event"),
    OUTBOX_EVENT("outbox_event"),
  }

  @Transactional
  fun truncate(vararg tables: SasTables = arrayOf(SAS_USER, SAS_CASE)) {
    val tableList = tables.joinToString(", ") { it.tableName }

    jdbcTemplate.execute(
      """
      TRUNCATE TABLE $tableList CASCADE
      """.trimIndent(),
    )
  }
}
