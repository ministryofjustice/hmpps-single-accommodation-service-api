package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.unit.sar

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildAccommodationTypeEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildLocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildProposedAccommodationNoteEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildUserEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.AccommodationSettledType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.UserRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.sar.SubjectAccessRequestService
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@ExtendWith(MockKExtension::class)
@Suppress("UNCHECKED_CAST")
class SubjectAccessRequestServiceTest {

  @MockK
  lateinit var caseRepository: CaseRepository

  @MockK
  lateinit var proposedAccommodationRepository: ProposedAccommodationRepository

  @MockK
  lateinit var dutyToReferRepository: DutyToReferRepository

  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var accommodationTypeRepository: AccommodationTypeRepository

  @MockK
  lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @InjectMockKs
  lateinit var subjectAccessRequestService: SubjectAccessRequestService

  @Nested
  inner class UnknownUserFallback {

    @Test
    fun `defaults createdBy and lastUpdatedBy to Unknown when user ids are not found in userRepository`() {
      val crn = "X123456"
      val caseEntity = buildCaseEntity { withCrn(crn) }
      val unknownUser1Id = UUID.randomUUID()
      val unknownUser2Id = UUID.randomUUID()
      val unknownUser3Id = UUID.randomUUID()
      val unknownUser4Id = UUID.randomUUID()
      val unknownUser5Id = UUID.randomUUID()
      val unknownUser6Id = UUID.randomUUID()

      val localAuthority = buildLocalAuthorityAreaEntity(name = "Test Local Authority")
      val accommodationType = buildAccommodationTypeEntity(
        code = "A07B",
        name = "Settled Accommodation",
        settledType = AccommodationSettledType.SETTLED,
      )

      val dutyToRefer = buildDutyToReferEntity(
        caseId = caseEntity.id,
        localAuthorityAreaId = localAuthority.id,
        createdByUserId = unknownUser1Id,
        lastUpdatedByUserId = unknownUser2Id,
      )

      val proposedAccommodation = buildProposedAccommodationEntity(
        caseId = caseEntity.id,
        accommodationTypeEntity = accommodationType,
        createdByUserId = unknownUser3Id,
        lastUpdatedByUserId = unknownUser4Id,
      )

      val note = buildProposedAccommodationNoteEntity(
        note = "Sample Note",
        createdByUserId = unknownUser5Id,
        proposedAccommodationEntity = proposedAccommodation,
      ).apply {
        this.lastUpdatedByUserId = unknownUser6Id
      }
      proposedAccommodation.notes.add(note)

      every {
        caseRepository.findByIdentifiers(listOf(crn), null)
      } returns caseEntity

      every {
        proposedAccommodationRepository.findAllForSar(caseEntity.id, any(), any())
      } returns listOf(proposedAccommodation)

      every {
        dutyToReferRepository.findAllForSar(caseEntity.id, any(), any())
      } returns listOf(dutyToRefer)

      every { userRepository.findAll() } returns emptyList()
      every { accommodationTypeRepository.findAll() } returns listOf(accommodationType)
      every { localAuthorityAreaRepository.findAll() } returns listOf(localAuthority)

      val result = subjectAccessRequestService.getSarResult(
        crn = crn,
        prisonNumber = null,
        startDate = Instant.MIN,
        endDate = Instant.MAX,
      )

      assertThat(result).isNotNull
      val dtrs = result!!["DutyToRefer"] as List<Map<String, Any?>>
      assertThat(dtrs).hasSize(1)
      val dtr = dtrs[0]
      val submission = dtr["submission"] as Map<String, Any?>
      assertThat(submission["createdBy"]).isEqualTo("Unknown")
      assertThat(dtr["lastUpdatedBy"]).isEqualTo("Unknown")

      val pas = result["ProposedAccommodations"] as List<Map<String, Any?>>
      assertThat(pas).hasSize(1)
      val pa = pas[0]
      assertThat(pa["createdBy"]).isEqualTo("Unknown")
      assertThat(pa["lastUpdatedBy"]).isEqualTo("Unknown")

      val notes = pa["accommodation_notes"] as List<Map<String, Any?>>
      assertThat(notes).hasSize(1)
      val noteMap = notes[0]
      assertThat(noteMap["createdBy"]).isEqualTo("Unknown")
      assertThat(noteMap["lastUpdatedBy"]).isEqualTo("Unknown")
    }

    @Test
    fun `defaults createdBy and lastUpdatedBy to Unknown when user ids are null`() {
      val crn = "X123456"
      val caseEntity = buildCaseEntity { withCrn(crn) }

      val localAuthority = buildLocalAuthorityAreaEntity(name = "Test Local Authority")
      val accommodationType = buildAccommodationTypeEntity(
        code = "A07B",
        name = "Settled Accommodation",
        settledType = AccommodationSettledType.SETTLED,
      )

      val dutyToRefer = buildDutyToReferEntity(
        caseId = caseEntity.id,
        localAuthorityAreaId = localAuthority.id,
        createdByUserId = null,
        lastUpdatedByUserId = null,
      )

      val proposedAccommodation = buildProposedAccommodationEntity(
        caseId = caseEntity.id,
        accommodationTypeEntity = accommodationType,
        createdByUserId = null,
        lastUpdatedByUserId = null,
      )

      val note = buildProposedAccommodationNoteEntity(
        note = "Sample Note",
        createdByUserId = null,
        proposedAccommodationEntity = proposedAccommodation,
      ).apply {
        this.lastUpdatedByUserId = null
      }
      proposedAccommodation.notes.add(note)

      every {
        caseRepository.findByIdentifiers(listOf(crn), null)
      } returns caseEntity

      every {
        proposedAccommodationRepository.findAllForSar(caseEntity.id, any(), any())
      } returns listOf(proposedAccommodation)

      every {
        dutyToReferRepository.findAllForSar(caseEntity.id, any(), any())
      } returns listOf(dutyToRefer)

      every { userRepository.findAll() } returns emptyList()
      every { accommodationTypeRepository.findAll() } returns listOf(accommodationType)
      every { localAuthorityAreaRepository.findAll() } returns listOf(localAuthority)

      val result = subjectAccessRequestService.getSarResult(
        crn = crn,
        prisonNumber = null,
        startDate = Instant.MIN,
        endDate = Instant.MAX,
      )

      assertThat(result).isNotNull
      val dtrs = result!!["DutyToRefer"] as List<Map<String, Any?>>
      val submission = dtrs[0]["submission"] as Map<String, Any?>
      assertThat(submission["createdBy"]).isEqualTo("Unknown")
      assertThat(dtrs[0]["lastUpdatedBy"]).isEqualTo("Unknown")

      val pas = result["ProposedAccommodations"] as List<Map<String, Any?>>
      assertThat(pas[0]["createdBy"]).isEqualTo("Unknown")
      assertThat(pas[0]["lastUpdatedBy"]).isEqualTo("Unknown")

      val notes = pas[0]["accommodation_notes"] as List<Map<String, Any?>>
      assertThat(notes[0]["createdBy"]).isEqualTo("Unknown")
      assertThat(notes[0]["lastUpdatedBy"]).isEqualTo("Unknown")
    }
  }

