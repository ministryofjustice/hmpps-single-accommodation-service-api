package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.proposedaccommodation

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationDetail
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AuditRecordDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.audit.AuditService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import java.util.UUID

@Service
class ProposedAccommodationQueryService(
  private val auditService: AuditService,
  private val userRepository: UserRepository,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val caseRepository: CaseRepository,
) {
  fun getProposedAccommodations(crn: String): List<AccommodationDetail> = proposedAccommodationRepository.findAllByCrnOrderByCreatedAtDesc(crn).map {
    val createdByUser = userRepository.findByIdOrNull(it.createdByUserId!!)
      .orThrowNotFound("createdByUserId" to it.createdByUserId!!)
    ProposedAccommodationTransformer.toAccommodationDetail(it, crn, createdByUser.name)
  }

  fun getProposedAccommodation(crn: String, id: UUID): AccommodationDetail {
    val entity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    return ProposedAccommodationTransformer.toAccommodationDetail(entity, crn, createdByUser!!.name)
  }

  fun getProposedAccommodation(id: UUID): AccommodationDetail {
    val entity = proposedAccommodationRepository.findByIdOrNull(id).orThrowNotFound("id" to id)
    val case = caseRepository.findByIdOrNull(entity.caseId).orThrowNotFound("id" to entity.id)
    val createdByUser = userRepository.findByIdOrNull(entity.createdByUserId!!)
    return ProposedAccommodationTransformer.toAccommodationDetail(entity, case.latestCrn(), createdByUser!!.name)
  }

  fun getProposedAccommodationTimeline(id: UUID, crn: String): List<AuditRecordDto> {
    val entity = proposedAccommodationRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    return auditService.fullAuditHistory(id = entity.id, ProposedAccommodationEntity::class.java)
  }
}
