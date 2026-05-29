package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.config

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.exception.UpstreamFailureException
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions.DomainException
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@RestControllerAdvice
class SingleAccommodationServiceApiExceptionHandler {
  @ExceptionHandler(DomainException::class)
  fun handleDomainException(e: DomainException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Domain exception: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Domain exception: {}", e.message) }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Validation exception: {}", e.message) }

  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Invalid request: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Illegal argument exception: {}", e.message) }

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "No resource found failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("No resource found exception: {}", e.message) }

  @ExceptionHandler(NotFoundException::class)
  fun handleNotFoundException(e: RuntimeException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "Not found: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Not found: {}", e.message) }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(FORBIDDEN)
    .body(
      ErrorResponse(
        status = FORBIDDEN,
        userMessage = "Forbidden: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Forbidden (403) returned: {}", e.message) }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        userMessage = "Unexpected error: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Unexpected exception", e) }

  @ExceptionHandler(UpstreamFailureException::class)
  fun handleUpstreamFailureException(e: UpstreamFailureException): ResponseEntity<ErrorResponse> {
    val firstUpstreamFailure = e.upstreamFailures.first()
    val httpStatusCode = firstUpstreamFailure.httpResponseStatus?.value() ?: INTERNAL_SERVER_ERROR.value()
    val httpStatus = HttpStatus.valueOf(httpStatusCode)
    return ResponseEntity
      .status(httpStatus)
      .body(
        ErrorResponse(
          status = httpStatus,
          userMessage = e.message,
          developerMessage = e.message,
        ),
      ).also { log.error("{}: {}", httpStatus.name, e.message) }
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
