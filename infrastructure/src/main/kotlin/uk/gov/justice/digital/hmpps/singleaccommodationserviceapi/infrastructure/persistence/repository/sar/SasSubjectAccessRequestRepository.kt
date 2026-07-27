package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.sar

import jakarta.persistence.EntityManager
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.UserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import java.time.Instant
import java.util.UUID

@Repository
class SasSubjectAccessRequestRepository(
  private val entityManager: EntityManager,
  jdbcTemplate: NamedParameterJdbcTemplate,
) : SubjectAccessRequestRepositoryBase(jdbcTemplate) {

  // Make this a repo, with jpaql queries

  fun findProposedAccommodations(caseId: UUID, startDate: Instant?, endDate: Instant?): List<ProposedAccommodationEntity> {
    val clauses = mutableListOf("pa.caseId = :caseId", "pa.deleted = false")
    val params = mutableMapOf<String, Any>("caseId" to caseId)

    startDate?.let {
      clauses.add("pa.createdAt >= :startDate")
      params["startDate"] = it
    }
    endDate?.let {
      clauses.add("pa.createdAt <= :endDate")
      params["endDate"] = it
    }

    val jpql = """
      select distinct pa from ProposedAccommodationEntity pa 
      left join fetch pa.notes 
      where ${clauses.joinToString(" and ")} 
      order by pa.createdAt desc
    """.trimIndent()

    val query = entityManager.createQuery(jpql, ProposedAccommodationEntity::class.java)
    params.forEach { (k, v) -> query.setParameter(k, v) }

    return query.resultList
  }

  fun findDutyToRefers(caseId: UUID, startDate: Instant?, endDate: Instant?): List<DutyToReferEntity> {
    val clauses = mutableListOf("dtr.caseId = :caseId")
    val params = mutableMapOf<String, Any>("caseId" to caseId)

    startDate?.let {
      clauses.add("dtr.createdAt >= :startDate")
      params["startDate"] = it
    }
    endDate?.let {
      clauses.add("dtr.createdAt <= :endDate")
      params["endDate"] = it
    }

    val jpql = """
      select dtr from DutyToReferEntity dtr 
      where ${clauses.joinToString(" and ")} 
      order by dtr.createdAt desc
    """.trimIndent()

    val query = entityManager.createQuery(jpql, DutyToReferEntity::class.java)
    params.forEach { (k, v) -> query.setParameter(k, v) }

    return query.resultList
  }

  fun findAllUsers(): List<UserEntity> = entityManager.createQuery("select u from UserEntity u", UserEntity::class.java).resultList

  fun findAllAccommodationTypes(): List<AccommodationTypeEntity> = entityManager.createQuery("select at from AccommodationTypeEntity at", AccommodationTypeEntity::class.java).resultList

  fun findAllLocalAuthorityAreas(): List<LocalAuthorityAreaEntity> = entityManager.createQuery("select laa from LocalAuthorityAreaEntity laa", LocalAuthorityAreaEntity::class.java).resultList
}
