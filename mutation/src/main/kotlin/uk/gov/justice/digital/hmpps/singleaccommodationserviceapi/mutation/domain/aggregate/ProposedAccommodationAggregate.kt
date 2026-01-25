package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.event.AccommodationDataDomainEvent
import java.util.UUID

class ProposedAccommodationAggregate private constructor(
  private val id: UUID
) {
  private val domainEvents = mutableListOf<AccommodationDataDomainEvent>()

  companion object {
    fun hydrate(
      id: UUID,
    ) = ProposedAccommodationAggregate(
      id = id,
    )
  }
}
