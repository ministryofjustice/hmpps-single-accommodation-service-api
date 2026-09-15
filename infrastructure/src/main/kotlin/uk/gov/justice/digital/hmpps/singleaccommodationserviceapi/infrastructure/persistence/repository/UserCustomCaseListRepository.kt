package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserCustomCaseListEntity
import java.util.UUID

@Repository
interface UserCustomCaseListRepository : JpaRepository<UserCustomCaseListEntity, UUID> {
  fun deleteBySasUserId(sasUserId: UUID)
}
