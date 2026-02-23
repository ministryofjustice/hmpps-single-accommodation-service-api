package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

class WireMockInitializer :
  ApplicationContextInitializer<ConfigurableApplicationContext>,
  AfterEachCallback,
  AfterAllCallback {

  companion object {
    lateinit var sasWiremock: WireMockServer
      private set
  }

  override fun initialize(context: ConfigurableApplicationContext) {
    sasWiremock = WireMockServer(options().dynamicPort())
    sasWiremock.start()

    /*
     * get a list of the services from application-test.yml and add them to the environment properties using the
     * random port.
     */
    val services: List<String> =
      Binder.get(context.environment)
        .bind("service", Bindable.listOf(String::class.java))
        .get()

    TestPropertyValues.of(
      services.map { service ->
        "service.$service.base-url=${sasWiremock.baseUrl()}"
      },
    ).and("hmpps-auth.url=${sasWiremock.baseUrl()}/auth")
      .applyTo(context.environment)
  }

  override fun afterEach(context: ExtensionContext) {
    sasWiremock.resetAll()
  }

  override fun afterAll(context: ExtensionContext) {
    sasWiremock.stop()
  }
}
