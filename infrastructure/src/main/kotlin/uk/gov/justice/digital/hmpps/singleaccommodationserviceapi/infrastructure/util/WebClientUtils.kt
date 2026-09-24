package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.util

import io.netty.channel.ConnectTimeoutException
import io.netty.handler.timeout.ReadTimeoutException
import org.springframework.web.reactive.function.client.WebClientRequestException

fun WebClientRequestException.isTimeout() = cause is ReadTimeoutException || cause is ConnectTimeoutException
