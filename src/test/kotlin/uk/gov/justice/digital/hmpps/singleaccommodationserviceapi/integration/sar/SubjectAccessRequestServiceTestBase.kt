package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.sar

import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationStatusRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.AccommodationTypeRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.DutyToReferRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.LocalAuthorityAreaRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.ProposedAccommodationRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.service.sar.SubjectAccessRequestService
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.IntegrationTestBase

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
}