  @Nested
  inner class KnownUserMapping {

    @Test
    fun `maps usernames correctly when user ids match existing users in userRepository`() {
      val crn = "X123456"
      val caseEntity = buildCaseEntity { withCrn(crn) }

      val dtrCreator = buildUserEntity(username = "dtr.creator")
      val dtrUpdater = buildUserEntity(username = "dtr.updater")
      val paCreator = buildUserEntity(username = "pa.creator")
      val paUpdater = buildUserEntity(username = "pa.updater")
      val noteCreator = buildUserEntity(username = "note.creator")
      val noteUpdater = buildUserEntity(username = "note.updater")

      val localAuthority = buildLocalAuthorityAreaEntity(name = "Test Local Authority")
      val accommodationType = buildAccommodationTypeEntity(
        code = "A07B",
        name = "Settled Accommodation",
        settledType = AccommodationSettledType.SETTLED,
      )

      val dutyToRefer = buildDutyToReferEntity(
        caseId = caseEntity.id,
        localAuthorityAreaId = localAuthority.id,
        createdByUserId = dtrCreator.id,
        lastUpdatedByUserId = dtrUpdater.id,
      )

      val proposedAccommodation = buildProposedAccommodationEntity(
        caseId = caseEntity.id,
        accommodationTypeEntity = accommodationType,
        createdByUserId = paCreator.id,
        lastUpdatedByUserId = paUpdater.id,
      )

      val note = buildProposedAccommodationNoteEntity(
        note = "Sample Note",
        createdByUserId = noteCreator.id,
        proposedAccommodationEntity = proposedAccommodation,
      ).apply {
        this.lastUpdatedByUserId = noteUpdater.id
      }
      proposedAccommodation.notes.add(note)

      every {
        caseRepository.findByIdentifiers(listOf(crn), null)
      } returns caseEntity

      every {
        proposedAccommodationRepository.findAllForSar(caseEntity.id, any(), any())
      } returns listOf(proposedAccommodation)

      every {
        dutyToReferRepository.findAllForSar(caseEntity.id, any(), any())
      } returns listOf(dutyToRefer)

      every { userRepository.findAll() } returns listOf(
        dtrCreator,
        dtrUpdater,
        paCreator,
        paUpdater,
        noteCreator,
        noteUpdater,
      )
      every { accommodationTypeRepository.findAll() } returns listOf(accommodationType)
      every { localAuthorityAreaRepository.findAll() } returns listOf(localAuthority)

      val result = subjectAccessRequestService.getSarResult(
        crn = crn,
        prisonNumber = null,
        startDate = Instant.MIN,
        endDate = Instant.MAX,
      )

      assertThat(result).isNotNull
      val dtrs = result!!["DutyToRefer"] as List<Map<String, Any?>>
      val submission = dtrs[0]["submission"] as Map<String, Any?>
      assertThat(submission["createdBy"]).isEqualTo("dtr.creator")
      assertThat(dtrs[0]["lastUpdatedBy"]).isEqualTo("dtr.updater")

      val pas = result["ProposedAccommodations"] as List<Map<String, Any?>>
      assertThat(pas[0]["createdBy"]).isEqualTo("pa.creator")
      assertThat(pas[0]["lastUpdatedBy"]).isEqualTo("pa.updater")

      val notes = pas[0]["accommodation_notes"] as List<Map<String, Any?>>
      assertThat(notes[0]["createdBy"]).isEqualTo("note.creator")
      assertThat(notes[0]["lastUpdatedBy"]).isEqualTo("note.updater")
    }
  }

