package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class WireMockManager : ApplicationContextInitializer<ConfigurableApplicationContext> {

  companion object {
    lateinit var sasWiremock: WireMockServer
      private set

    private fun ensureStarted() {
      if (!::sasWiremock.isInitialized) {
        sasWiremock = WireMockServer(options().dynamicPort())
        sasWiremock.start()

        reset()
      }
    }

    fun reset() {
      sasWiremock.resetAll()
      setupDefaultResponse()
    }

    fun setupDefaultResponse() {
      ApprovedPremisesStubs.setupDefaultResponses()
    }
  }

  override fun initialize(context: ConfigurableApplicationContext) {
    ensureStarted()

    /*
     * get a list of the services from application-test.yml and add them to the environment properties using the
     * random port.
     */
    val services: List<String> =
      Binder.get(context.environment)
        .bind("wiremock.service-names", Bindable.listOf(String::class.java))
        .get()

    TestPropertyValues.of(
      services.map { service ->
        "service.$service.base-url=${sasWiremock.baseUrl()}"
      },
    ).and("hmpps-auth.url=${sasWiremock.baseUrl()}/auth")
      .applyTo(context.environment)
  }
}
