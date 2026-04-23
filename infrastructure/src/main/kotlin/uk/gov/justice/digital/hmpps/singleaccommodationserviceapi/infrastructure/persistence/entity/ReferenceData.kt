package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity

import java.util.UUID

interface ReferenceData {
  val id: UUID
  val name: String
  val code: String
}
