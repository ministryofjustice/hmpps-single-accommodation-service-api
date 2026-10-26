package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.config.SchedulingProperties

/**
 * Message processing (inbox dispatching, outbox publishing, case refresh worker, etc.) is switched
 * off by default in the test profile. Annotate a test class or an individual test with this to turn
 * it on for the duration of each annotated test. It is switched off again afterwards.
 *
 * This toggles [SchedulingProperties.enabled] on the shared Spring context rather than requiring a
 * separate context (e.g. via `@TestPropertySource`).
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(MessageProcessingExtension::class)
annotation class EnableMessageProcessing

class MessageProcessingExtension :
  BeforeEachCallback,
  AfterEachCallback {
  override fun beforeEach(context: ExtensionContext) {
    schedulingProperties(context).enabled = true
  }

  override fun afterEach(context: ExtensionContext) {
    schedulingProperties(context).enabled = false
  }

  private fun schedulingProperties(context: ExtensionContext) = SpringExtension.getApplicationContext(context).getBean(SchedulingProperties::class.java)
}
