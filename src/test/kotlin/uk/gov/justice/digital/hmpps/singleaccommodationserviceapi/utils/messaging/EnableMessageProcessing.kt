package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.messaging

import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.getBean
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Message processing (inbox dispatching, outbox publishing, case refresh worker, etc.) is switched
 * off by default in the test profile. Annotate a test class or an individual test with this to turn
 * it on for the duration of each annotated test. It is switched off again afterwards.
 *
 * This toggles [TestSchedulingProperties.enabled] on the shared Spring context rather than requiring a
 * separate context (e.g. via `@TestPropertySource`).
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(MessageProcessingExtension::class)
annotation class EnableMessageProcessing

class MessageProcessingExtension :
  BeforeTestExecutionCallback,
  AfterTestExecutionCallback {
  // Enabled only after all @BeforeEach fixture cleanup and setup has finished, so stale work from a
  // previous test cannot be processed while the queues, WireMock and tables are being reset.
  override fun beforeTestExecution(context: ExtensionContext) = helper(context).enableProcessing()

  // Disabled (and in-flight tasks awaited) straight after the test body, before any @AfterEach teardown.
  override fun afterTestExecution(context: ExtensionContext) = helper(context).disableProcessingAndAwaitIdle()

  private fun helper(context: ExtensionContext) = SpringExtension.getApplicationContext(context).getBean<MessagingTestStateHelper>()
}
