package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.sentry

import io.sentry.IScope
import io.sentry.ScopeType
import io.sentry.Sentry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.UUID

@Component
class SentryRequestContextInterceptor : HandlerInterceptor {
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    val requestId = UUID.randomUUID().toString()
    val endpointPattern = request.getPathPattern() ?: request.requestURI

    Sentry.configureScope(ScopeType.ISOLATION) { scope: IScope ->
      scope.setTag("request.id", requestId)
      scope.setTag("request.method", request.method)
      scope.setTag("request.pathPattern", endpointPattern)
    }

    return true
  }

  private fun HttpServletRequest.getPathPattern() = getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String
}

@Profile("!local & !test")
@Configuration
class SentryRequestContextInterceptorConfig(
  private val interceptor: SentryRequestContextInterceptor,
) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(interceptor)
  }
}
