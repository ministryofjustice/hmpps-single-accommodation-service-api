package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.api.model.Case
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mocks.mockUserCases

@Service
class CasesService {

  fun getUserCases(): List<Case> = mockUserCases()
}
