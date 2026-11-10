package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.assertions.assertThatJson
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.TierScore
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1PlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1RequestForPlacementStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas1Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas2AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3ApplicationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3AssessmentStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.approvedpremises.Cas3BookingStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.commissionedrehabilitativeservices.CrsReferralStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsage
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.canonical.CanonicalAddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressStatusCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.corepersonrecord.probation.AddressUsageCode
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.prisonersearch.InOutStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.client.sasanddelius.SexualOffenceRegistrations
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCanonicalAddress
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ApplicationV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1AssessmentSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ExternalPremisesV2Dto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1ExternalRequestForPlacementOutcomeDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1PlacementV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1RequestForPlacementSummaryV2
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas1SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas2SubmittedApplicationSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Application
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3ExternalPreviousBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3ExternalPreviousBookingCancellation
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3LatestBooking
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3PremisesSummary
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3Staff
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCas3SubmittedApplication
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCaseEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCommissionedRehabilitativeServices
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildCorePersonRecord
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildDutyToReferEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildIdentifiers
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildPrisoner
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.buildTier
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withCrn
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.factories.withPrisonNumber
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.LocalAuthorityAreaEntity
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.utils.asJsonValue
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.NAME_OF_TEST_DATA_SETUP_USER
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityResponseCannotStartYet
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityResponseTierNotFound
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response.expectedGetEligibilityUpstreamFailuresResponse
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ApprovedPremisesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CommissionedRehabilitativeServicesStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.CorePersonRecordStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.HmppsAuthStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.PrisonerSearchStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.ProbationIntegrationDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.SasAndDeliusStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock.TierStubs
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.DatabaseUtils.SasTables.DUTY_TO_REFER
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.entity.DtrStatus as EntityDtrStatus

class EligibilityControllerIT : IntegrationTestBase() {
  private val crn = "FAKECRN1"
  private val prisonNumber = "PRI1"
  private val cas1ApplicationId = UUID.fromString("e6b202ce-c214-4b87-98f5-111111111111")
  private val cas2ApplicationId = UUID.fromString("e6b202ce-c214-4b87-98f7-111111111111")
  private val cas3ApplicationId = UUID.fromString("e6b202ce-c214-4b87-98f6-111111111111")
  private val dutyToReferCaseId = UUID.fromString("e6b202ce-c214-4b87-98f5-111111111112")

  private val crsSubmissionDate = LocalDate.now()

  private val cas1ApplicationUiUrl = "https://cas1-ui/applications/$cas1ApplicationId"
  private val cas2ApplicationUiUrl = "https://cas2-ui/applications/$cas2ApplicationId"
  private val cas3ReferralUiUrl = "https://cas3-ui/referrals/$cas3ApplicationId/full"

  @Value($$"${service.commissioned-rehabilitative-services-ui.base-url}")
  private lateinit var crsUrl: String

  @Autowired
  private lateinit var localAuthorityAreaRepository: LocalAuthorityAreaRepository

  @Autowired
  private lateinit var dutyToReferRepository: DutyToReferRepository

  private val localAuthorityArea: LocalAuthorityAreaEntity by lazy {
    localAuthorityAreaRepository.findAllByActiveIsTrueOrderByName().first()
  }

