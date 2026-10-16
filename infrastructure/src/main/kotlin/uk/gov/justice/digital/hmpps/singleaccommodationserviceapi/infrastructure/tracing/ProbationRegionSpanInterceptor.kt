package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.tracing

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

const val USER_PROBATION_REGION_HEADER = "x-user-probation-region"
const val USER_PROBATION_REGION_ATTRIBUTE = "user.probation.region"

@Component
class ProbationRegionSpanInterceptor : HandlerInterceptor {
  private val log = LoggerFactory.getLogger(javaClass)

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    request.getHeader(USER_PROBATION_REGION_HEADER)
      ?.takeIf { it.isNotBlank() }
      ?.let { region ->
        val span = Span.current()
        log.debug("Found {} header with value '{}' on {} {}", USER_PROBATION_REGION_HEADER, region, request.method, request.requestURI)
        if (span.spanContext.isValid) {
          span.setAttribute(USER_PROBATION_REGION_ATTRIBUTE, region)
          log.debug("Applied span attribute {}='{}'", USER_PROBATION_REGION_ATTRIBUTE, region)
        } else {
          log.debug("No valid current span available for {}='{}'", USER_PROBATION_REGION_ATTRIBUTE, region)
        }
      }
    return true
  }
}

@Configuration
class ProbationRegionSpanInterceptorConfig(
  private val interceptor: ProbationRegionSpanInterceptor,
) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(interceptor)
  }
}
