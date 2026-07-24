package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AuthSource
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.Username
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
  fun findByUsernameAndAuthSource(username: Username, authSource: AuthSource): UserEntity?

  @Modifying
  @Transactional
  @Query(
    nativeQuery = true,
    value = """
    INSERT INTO sas_user (
      id, username, forename, middle_names, surname, 
      auth_source, email, telephone_number,
      nomis_staff_id, nomis_account_type, nomis_active_caseload_id,
      is_enabled, is_active 
    )
    VALUES (
      :#{#user.id}, :#{#user.username}, :#{#user.forename}, :#{#user.middleNames}, :#{#user.surname},
      :#{#user.authSource.name}, :#{#user.email}, :#{#user.telephoneNumber},
      :#{#user.nomisStaffId}, :#{#user.nomisAccountType}, :#{#user.nomisActiveCaseloadId},
      :#{#user.isEnabled}, :#{#user.isActive}
    )
    ON CONFLICT (username, auth_source) DO NOTHING
  """,
  )
  fun createUser(user: UserEntity)
}
