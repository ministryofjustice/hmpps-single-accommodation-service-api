package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.util

import io.netty.channel.ConnectTimeoutException
import io.netty.handler.timeout.ReadTimeoutException
import org.springframework.web.reactive.function.client.WebClientRequestException
import java.net.ConnectException
import java.net.UnknownHostException

fun WebClientRequestException.isConnectionError() = cause is UnknownHostException || cause is ConnectException
fun WebClientRequestException.isTimeout() = cause is ReadTimeoutException || cause is ConnectTimeoutException
