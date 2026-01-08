package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.temp

import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile(value = ["local", "dev"])
@RestController
class DatabaseIntegrationController(private val tableCountProbe: TableCountProbe) {

  @PreAuthorize("hasAnyRole('ROLE_PROBATION', 'ROLE_ACCOMMODATION_API__SINGLE_ACCOMMODATION_SERVICE')")
  @GetMapping("/db/table/count")
  fun getMockData() = ResponseEntity.ok("Current active tables in db: ${tableCountProbe.countTables()}")
}

@Component
class TableCountProbe(
  private val jdbcTemplate: JdbcTemplate,
) {

  fun countTables(): Int = jdbcTemplate.queryForObject(
    """
      SELECT COUNT(*)
      FROM INFORMATION_SCHEMA.TABLES
      WHERE TABLE_TYPE = 'BASE TABLE'
    """.trimIndent(),
    Int::class.java,
  ) ?: 0
}
