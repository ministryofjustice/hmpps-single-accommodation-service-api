package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.sar

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationAddressDetails
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationTypeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DtrSubmissionDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.DutyToReferDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.LocalAuthorityDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.NextAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.OutcomeReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ProposedAccommodationDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TitleEnum
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TitleEnumSerialiser
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.VerificationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.WithdrawalReason
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Service
@Transactional(readOnly = true)
class SubjectAccessRequestService(
  private val caseRepository: CaseRepository,
  private val proposedAccommodationRepository: ProposedAccommodationRepository,
  private val dutyToReferRepository: DutyToReferRepository,
  private val userRepository: UserRepository,
  private val accommodationTypeRepository: AccommodationTypeRepository,
  private val localAuthorityAreaRepository: LocalAuthorityAreaRepository,
) : HmppsPrisonProbationSubjectAccessRequestService {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val enumModule: SimpleModule = SimpleModule()
      .addSerializer(TitleEnum::class.java, TitleEnumSerialiser())
    private val mapper: JsonMapper = JsonMapper.builder()
      .addModule(enumModule)
      .build()
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
      fromDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant() ?: Instant.MIN,
      toDate?.plusDays(1).atStartOfDay(ZoneId.systemDefault())?.toInstant() ?: Instant.MAX,
    ) ?: return null

    return HmppsSubjectAccessRequestContent(content = sarResult)
  }

  fun getSarResult(
    crn: String?,
    prisonNumber: String?,
    startDate: Instant,
    endDate: Instant,
  ): Map<String, Any>? {
    if (crn == null && prisonNumber == null) return null

    val caseEntity = caseRepository.findByIdentifiers(
      crn?.let { listOf(it) },
      prisonNumber?.let { listOf(it) },
    ) ?: return null

    val caseId = caseEntity.id
    val personCrn = caseEntity.latestCrn()

    val accommodations = proposedAccommodationRepository.findAllForSar(caseId, startDate, endDate)
    val dutyToRefers = dutyToReferRepository.findAllForSar(caseId, startDate, endDate)

    if (accommodations.isEmpty() && dutyToRefers.isEmpty()) return null

    val users = userRepository.findAll().associateBy { it.id }
    val accTypes = accommodationTypeRepository.findAll().associateBy { it.id }
    val laas = localAuthorityAreaRepository.findAll().associateBy { it.id }

    val nestedDutyToRefers = dutyToRefers.map { dtr ->
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
          createdAt = dtr.createdAt!!,
          withdrawalReason = dtr.withdrawalReason?.let { WithdrawalReason.valueOf(it.name) },
          withdrawalReasonOther = dtr.withdrawalReasonOther,
          outcomeReason = dtr.outcomeReason?.let { OutcomeReason.valueOf(it.name) },
          submissionNote = dtr.submissionNote,
          outcomeNote = dtr.outcomeNote,
        ),
      )
      val dtrMap = mapper.convertValue(dtrDto, Map::class.java).toMutableMap()
      dtrMap["lastUpdatedBy"] = dtrLastUpdatedByUser?.displayName() ?: "Unknown"
      dtrMap["lastUpdatedAt"] = dtr.lastUpdatedAt
      dtrMap
    }

    val nestedAccommodations = accommodations.map { pa ->
      val type = accTypes[pa.accommodationTypeId]
      val createdByUser = users[pa.createdByUserId]
      val lastUpdatedByUser = users[pa.lastUpdatedByUserId]

      val paDto = ProposedAccommodationDto(
        id = pa.id,
        crn = personCrn,
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
          thoroughfareName = pa.thoroughfareName,
          dependentLocality = pa.dependentLocality,
          postTown = pa.postTown,
          county = pa.county,
          country = pa.country,
          uprn = pa.uprn,
        ),
        createdBy = createdByUser?.displayName() ?: "Unknown",
        createdAt = pa.createdAt!!,
      )

      val paMap = mapper.convertValue(paDto, Map::class.java).toMutableMap()
      paMap["lastUpdatedBy"] = lastUpdatedByUser?.displayName() ?: "Unknown"
      paMap["lastUpdatedAt"] = pa.lastUpdatedAt
      paMap["settledType"] = when (type?.settledType) {
        AccommodationSettledType.SETTLED -> "Settled"
        AccommodationSettledType.TRANSIENT -> "Transient"
        null -> null
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

    return mapOf(
      "ProposedAccommodations" to nestedAccommodations,
      "DutyToRefer" to nestedDutyToRefers,
    )
  }
}
