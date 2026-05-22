package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.sar

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.sar.SasSubjectAccessRequestRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
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

    return HmppsSubjectAccessRequestContent(
      content = jsonMapper.readValue(sarResult, Map::class.java),
    )
  }

  fun getSarResult(
    crn: String?,
    nomsNumber: String?,
    startDate: LocalDateTime?,
    endDate: LocalDateTime?,
  ): String? {
    val cases = sasSubjectAccessRequestRepository.getCasesJson(crn, nomsNumber, startDate, endDate)
    val caseIdentifiers = sasSubjectAccessRequestRepository.getCaseIdentifiersJson(crn, nomsNumber, startDate, endDate)
    val dutyToRefers = sasSubjectAccessRequestRepository.getDutyToReferJson(crn, nomsNumber, startDate, endDate)
    val dutyToReferNotes = sasSubjectAccessRequestRepository.getDutyToReferNotesJson(crn, nomsNumber, startDate, endDate)
    val proposedAccommodations = sasSubjectAccessRequestRepository.getProposedAccommodationJson(crn, nomsNumber, startDate, endDate)
    val proposedAccommodationNotes = sasSubjectAccessRequestRepository.getProposedAccommodationNotesJson(crn, nomsNumber, startDate, endDate)

    if (listOf(cases, dutyToRefers, proposedAccommodations).all { it == null }) return null

    val result = """
      {
        "Cases": ${cases ?: "[]"},
        "CaseIdentifiers": ${caseIdentifiers ?: "[]"},
        "DutyToRefers": ${dutyToRefers ?: "[]"},
        "DutyToReferNotes": ${dutyToReferNotes ?: "[]"},
        "ProposedAccommodations": ${proposedAccommodations ?: "[]"},
        "ProposedAccommodationNotes": ${proposedAccommodationNotes ?: "[]"}
      }
    """.trimIndent()

    log.logDebugMessage(result)
    return result
  }

  private fun Logger.logDebugMessage(result: String) {
    if (isDebugEnabled) {
      val pretty = jsonMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(jsonMapper.readValue(result, Any::class.java))
      debug("SAR Result: $pretty")
    }
  }
}
