package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.CaseRepository
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.application.mapper.CaseMapper
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.aggregate.CaseAggregate

@Service
class CaseCreationService(private val caseRepository: CaseRepository) {

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun saveUnpersistedCases(crnsToPrisonNumbers: List<CrnToPrisonNumber>) {
    val unpersistedCrns = caseRepository
      .findUnpersistedCrns(crnsToPrisonNumbers.map { it.crn }.toTypedArray())
      .toSet()

    if (unpersistedCrns.isEmpty()) {
      return
    }

    val entities = crnsToPrisonNumbers
      .filter { it.crn in unpersistedCrns }
      .map {
        CaseMapper.create(
          CaseAggregate.hydrateNew().snapshot(),
          buildIdentifiers(it.crn, it.prisonNumber),
        )
      }

    caseRepository.saveAll(entities)
  }
}
