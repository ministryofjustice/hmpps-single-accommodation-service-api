package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate

import java.util.UUID

class PublicProtectionCheckerAggregate private constructor(
  private val id: UUID,
  private val caseId: UUID,
  private val supervisionRequired: Boolean,
  private val controlMeasuresSuitable: Boolean,
  private val practicalSupportSufficient: Boolean,
) {
  companion object {
    fun create(
      caseId: UUID,
      supervisionRequired: Boolean,
      controlMeasuresSuitable: Boolean,
      practicalSupportSufficient: Boolean,
    ) = PublicProtectionCheckerAggregate(
      id = UUID.randomUUID(),
      caseId = caseId,
      supervisionRequired = supervisionRequired,
      controlMeasuresSuitable = controlMeasuresSuitable,
      practicalSupportSufficient = practicalSupportSufficient,
    )
  }

  fun snapshot() = PublicProtectionCheckerSnapshot(
    id = id,
    caseId = caseId,
    supervisionRequired = supervisionRequired,
    controlMeasuresSuitable = controlMeasuresSuitable,
    practicalSupportSufficient = practicalSupportSufficient,
  )

  data class PublicProtectionCheckerSnapshot(
    val id: UUID,
    val caseId: UUID,
    val supervisionRequired: Boolean,
    val controlMeasuresSuitable: Boolean,
    val practicalSupportSufficient: Boolean,
  )
}
