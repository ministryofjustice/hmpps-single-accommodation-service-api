package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.sar

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.sar.SasSubjectAccessRequestRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Service
@Transactional(readOnly = true)
class SubjectAccessRequestService(
  val jsonMapper: JsonMapper,
  val sasSubjectAccessRequestRepository: SasSubjectAccessRequestRepository,
) : HmppsPrisonProbationSubjectAccessRequestService {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  override fun getContentFor(
    prn: String?,
    crn: String?,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val sarResult = getSarResult(
      crn,
      prn,
      fromDate?.atStartOfDay(),
      toDate?.atTime(LocalTime.MAX),
    ) ?: return null

    return HmppsSubjectAccessRequestContent(content = sarResult)
  }

  fun getSarResult(
    crn: String?,
    nomsNumber: String?,
    startDate: LocalDateTime?,
    endDate: LocalDateTime?,
  ): Map<String, Any>? {
    val caseIdentifier = sasSubjectAccessRequestRepository.findCaseIdentifier(crn, nomsNumber) ?: return null
    val caseId = caseIdentifier.caseEntity.id
    val personCrn = caseIdentifier.caseEntity.latestCrn()

    val startInstant = startDate?.toInstant(ZoneOffset.UTC)
    val endInstant = endDate?.toInstant(ZoneOffset.UTC)

    val accommodations = sasSubjectAccessRequestRepository.findProposedAccommodations(caseId, startInstant, endInstant)
    val dutyToRefers = sasSubjectAccessRequestRepository.findDutyToRefers(caseId, startInstant, endInstant)

    if (accommodations.isEmpty()) return null

    val users = sasSubjectAccessRequestRepository.findAllUsers().associateBy { it.id }
    val accTypes = sasSubjectAccessRequestRepository.findAllAccommodationTypes().associateBy { it.id }
    val laas = sasSubjectAccessRequestRepository.findAllLocalAuthorityAreas().associateBy { it.id }

    val nestedAccommodations = accommodations.map { pa ->
      val type = accTypes[pa.accommodationTypeId]
      val createdByUser = users[pa.createdByUserId]
      val lastUpdatedByUser = users[pa.lastUpdatedByUserId]

      val paDto = ProposedAccommodationDto(
        id = pa.id,
        crn = personCrn,
        name = pa.name,
        accommodationType = AccommodationTypeDto(
          code = type?.code ?: "UNKNOWN",
          description = type?.name ?: "Unknown",
        ),
        verificationStatus = pa.verificationStatus?.let { VerificationStatus.valueOf(it.name) },
        nextAccommodationStatus = pa.nextAccommodationStatus?.let { NextAccommodationStatus.valueOf(it.name) },
        address = AccommodationAddressDetails(
          postcode = pa.postcode,
          subBuildingName = pa.subBuildingName,
          buildingName = pa.buildingName,
          buildingNumber = pa.buildingNumber,
          thoroughfareName = pa.throughfareName,
          dependentLocality = pa.dependentLocality,
          postTown = pa.postTown,
          county = pa.county,
          country = pa.country,
          uprn = pa.uprn,
        ),
        startDate = pa.startDate,
        endDate = pa.endDate,
        createdBy = createdByUser?.displayName() ?: "Unknown",
        createdAt = pa.createdAt ?: Instant.now(),
      )

      val paMap = jsonMapper.convertValue(paDto, Map::class.java).toMutableMap()
      paMap["lastUpdatedBy"] = lastUpdatedByUser?.displayName() ?: "Unknown"
      paMap["lastUpdatedAt"] = pa.lastUpdatedAt
      paMap["settledType"] = type?.settledType

      paMap["duty_to_refer"] = dutyToRefers.map { dtr ->
        val dtrCreatedByUser = users[dtr.createdByUserId]
        val dtrLastUpdatedByUser = users[dtr.lastUpdatedByUserId]
        val laa = laas[dtr.localAuthorityAreaId]

        val dtrDto = DutyToReferDto(
          caseId = dtr.caseId,
          crn = personCrn,
          status = DtrStatus.valueOf(dtr.status.name),
          submission = DtrSubmissionDto(
            id = dtr.id,
            localAuthority = LocalAuthorityDto(
              localAuthorityAreaId = dtr.localAuthorityAreaId,
              localAuthorityAreaName = laa?.name,
            ),
            referenceNumber = dtr.referenceNumber,
            submissionDate = dtr.submissionDate,
            createdBy = dtrCreatedByUser?.displayName() ?: "Unknown",
            createdAt = dtr.createdAt ?: Instant.now(),
            withdrawalReason = dtr.withdrawalReason?.let { WithdrawalReason.valueOf(it.name) },
            withdrawalReasonOther = dtr.withdrawalReasonOther,
            outcomeReason = dtr.outcomeReason?.let { OutcomeReason.valueOf(it.name) },
            submissionNote = dtr.submissionNote,
            outcomeNote = dtr.outcomeNote,
          ),
        )
        val dtrMap = jsonMapper.convertValue(dtrDto, Map::class.java).toMutableMap()
        dtrMap["lastUpdatedBy"] = dtrLastUpdatedByUser?.displayName() ?: "Unknown"
        dtrMap["lastUpdatedAt"] = dtr.lastUpdatedAt
        dtrMap
      }

      paMap["accommodation_notes"] = pa.notes.map { note ->
        mapOf(
          "note" to note.note,
          "createdAt" to note.createdAt,
          "createdBy" to (users[note.createdByUserId]?.displayName() ?: "Unknown"),
          "lastUpdatedAt" to note.lastUpdatedAt,
          "lastUpdatedBy" to (users[note.lastUpdatedByUserId]?.displayName() ?: "Unknown"),
        )
      }
      paMap
    }

    return mapOf("ProposedAccommodations" to nestedAccommodations)
  }
}
