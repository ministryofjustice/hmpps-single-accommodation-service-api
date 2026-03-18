package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit

import org.javers.core.Changes
import org.javers.core.Javers
import org.javers.core.diff.Change
import org.javers.core.diff.changetype.NewObject
import org.javers.core.diff.changetype.ValueChange
import org.javers.repository.jql.QueryBuilder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CreateFieldChangeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.UpdateFieldChangeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.util.UUID

@Service
class AuditService(
  private val javers: Javers,
  private val userRepository: UserRepository,
) {
  fun <T : Any> fullAuditHistory(id: UUID, entityClass: Class<T>): List<AuditRecordDto> {
    val changes = javers.findChanges(
      QueryBuilder.byInstanceId(id, entityClass)
        .build(),
    )
    return if (changes.isEmpty()) emptyList() else generateAuditRecordList(changes)
  }

  private fun generateAuditRecordList(changes: Changes): List<AuditRecordDto> {
    val changesByCommit = changes.groupBy {
      it.commitMetadata.get().id
    }
    val authorsIds = changesByCommit.entries
      .map { (_, commitChanges) -> commitChanges.first().commitMetadata.get().author }
      .distinct()

    val authorIdAndUserMap = authorsIds.associate { authorId ->
      val id = UUID.fromString(authorId)
      id to userRepository.findByIdOrNull(id)!!
    }
    return changesByCommit.entries
      .map { (_, commitChanges) ->
        generateAuditRecordDto(authorIdAndUserMap, commitChanges)
      }
  }

  private fun generateAuditRecordDto(authorUsers: Map<UUID, UserEntity>, commitChanges: List<Change>): AuditRecordDto {
    val commitMeta = commitChanges.first().commitMetadata.get()
    val eventType =
      if (commitChanges.any { it is NewObject }) {
        AuditRecordType.CREATE
      } else {
        AuditRecordType.UPDATE
      }

    val fieldChanges = commitChanges
      .filterIsInstance<ValueChange>()
      .map {
        when (eventType) {
          AuditRecordType.CREATE -> CreateFieldChangeDto(
            field = it.propertyName,
            value = it.right,
          )
          else -> UpdateFieldChangeDto(
            field = it.propertyName,
            value = it.right,
            oldValue = it.left,
          )
        }
      }
    val userId = UUID.fromString(commitMeta.author)
    val user = authorUsers[userId]!!
    return AuditRecordDto(
      type = eventType,
      author = user.name,
      commitDate = commitMeta.commitDateInstant,
      changes = fieldChanges,
    )
  }
}
