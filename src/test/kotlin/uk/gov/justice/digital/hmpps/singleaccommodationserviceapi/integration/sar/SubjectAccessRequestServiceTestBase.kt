package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.CaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DutyToReferNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.ProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.sar.SubjectAccessRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.ZoneOffset

open class SubjectAccessRequestServiceTestBase : IntegrationTestBase() {
  @Autowired
  lateinit var sarService: SubjectAccessRequestService

  @Autowired
  lateinit var caseRepository: CaseRepository

  @Autowired
  lateinit var dutyToReferRepository: DutyToReferRepository

  @Autowired
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @Autowired
  lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @Autowired
  lateinit var accommodationStatusRepository: AccommodationStatusRepository

  @Autowired
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  companion object {
    val START_DATE: LocalDateTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
    val END_DATE: LocalDateTime = LocalDateTime.of(2027, 1, 1, 0, 0, 0)
  }

  protected fun assertJsonEquals(expected: String, actual: String?) {
    JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT)
  }

  protected fun caseJson(case: CaseEntity) =
    """
      {
        "id": "${case.id}",
        "tier_score": "${case.tierScore}",
        "cas1_application_id": ${case.cas1ApplicationId?.let { "\"$it\"" } ?: "null"},
        "cas1_application_application_status": ${case.cas1ApplicationApplicationStatus?.let { "\"$it\"" } ?: "null"},
        "cas1_application_placement_status": ${case.cas1ApplicationPlacementStatus?.let { "\"$it\"" } ?: "null"},
        "cas1_application_request_for_placement_status": ${case.cas1ApplicationRequestForPlacementStatus?.let { "\"$it\"" } ?: "null"}
      }
    """.trimIndent()

  protected fun caseIdentifierJson(identifier: String, type: String, createdAt: String) =
    """
      {
        "identifier": "$identifier",
        "identifier_type": "$type",
        "created_at": "$createdAt"
      }
    """.trimIndent()

  protected fun dutyToReferJson(dtr: DutyToReferEntity, localAuthorityAreaName: String) =
    """
      {
        "id": "${dtr.id}",
        "reference_number": "${dtr.referenceNumber}",
        "submission_date": "${dtr.submissionDate}",
        "status": "${dtr.status}",
        "local_authority_area_name": "$localAuthorityAreaName",
        "created_at": "${dtr.createdAt?.atOffset(ZoneOffset.UTC)?.toLocalDateTime()}",
        "last_updated_at": "${dtr.lastUpdatedAt?.atOffset(ZoneOffset.UTC)?.toLocalDateTime()}"
      }
    """.trimIndent()

  protected fun dutyToReferNoteJson(note: DutyToReferNoteEntity) =
    """
      {
        "note": "${note.note}",
        "created_at": "${note.createdAt?.atOffset(ZoneOffset.UTC)?.toLocalDateTime()}"
      }
    """.trimIndent()

  protected fun proposedAccommodationJson(pa: ProposedAccommodationEntity, arrangementType: String, status: String) =
    """
      {
        "name": "${pa.name}",
        "arrangement_type": "$arrangementType",
        "status": "$status",
        "start_date": ${pa.startDate?.let { "\"$it\"" } ?: "null"},
        "end_date": ${pa.endDate?.let { "\"$it\"" } ?: "null"},
        "postcode": "${pa.postcode}",
        "sub_building_name": ${pa.subBuildingName?.let { "\"$it\"" } ?: "null"},
        "building_name": ${pa.buildingName?.let { "\"$it\"" } ?: "null"},
        "building_number": "${pa.buildingNumber}",
        "throughfare_name": "${pa.thoroughfareName}",
        "dependent_locality": ${pa.dependentLocality?.let { "\"$it\"" } ?: "null"},
        "post_town": "${pa.postTown}",
        "county": ${pa.county?.let { "\"$it\"" } ?: "null"},
        "country": "${pa.country}",
        "uprn": ${pa.uprn?.let { "\"$it\"" } ?: "null"},
        "created_at": "${pa.createdAt?.atOffset(ZoneOffset.UTC)?.toLocalDateTime()}",
        "last_updated_at": "${pa.lastUpdatedAt?.atOffset(ZoneOffset.UTC)?.toLocalDateTime()}"
      }
    """.trimIndent()

  protected fun proposedAccommodationNoteJson(note: ProposedAccommodationNoteEntity) =
    """
      {
        "note": "${note.note}",
        "created_at": "${note.createdAt?.atOffset(ZoneOffset.UTC)?.toLocalDateTime()}"
      }
    """.trimIndent()
}
