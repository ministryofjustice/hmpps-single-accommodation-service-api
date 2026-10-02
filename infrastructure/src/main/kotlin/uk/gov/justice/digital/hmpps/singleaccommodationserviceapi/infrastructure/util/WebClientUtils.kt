package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.util

import io.netty.channel.ConnectTimeoutException
import io.netty.handler.timeout.ReadTimeoutException
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClientRequestException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.net.UnknownHostException

private val idempotentHttpMethods = setOf(HttpMethod.GET)

fun WebClientRequestException.isConnectionError() = cause.isConnectionErrorCause()
fun WebClientRequestException.isRetryableConnectionError() = method in idempotentHttpMethods && isConnectionError()
fun WebClientRequestException.isTimeout() = cause is ReadTimeoutException || cause is ConnectTimeoutException

private fun Throwable?.isConnectionErrorCause(): Boolean = when (this) {
  is UnknownHostException, is ConnectException -> true
  is SocketException -> true
  is IOException -> javaClass.name == "io.netty.channel.unix.Errors\$NativeIoException" || (message?.contains("Connection reset", ignoreCase = true) == true)
  else -> false
}
