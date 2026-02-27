package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrOutcomeStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.messaging.event.DutyToReferCreatedDomainEvent
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
  private var outcomeDate: LocalDate? = null,
) {
  private val domainEvents = mutableListOf<SingleAccommodationServiceDomainEvent>()

  companion object {
    fun hydrateNew(crn: String) = DutyToReferAggregate(
      id = UUID.randomUUID(),
      crn = crn,
    )
  }

  fun createDutyToRefer(
    localAuthorityAreaId: UUID,
    submissionDate: LocalDate,
    referenceNumber: String?,
  ) {
    this.localAuthorityAreaId = localAuthorityAreaId
    this.submissionDate = submissionDate
    this.referenceNumber = referenceNumber

    domainEvents += DutyToReferCreatedDomainEvent(id)
  }

  fun pullDomainEvents(): List<SingleAccommodationServiceDomainEvent> = domainEvents.toList().also { domainEvents.clear() }

  fun snapshot() = DutyToReferSnapshot(
    id = id,
    crn = crn,
    localAuthorityAreaId = localAuthorityAreaId!!,
    referenceNumber = referenceNumber,
    submissionDate = submissionDate!!,
    outcomeStatus = outcomeStatus,
    outcomeDate = outcomeDate,
  )

  data class DutyToReferSnapshot(
    val id: UUID,
    val crn: String,
    val localAuthorityAreaId: UUID,
    val referenceNumber: String?,
    val submissionDate: LocalDate,
    val outcomeStatus: DtrOutcomeStatus?,
    val outcomeDate: LocalDate?,
  )
}
