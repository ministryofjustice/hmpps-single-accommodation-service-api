package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrOutcomeStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferUpdatedDomainEvent
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.SingleAccommodationServiceDomainEvent
import java.time.LocalDate
import java.util.UUID

class DutyToReferAggregate private constructor(
  private val id: UUID,
  private val crn: String,
  private var localAuthorityAreaId: UUID? = null,
  private var referenceNumber: String? = null,
  private var submissionDate: LocalDate? = null,
  private var outcomeStatus: DtrOutcomeStatus? = null,
) {
  private val domainEvents = mutableListOf<SingleAccommodationServiceDomainEvent>()

  companion object {
    fun hydrateNew(crn: String) = DutyToReferAggregate(
      id = UUID.randomUUID(),
      crn = crn,
    )
  }

  fun updateDutyToRefer(
    localAuthorityAreaId: UUID,
    submissionDate: LocalDate,
    referenceNumber: String?,
    outcomeStatus: DtrOutcomeStatus?,
  ) {
    this.localAuthorityAreaId = localAuthorityAreaId
    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber
    this.outcomeStatus = outcomeStatus

    if (outcomeStatus == DtrOutcomeStatus.YES) {
      domainEvents += DutyToReferUpdatedDomainEvent(id)
    }
  }

  fun pullDomainEvents(): List<SingleAccommodationServiceDomainEvent> = domainEvents.toList().also { domainEvents.clear() }

  fun snapshot() = DutyToReferSnapshot(
    id = id,
    crn = crn,
    localAuthorityAreaId = localAuthorityAreaId!!,
    referenceNumber = referenceNumber,
    submissionDate = submissionDate!!,
    outcomeStatus = outcomeStatus,
  )

  data class DutyToReferSnapshot(
    val id: UUID,
    val crn: String,
    val localAuthorityAreaId: UUID,
    val referenceNumber: String?,
    val submissionDate: LocalDate,
    val outcomeStatus: DtrOutcomeStatus?,
  )
}
