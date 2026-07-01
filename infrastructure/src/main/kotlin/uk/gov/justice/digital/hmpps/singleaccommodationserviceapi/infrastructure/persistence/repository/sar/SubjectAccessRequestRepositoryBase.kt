package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.sar

import org.postgresql.util.PGobject
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
open class SubjectAccessRequestRepositoryBase(
  protected val jdbcTemplate: NamedParameterJdbcTemplate,
) {
  protected fun toJsonString(result: Map<String, Any?>): String? {
    val json = (result["json"] as PGobject?)
    if (json != null && json.value != null) {
      return json.value
    }
    return null
  }

  protected fun MapSqlParameterSource.addSarParameters(
    crn: String?,
    nomsNumber: String?,
    startDate: LocalDateTime?,
    endDate: LocalDateTime?,
  ): MapSqlParameterSource {
    addValue("crn", crn)
    addValue("noms_number", nomsNumber)
    addValue("start_date", startDate)
    addValue("end_date", endDate)
    return this
  }
}
