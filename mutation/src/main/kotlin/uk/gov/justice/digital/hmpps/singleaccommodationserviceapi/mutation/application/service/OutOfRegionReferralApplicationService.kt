package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NoteCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OorCommand
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutOfRegionReferralDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.orThrowNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.OutOfRegionReferralRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.security.UserService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OutOfRegionReferralMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.OutOfRegionReferralMapper.merge
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.OutOfRegionReferralAggregate
import java.util.UUID

@Service
class OutOfRegionReferralApplicationService(
  private val outOfRegionReferralRepository: OutOfRegionReferralRepository,
  private val userService: UserService,
  private val caseRepository: CaseRepository,
) {
  @Transactional
  fun createOutOfRegionReferral(crn: String, command: OorCommand): OutOfRegionReferralDto {
    val user = userService.authorizeAndRetrieveUser()
    val case = caseRepository.findByCrn(crn).orThrowNotFound("crn" to crn)
    val aggregate = OutOfRegionReferralAggregate.hydrateNew(caseId = case.id)
    aggregate.updateOutOfRegionReferral(
      submissionDate = command.submissionDate,
      referenceNumber = command.referenceNumber,
      status = command.status,
      submissionNote = command.submissionNote,
    )
    val persistedRecord = outOfRegionReferralRepository.save(
      OutOfRegionReferralMapper.toEntity(aggregate.snapshot()),
    )
    return OutOfRegionReferralMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = user.displayName(),
      createdAt = persistedRecord.createdAt!!,
    )
  }

  @Transactional
  fun updateOutOfRegionReferral(crn: String, id: UUID, command: OorCommand): OutOfRegionReferralDto {
    val oor = outOfRegionReferralRepository.findByIdAndCrn(id, crn)
      .orThrowNotFound("id" to id, "crn" to crn)
    val createdByUser = userService.findUserByUserId(oor.createdByUserId!!)
      .orThrowNotFound("id" to oor.createdByUserId!!)

    val aggregate = OutOfRegionReferralMapper.toAggregate(oor).also {
      it.updateOutOfRegionReferral(
        submissionDate = command.submissionDate,
        referenceNumber = command.referenceNumber,
        status = command.status,
        outcomeReason = command.outcomeReason,
        outcomeNote = command.outcomeNote,
        submissionNote = command.submissionNote,
      )
    }
    val updatedRecord = outOfRegionReferralRepository.save(merge(aggregate.snapshot(), oor))

    return OutOfRegionReferralMapper.toDto(
      snapshot = aggregate.snapshot(),
      crn = crn,
      createdBy = createdByUser.displayName(),
      createdAt = updatedRecord.createdAt!!,
    )
  }

  @Transactional
  fun createOutOfRegionReferralNote(crn: String, id: UUID, noteCommand: NoteCommand) {
    val entity = outOfRegionReferralRepository.findByIdAndCrn(id, crn).orThrowNotFound("id" to id, "crn" to crn)
    val aggregate = OutOfRegionReferralMapper.toAggregate(entity)
    aggregate.addNote(note = noteCommand.note)
    val merged = merge(aggregate.snapshot(), entity)
    outOfRegionReferralRepository.save(merged)
  }
}
