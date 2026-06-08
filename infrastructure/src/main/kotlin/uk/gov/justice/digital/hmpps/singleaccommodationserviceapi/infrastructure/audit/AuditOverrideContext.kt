package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit

import java.util.UUID

object AuditOverrideContext {
  private val auditorId = ThreadLocal<UUID?>()

  fun currentAuditorId(): UUID? = auditorId.get()

  fun <T> withAuditorId(auditorId: UUID, block: () -> T): T {
    AuditOverrideContext.auditorId.set(auditorId)
    try {
      return block()
    } finally {
      AuditOverrideContext.auditorId.remove()
    }
  }
}
