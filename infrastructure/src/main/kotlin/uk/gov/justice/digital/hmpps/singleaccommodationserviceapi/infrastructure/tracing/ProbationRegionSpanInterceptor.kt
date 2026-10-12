package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.tracing

import io.opentelemetry.api.trace.Span
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

const val USER_PROBATION_REGION_HEADER = "x-user-probation-region"
const val USER_PROBATION_REGION_ATTRIBUTE = "user.probation.region"

@Component
class ProbationRegionSpanInterceptor : HandlerInterceptor {
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    request.getHeader(USER_PROBATION_REGION_HEADER)
      ?.takeIf { it.isNotBlank() }
      ?.let { region ->
        val span = Span.current()
        if (span.spanContext.isValid) {
          span.setAttribute(USER_PROBATION_REGION_ATTRIBUTE, region)
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
