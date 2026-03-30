package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DutyToReferInvalidStatusTransitionException
import java.time.LocalDate
import java.util.UUID

class DutyToReferAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private var localAuthorityAreaId: UUID? = null,
  private var referenceNumber: String? = null,
  private var submissionDate: LocalDate? = null,
  private var status: DtrStatus? = null,
) {
  private val domainEvents = mutableListOf<SingleAccommodationServiceDomainEvent>()

  companion object {
    fun hydrateNew(caseId: UUID) = DutyToReferAggregate(
      id = UUID.randomUUID(),
      caseId = caseId,
    )

    fun hydrateExisting(
      id: UUID,
      caseId: UUID,
      localAuthorityAreaId: UUID,
      referenceNumber: String?,
      submissionDate: LocalDate,
      status: DtrStatus,
    ) = DutyToReferAggregate(
      id = id,
      caseId = caseId,
      localAuthorityAreaId = localAuthorityAreaId,
      referenceNumber = referenceNumber,
      submissionDate = submissionDate,
      status = status,
    )
  }

  fun updateDutyToRefer(
    localAuthorityAreaId: UUID,
    submissionDate: LocalDate,
    referenceNumber: String?,
    status: DtrStatus,
  ) {
    validateStatusTransition(status)

    val previousStatus = this.status

    this.localAuthorityAreaId = localAuthorityAreaId
    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.status = status

    if (previousStatus != status) {
      domainEvents += DutyToReferUpdatedDomainEvent(id)
    }
  }

  private fun validateStatusTransition(newStatus: DtrStatus) {
    when (this.status) {
      null -> if (newStatus != DtrStatus.SUBMITTED) throw DutyToReferInvalidStatusException()
      DtrStatus.SUBMITTED -> Unit
      else -> if (newStatus == DtrStatus.SUBMITTED) throw DutyToReferInvalidStatusTransitionException()
    }
  }

  fun pullDomainEvents(): List<SingleAccommodationServiceDomainEvent> = domainEvents.toList().also { domainEvents.clear() }

  fun snapshot() = DutyToReferSnapshot(
    id = id,
    caseId = caseId,
    localAuthorityAreaId = localAuthorityAreaId!!,
    referenceNumber = referenceNumber,
    submissionDate = submissionDate!!,
    status = status!!,
  )

  data class DutyToReferSnapshot(
    val id: UUID,
    val caseId: UUID,
    val localAuthorityAreaId: UUID,
    val referenceNumber: String?,
    val submissionDate: LocalDate,
    val status: DtrStatus,
  )
}