  @BeforeEach
  fun setup() {
    databaseUtils.truncate(DUTY_TO_REFER)

    val corePersonRecord = buildCorePersonRecord(
      identifiers = buildIdentifiers(
        crns = listOf(crn),
      ),
      addresses = listOf(
        buildCanonicalAddress(
          status = CanonicalAddressStatus(
            code = AddressStatusCode.M.name,
          ),
          endDate = LocalDate.now().plusDays(1),
          usages = listOf(
            CanonicalAddressUsage(
              usageCode = CanonicalAddressUsageCode(
                code = AddressUsageCode.A02.name,
              ),
              isActive = true,
            ),
          ),
        ),
      ),
    )

    val cas3Application = buildCas3Application(
      id = cas3ApplicationId,
      applicationStatus = Cas3ApplicationStatus.SUBMITTED,
      submittedApplication = buildCas3SubmittedApplication(
        assessmentStatus = Cas3AssessmentStatus.UNALLOCATED,
        submittedDate = LocalDate.of(2025, 1, 2),
      ),
      uiUrl = cas3ReferralUiUrl,
    )

    ApprovedPremisesStubs.getCas3SuitableApplicationOKResponse(crn = crn, response = cas3Application)

    HmppsAuthStubs.stubGrantToken()
    createTestDataSetupUserAndDeliusUser()

    CorePersonRecordStubs.getCorePersonRecordOKResponse(crn = crn, response = corePersonRecord)
    SasAndDeliusStubs.stubGetSexualOffenceRegistrations(crn, SexualOffenceRegistrations(crn, emptyList()))
    PrisonerSearchStubs.getPrisonerOKResponse(prisonNumber = prisonNumber, response = buildPrisoner(prisonNumber = prisonNumber))
    ApprovedPremisesStubs.getCas3SuitableApplicationOKResponse(crn = crn, response = cas3Application)
    ApprovedPremisesStubs.getCas1CurrentApplicationNoContentResponse(crn = crn)
    ApprovedPremisesStubs.getCas2SuitableApplicationNoContentResponse(crn = crn)
    ApprovedPremisesStubs.getCas1CurrentPremisesNoContentResponse(crn = crn)
    ApprovedPremisesStubs.getCas3CurrentPremisesNoContentResponse(crn = crn)
    CommissionedRehabilitativeServicesStubs.getCrsOkResponse(
      crn = crn,
      response = listOf(
        buildCommissionedRehabilitativeServices(
          status = CrsReferralStatus.LIVE,
          sentAt = crsSubmissionDate.atStartOfDay().atOffset(ZoneOffset.UTC),
        ),
      ),
    )
  }

