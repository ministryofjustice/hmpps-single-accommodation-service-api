package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import java.util.UUID

@Repository
interface UserRepository: JpaRepository<UserEntity, UUID> {
  @Query("select u.id from UserEntity u where u.username = :username")
  fun findIdByUsername(@Param("username") username: String): UUID?

  fun findByUsername(username: String): UserEntity?
}