package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.audit

import org.javers.core.Javers
import org.javers.core.commit.CommitId
import org.javers.core.diff.Change
import org.javers.core.diff.changetype.NewObject
import org.javers.core.diff.changetype.ValueChange
import org.javers.repository.jql.QueryBuilder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditEventDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditEventType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.FieldChangeDto
import java.time.ZoneOffset
import java.util.UUID

@Service
class AuditService(
    private val javers: Javers,
) {

  fun  <T : Any> fullAuditHistory(id: UUID, entityClass: Class<T>): List<AuditEventDto> {
    val changes = javers.findChanges(
      QueryBuilder.byInstanceId(id, entityClass)
        .build(),
    )

    return when (changes.isEmpty()) {
      true -> emptyList()
      else -> {
        val changesByCommit = changes.groupBy {
          it.commitMetadata.get().id
        }
        return changesByCommit.entries
          .map { (commitId, commitChanges) ->
            convertChangersToAuditEventDto(commitId, commitChanges)
          }
      }
    }
  }

  private fun convertChangersToAuditEventDto(commitId: CommitId, commitChanges: List<Change>): AuditEventDto {
    val commitMeta = commitChanges.first().commitMetadata.get()
    val eventType = when {
      commitChanges.any { it is NewObject } -> AuditEventType.CREATE
      else -> AuditEventType.UPDATE
    }

    val fieldChanges = commitChanges
      .filterIsInstance<ValueChange>()
      .map {
          FieldChangeDto(
              field = it.propertyName,
              oldValue = it.left,
              newValue = it.right,
          )
      }

    return AuditEventDto(
        type = eventType,
        commitId = commitId.value(),
        commitDate = commitMeta.commitDate.toInstant(ZoneOffset.UTC),
        changes = fieldChanges,
    )
  }
}