  @Test
  fun `should get eligibility for crn`() {
    val tier = buildTier(TierScore.A)
    ProbationIntegrationDeliusStubs.getCaseByCrn(crn, buildCase(crn = crn, nomsNumber = prisonNumber))
    val staff = Cas1Staff(
      name = "Bob",
      username = "Bob123",
      staffCode = "123",
    )

    val cas1RequestForPlacementOutcome = buildCas1ExternalRequestForPlacementOutcomeDto(
      requestForPlacement = buildCas1RequestForPlacementSummaryV2(
        status = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED,
        decision = "accepted",
        rejectionReason = null,
        submittedBy = staff,
        submittedAt = LocalDate.now(),
        withdrawal = null,
        expectedArrivalDate = LocalDate.now(),
        durationDays = 12,
      ),
      placement = buildCas1PlacementV2(
        status = Cas1PlacementStatus.CANCELLED,
        canonicalArrivalDate = LocalDate.now(),
        canonicalDepartureDate = LocalDate.now().plusMonths(6),
        expectedArrivalDate = LocalDate.now(),
        expectedDepartureDate = LocalDate.now().plusMonths(6),
        cancellationReason = "Oops",
        premises = buildCas1ExternalPremisesV2Dto(
          addressLine1 = "Test House",
          addressLine2 = "Test Road",
          town = "Test Town",
          postcode = "Test Postcode",
        ),
      ),
    )

    val cas1Application = buildCas1ApplicationV2(
      uiUrl = cas1ApplicationUiUrl,
      id = cas1ApplicationId,
      status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      createdAt = OffsetDateTime.now(),
      createdBy = staff,
      expiresAt = LocalDate.now(),
      submittedApplication = buildCas1SubmittedApplication(
        submittedAt = OffsetDateTime.now(),
        assessment = buildCas1AssessmentSummary(
          decision = "accepted",
          rejectionRationale = null,
        ),
        requestForPlacementHistory = listOf(cas1RequestForPlacementOutcome),
      ),
    )

    val cas2Application = buildCas2Application(
      uiUrl = cas2ApplicationUiUrl,
      id = cas2ApplicationId,
      createdAt = OffsetDateTime.parse("2024-01-01T12:00:00.000Z"),
      createdBy = buildCas2Staff(
        name = "Anne",
        username = "anne.smith",
        deliusStaffCode = "ANNE",
        nomisStaffId = null,
        userType = "DELIUS",
      ),
      submittedApplication = buildCas2SubmittedApplicationSummary(
        latestAssessmentStatus = Cas2AssessmentStatus.CANCELLED,
        offerDeclinedReason = null,
        cancelledReason = "cancelled reason",
        submittedAt = OffsetDateTime.parse("2024-01-02T12:00:00.000Z"),
      ),
      cohort = "isc",
    )

    val cas3Application = buildCas3Application(
      id = cas3ApplicationId,
      applicationStatus = Cas3ApplicationStatus.SUBMITTED,
      submittedApplication = buildCas3SubmittedApplication(
        submittedDate = LocalDate.parse("2023-01-01"),
        submittedBy = buildCas3Staff(),
        assessmentStatus = Cas3AssessmentStatus.REJECTED,
        assessmentRejectionReason = "Oops",
        latestBooking = buildCas3LatestBooking(
          status = Cas3BookingStatus.CANCELLED,
          provisionalOfferSentDate = LocalDate.parse("2023-01-02"),
          cancellation = buildCas3ExternalPreviousBookingCancellation(
            cancellationDate = LocalDate.parse("2023-01-03"),
            cancellationReason = "Mistake",
          ),
          premises = buildCas3PremisesSummary(
            name = "Test Premises",
            startDate = LocalDate.parse("2023-01-04"),
            endDate = LocalDate.parse("2023-01-05"),
            addressLine1 = "123 Test Street",
            addressLine2 = "Test Road",
            town = "Test Town",
            postcode = "Test Postcode",
          ),
        ),
        previousBookings = listOf(
          buildCas3ExternalPreviousBooking(
            bookingStatus = Cas3BookingStatus.CANCELLED,
            cancellation = buildCas3ExternalPreviousBookingCancellation(
              cancellationDate = LocalDate.parse("2023-01-03"),
              cancellationReason = "Mistake",
            ),
          ),
        ),
      ),
      uiUrl = cas3ReferralUiUrl,
    )

    ApprovedPremisesStubs.getCas3SuitableApplicationOKResponse(crn = crn, response = cas3Application)
    ApprovedPremisesStubs.getCas2SuitableApplicationOKResponse(crn = crn, response = cas2Application)
    ApprovedPremisesStubs.getCas1CurrentApplicationOKResponse(crn = crn, response = cas1Application)

    TierStubs.getTierOKResponse(crn = crn, tier)

    val entity = buildCaseEntity(id = dutyToReferCaseId) {
      withCrn(crn)
      withPrisonNumber(prisonNumber)
    }
    caseRepository.save(entity)

    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = entity.id,
        localAuthorityAreaId = localAuthorityArea.id,
        referenceNumber = "DTR-REF-001",
        submissionDate = LocalDate.now().minusMonths(5),
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetEligibilityResponse(
            crn = crn,
            cas1ApplicationId = cas1ApplicationId,
            cas3ApplicationId = cas3ApplicationId,
            dutyToReferCaseId = dutyToReferCaseId,
            dutyToReferId = existingEntity.id,
            localAuthorityAreaId = localAuthorityArea.id,
            localAuthorityAreaName = localAuthorityArea.name,
            submissionDate = existingEntity.submissionDate.toString(),
            referenceNumber = "DTR-REF-001",
            createdBy = NAME_OF_TEST_DATA_SETUP_USER,
            createdAt = existingEntity.createdAt.truncatedTo(ChronoUnit.SECONDS).toString(),
            crsSubmissionDate = crsSubmissionDate.toString(),
            cas1ApplicationUrl = cas1ApplicationUiUrl,
            crsUrl = crsUrl,
            cas3ReferralUrl = cas3ReferralUiUrl,
            cas1ApplicationStartedAt = cas1Application.createdAt.asJsonValue(),
            submittedAt = cas1Application.submittedApplication?.submittedAt?.asJsonValue().toString(),
            requestSubmittedAt = cas1RequestForPlacementOutcome.requestForPlacement.submittedAt.toString(),
            expectedArrivalDate = cas1RequestForPlacementOutcome.requestForPlacement.expectedArrivalDate.toString(),
            expectedDepartureDate = cas1RequestForPlacementOutcome.placement?.expectedDepartureDate.toString(),
            expiresAt = cas1Application.expiresAt.toString(),
            startDate = cas1RequestForPlacementOutcome.placement!!.canonicalArrivalDate.toString(),
            endDate = cas1RequestForPlacementOutcome.placement!!.canonicalDepartureDate.toString(),
          ),
        )
      }
  }

  @Test
  fun `should get eligibility for cas 3 blocking reason`() {
    // eligible for CAS2 but not CAS1
    TierStubs.getTierOKResponse(crn, buildTier(tierScore = TierScore.E))
    ProbationIntegrationDeliusStubs.getCaseByCrn(crn, buildCase(crn = crn, nomsNumber = prisonNumber))

    val entity = buildCaseEntity(
      id = dutyToReferCaseId,
    ) {
      withCrn(crn)
      withPrisonNumber(prisonNumber)
    }
    caseRepository.save(entity)

    PrisonerSearchStubs.getPrisonerOKResponse(prisonNumber = prisonNumber, response = buildPrisoner(prisonNumber = prisonNumber, inOutStatus = InOutStatus.IN))

    val cas3Application = buildCas3Application(
      id = cas3ApplicationId,
      applicationStatus = Cas3ApplicationStatus.REJECTED,
      uiUrl = cas3ReferralUiUrl,
      submittedApplication = buildCas3SubmittedApplication(
        submittedDate = LocalDate.of(2025, 1, 2),
      ),
    )

    ApprovedPremisesStubs.getCas3SuitableApplicationOKResponse(crn = crn, response = cas3Application)

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetEligibilityResponseCannotStartYet(
            crn = crn,
            cas3ApplicationId = cas3ApplicationId,
            cas3Url = cas3ReferralUiUrl,
            crsSubmissionDate = crsSubmissionDate.toString(),
            crsUrl = crsUrl,
          ),
        )
      }
  }

  @Test
  fun `should continue evaluation and do not include 404 in upstream failures when tier returns not found`() {
    val cas1Application = buildCas1ApplicationV2(
      status = Cas1ApplicationStatus.PLACEMENT_ALLOCATED,
      id = cas1ApplicationId,
      uiUrl = cas1ApplicationUiUrl,
      submittedApplication = buildCas1SubmittedApplication(
        upcomingRequestForPlacement = buildCas1ExternalRequestForPlacementOutcomeDto(
          requestForPlacement = buildCas1RequestForPlacementSummaryV2(status = Cas1RequestForPlacementStatus.PLACEMENT_BOOKED),
          placement = buildCas1PlacementV2(
            status = Cas1PlacementStatus.ARRIVED,
            canonicalArrivalDate = LocalDate.parse("2025-01-01"),
            canonicalDepartureDate = LocalDate.parse("2025-07-01"),
          ),
        ),
      ),
    )
    ApprovedPremisesStubs.getCas1CurrentApplicationOKResponse(crn = crn, response = cas1Application)
    TierStubs.getTierNotFoundResponse(crn = crn)

    val entity = buildCaseEntity(id = dutyToReferCaseId) {
      withCrn(crn)
      withPrisonNumber(prisonNumber)
    }
    caseRepository.save(entity)

    val existingEntity = dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = entity.id,
        localAuthorityAreaId = localAuthorityArea.id,
        referenceNumber = "DTR-REF-001",
        submissionDate = LocalDate.now().minusMonths(5),
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetEligibilityResponseTierNotFound(
            crn = crn,
            cas1ApplicationId = cas1ApplicationId,
            cas3ApplicationId = cas3ApplicationId,
            dutyToReferCaseId = dutyToReferCaseId,
            dutyToReferId = existingEntity.id,
            localAuthorityAreaId = localAuthorityArea.id,
            localAuthorityAreaName = localAuthorityArea.name,
            submissionDate = existingEntity.submissionDate.toString(),
            referenceNumber = "DTR-REF-001",
            createdBy = NAME_OF_TEST_DATA_SETUP_USER,
            createdAt = existingEntity.createdAt.truncatedTo(ChronoUnit.SECONDS).toString(),
            crsSubmissionDate = crsSubmissionDate.toString(),
            cas1ApplicationUrl = cas1ApplicationUiUrl,
            crsUrl = crsUrl,
            cas3ReferralUrl = cas3ReferralUiUrl,
            cas1ApplicationStartedAt = cas1Application.createdAt.asJsonValue(),
            cas1ApplicationSubmittedAt = cas1Application.submittedApplication?.submittedAt?.asJsonValue().toString(),
            cas1RequestForPlacementExpectedArrivalDate = cas1Application.submittedApplication?.upcomingRequestForPlacement?.requestForPlacement?.expectedArrivalDate.toString(),
            cas1PlacementExpectedDepartureDate = cas1Application.submittedApplication?.upcomingRequestForPlacement?.placement?.expectedDepartureDate.toString(),
            cas1SubmittedAt = cas1Application.submittedApplication?.upcomingRequestForPlacement?.requestForPlacement?.submittedAt.toString(),
          ),
        )
      }
  }

  @Test
  fun `should send back upstream failures and a not eligible service result when there are upstream failures`() {
    val upstreamUrl = TierStubs.getTierServerErrorResponse(crn = crn)

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody<String>()
      .value {
        assertThatJson(it!!).matchesExpectedJson(
          expectedGetEligibilityUpstreamFailuresResponse(
            crn,
            upstreamUrl,
          ),
        )
      }
  }

  @Test
  fun `should include failureReason when defined`() {
    val cas1Application = buildCas1ApplicationV2(
      status = Cas1ApplicationStatus.REJECTED,
      id = cas1ApplicationId,
      createdAt = OffsetDateTime.parse("2023-01-01T12:00:00.000Z"),
    )

    TierStubs.getTierOKResponse(crn = crn, buildTier(TierScore.E))
    ApprovedPremisesStubs.getCas1CurrentApplicationOKResponse(
      crn = crn,
      response = cas1Application,
    )

    val entity = buildCaseEntity(id = dutyToReferCaseId) {
      withCrn(crn)
      withPrisonNumber(prisonNumber)
    }
    caseRepository.save(entity)

    dutyToReferRepository.save(
      buildDutyToReferEntity(
        caseId = entity.id,
        localAuthorityAreaId = localAuthorityArea.id,
        referenceNumber = "DTR-REF-001",
        submissionDate = LocalDate.now().minusMonths(5),
        status = EntityDtrStatus.SUBMITTED,
      ),
    )

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data.cas1.serviceResult.failureReasons[0]").isEqualTo("MALE_NOT_HIGH_RISK_TIER")
  }

  @Test
  fun `should succeed when prisoner search returns 404`() {
    PrisonerSearchStubs.getPrisonerNotFoundResponse(prisonNumber = prisonNumber)
    TierStubs.getTierOKResponse(crn = crn, buildTier(TierScore.A))

    val entity = buildCaseEntity(id = dutyToReferCaseId) {
      withCrn(crn)
      withPrisonNumber(prisonNumber)
    }
    caseRepository.save(entity)

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
  }

  @Test
  fun `should succeed when case has no prison number`() {
    TierStubs.getTierOKResponse(crn = crn, buildTier(TierScore.A))

    val entity = buildCaseEntity(id = dutyToReferCaseId) { withCrn(crn) }
    caseRepository.save(entity)

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
  }

  @Test
  fun `should send back failed eligibility when CAS applications returns 404`() {
    ApprovedPremisesStubs.getCas1CurrentApplicationNotFoundResponse(crn = crn)
    ApprovedPremisesStubs.getCas2SuitableApplicationNotFoundResponse(crn = crn)
    ApprovedPremisesStubs.getCas3SuitableApplicationNotFoundResponse(crn = crn)

    TierStubs.getTierOKResponse(crn = crn, buildTier(TierScore.MISSING))

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchangeSuccessfully()
      .expectBody()
      .jsonPath("$.data.cas1.serviceResult.serviceStatus").isEqualTo("NOT_ELIGIBLE")
      .jsonPath("$.data.cas2.serviceResult.serviceStatus").isEqualTo("NOT_ELIGIBLE")
      .jsonPath("$.data.cas3.serviceResult.serviceStatus").isEqualTo("NOT_ELIGIBLE")
      .jsonPath("$.data.cas1.cas1Application").isEmpty()
      .jsonPath("$.data.cas2.cas2Application").isEmpty()
      .jsonPath("$.data.cas3.cas3Application").isEmpty()
      .jsonPath("$.upstreamFailures.length()").isEqualTo(3)
      .jsonPath("$.upstreamFailures[0].endpoint").isEqualTo("getCas1CurrentApplication")
      .jsonPath("$.upstreamFailures[1].endpoint").isEqualTo("getCas2Application")
      .jsonPath("$.upstreamFailures[2].endpoint").isEqualTo("getCas3Application")
      .jsonPath("$.upstreamFailures[0].httpResponseStatus").isEqualTo("404 NOT_FOUND")
      .jsonPath("$.upstreamFailures[1].httpResponseStatus").isEqualTo("404 NOT_FOUND")
      .jsonPath("$.upstreamFailures[2].httpResponseStatus").isEqualTo("404 NOT_FOUND")
  }

  @Test
  fun `should successfully get eligibility when CAS applications returns 204`() {
    ApprovedPremisesStubs.getCas1CurrentPremisesNoContentResponse(crn = crn)
    ApprovedPremisesStubs.getCas3CurrentPremisesNoContentResponse(crn = crn)
    ApprovedPremisesStubs.getCas1CurrentApplicationNoContentResponse(crn = crn)
    ApprovedPremisesStubs.getCas2SuitableApplicationNoContentResponse(crn = crn)
    ApprovedPremisesStubs.getCas3SuitableApplicationNoContentResponse(crn = crn)

    TierStubs.getTierOKResponse(crn = crn, buildTier(TierScore.MISSING))

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.data.cas1.cas1Application").isEmpty()
      .jsonPath("$.data.cas2.cas2Application").isEmpty()
      .jsonPath("$.data.cas3.cas3Application").isEmpty()
      .jsonPath("$.upstreamFailures").doesNotExist()
  }

  @Test
  fun `should send back failed eligibility when CAS current premises returns 404`() {
    ApprovedPremisesStubs.getCas1CurrentPremisesNotFoundResponse(crn = crn)
    ApprovedPremisesStubs.getCas3CurrentPremisesNotFoundResponse(crn = crn)

    TierStubs.getTierOKResponse(crn = crn, buildTier(TierScore.MISSING))

    webTestClient.get().uri("/cases/{crn}/eligibility", crn)
      .withDeliusUserJwt()
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.data.cas1.serviceResult.serviceStatus").isEqualTo("NOT_ELIGIBLE")
      .jsonPath("$.data.cas3.serviceResult.serviceStatus").isEqualTo("NOT_ELIGIBLE")
      .jsonPath("$.upstreamFailures.length()").isEqualTo(2)
      .jsonPath("$.upstreamFailures[0].endpoint").isEqualTo("getCas1CurrentPremises")
      .jsonPath("$.upstreamFailures[1].endpoint").isEqualTo("getCas3CurrentPremises")
      .jsonPath("$.upstreamFailures[0].httpResponseStatus").isEqualTo("404 NOT_FOUND")
      .jsonPath("$.upstreamFailures[1].httpResponseStatus").isEqualTo("404 NOT_FOUND")
  }
}
