package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.graph

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.ClockConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.config.RulesConfig
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.DeeplinkResolver
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.eligibility.domain.EligibilityTreeProvider

@Configuration
@ComponentScan(
  basePackageClasses = [EligibilityTreeProvider::class],
  excludeFilters = [
    ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [DeeplinkResolver::class]),
  ],
)
@Import(RulesConfig::class, ClockConfig::class)
class EligibilityRulesGraphConfiguration