  @Nested
  inner class EdgeCases {

    @Test
    fun `returns null when both crn and prisonNumber are null`() {
      val result = subjectAccessRequestService.getSarResult(
        crn = null,
        prisonNumber = null,
        startDate = Instant.MIN,
        endDate = Instant.MAX,
      )

      assertThat(result).isNull()
      verify(exactly = 0) { caseRepository.findByIdentifiers(any(), any()) }
    }

    @Test
    fun `returns null when case is not found in caseRepository`() {
      val crn = "X999999"
      every { caseRepository.findByIdentifiers(listOf(crn), null) } returns null

      val result = subjectAccessRequestService.getSarResult(
        crn = crn,
        prisonNumber = null,
        startDate = Instant.MIN,
        endDate = Instant.MAX,
      )

      assertThat(result).isNull()
    }

    @Test
    fun `returns null when both accommodations and dutyToRefers are empty`() {
      val crn = "X123456"
      val caseEntity = buildCaseEntity { withCrn(crn) }

      every { caseRepository.findByIdentifiers(listOf(crn), null) } returns caseEntity
      every { proposedAccommodationRepository.findAllForSar(caseEntity.id, any(), any()) } returns emptyList()
      every { dutyToReferRepository.findAllForSar(caseEntity.id, any(), any()) } returns emptyList()

      val result = subjectAccessRequestService.getSarResult(
        crn = crn,
        prisonNumber = null,
        startDate = Instant.MIN,
        endDate = Instant.MAX,
      )

      assertThat(result).isNull()
      verify(exactly = 0) { userRepository.findAll() }
    }

    @Test
    fun `handles missing accommodationType and localAuthorityArea entities gracefully`() {
      val crn = "X123456"
      val caseEntity = buildCaseEntity { withCrn(crn) }
      val unknownTypeId = UUID.randomUUID()
      val unknownLaaId = UUID.randomUUID()

      val dutyToRefer = buildDutyToReferEntity(
        caseId = caseEntity.id,
        localAuthorityAreaId = unknownLaaId,
      )

      val proposedAccommodation = buildProposedAccommodationEntity(
        caseId = caseEntity.id,
        accommodationTypeEntity = null,
      ).apply {
        this.accommodationTypeId = unknownTypeId
      }

      every { caseRepository.findByIdentifiers(listOf(crn), null) } returns caseEntity
      every { proposedAccommodationRepository.findAllForSar(caseEntity.id, any(), any()) } returns listOf(proposedAccommodation)
      every { dutyToReferRepository.findAllForSar(caseEntity.id, any(), any()) } returns listOf(dutyToRefer)
      every { userRepository.findAll() } returns emptyList()
      every { accommodationTypeRepository.findAll() } returns emptyList()
      every { localAuthorityAreaRepository.findAll() } returns emptyList()

      val result = subjectAccessRequestService.getSarResult(
        crn = crn,
        prisonNumber = null,
        startDate = Instant.MIN,
        endDate = Instant.MAX,
      )

      assertThat(result).isNotNull
      val dtrs = result!!["DutyToRefer"] as List<Map<String, Any?>>
      val submission = dtrs[0]["submission"] as Map<String, Any?>
      val localAuthority = submission["localAuthority"] as Map<String, Any?>
      assertThat(localAuthority["localAuthorityAreaName"]).isNull()

      val pas = result["ProposedAccommodations"] as List<Map<String, Any?>>
      val accommodationType = pas[0]["accommodationType"] as Map<String, Any?>
      assertThat(accommodationType["code"]).isEqualTo("UNKNOWN")
      assertThat(accommodationType["description"]).isEqualTo("Unknown")
      assertThat(pas[0]["settledType"]).isNull()
    }

    @Test
    fun `getContentFor wraps getSarResult in HmppsSubjectAccessRequestContent and maps date ranges correctly`() {
      val crn = "X123456"
      val caseEntity = buildCaseEntity { withCrn(crn) }
      val fromDate = LocalDate.of(2026, 1, 1)
      val toDate = LocalDate.of(2026, 1, 31)

      val expectedStartDate = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
      val expectedEndDate = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

      val dutyToRefer = buildDutyToReferEntity(caseId = caseEntity.id)

      every { caseRepository.findByIdentifiers(listOf(crn), null) } returns caseEntity
      every { proposedAccommodationRepository.findAllForSar(caseEntity.id, expectedStartDate, expectedEndDate) } returns emptyList()
      every { dutyToReferRepository.findAllForSar(caseEntity.id, expectedStartDate, expectedEndDate) } returns listOf(dutyToRefer)
      every { userRepository.findAll() } returns emptyList()
      every { accommodationTypeRepository.findAll() } returns emptyList()
      every { localAuthorityAreaRepository.findAll() } returns emptyList()

      val content = subjectAccessRequestService.getContentFor(
        prn = null,
        crn = crn,
        fromDate = fromDate,
        toDate = toDate,
      )

      assertThat(content).isNotNull
      assertThat(content!!.content).isInstanceOf(Map::class.java)

      verify(exactly = 1) {
        dutyToReferRepository.findAllForSar(caseEntity.id, expectedStartDate, expectedEndDate)
      }
    }

    @Test
    fun `getContentFor returns null when getSarResult returns null`() {
      val crn = "X999999"
      every { caseRepository.findByIdentifiers(listOf(crn), null) } returns null

      val content = subjectAccessRequestService.getContentFor(
        prn = null,
        crn = crn,
        fromDate = null,
        toDate = null,
      )

      assertThat(content).isNull()
    }
  }
}
