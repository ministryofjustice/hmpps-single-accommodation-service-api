package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.cas1

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.ServiceResult
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.ContextUpdater
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EvaluationContext
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit.YEARS

@Component
class Cas1ContextUpdater(val clock: Clock) : ContextUpdater {

  override fun update(context: EvaluationContext): EvaluationContext {
    val isWithinOneYear = YEARS.between(LocalDate.now(clock), context.data.releaseDate) < 1 || LocalDate.now(clock).plusYears(1) == context.data.releaseDate
    val action = Cas1ActionTransformer.buildCas1Action(context.data, clock, isWithinOneYear)
    val link = Cas1LinkTransformer.buildCas1Link(context.data, isWithinOneYear)

    val updatedServiceResult =
      ServiceResult(
        serviceStatus =
          Cas1ServiceStatusTransformer.toServiceStatus(
            context.data.cas1Application?.applicationStatus,
            !action.isUpcoming,
          ),
        suitableApplicationId = context.data.cas1Application?.id,
        action = action,
        link = link,
      )

    return context.copy(currentResult = updatedServiceResult)
  }
}