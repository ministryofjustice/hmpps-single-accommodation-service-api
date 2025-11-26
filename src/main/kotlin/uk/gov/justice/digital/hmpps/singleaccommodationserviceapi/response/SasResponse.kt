package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SasResponse<T>(
  val data: T? = null,
  val paginatedData: PaginatedData<T>? = null,
  val metadata: Map<String, Any>? = null,
)

data class PaginatedData<T>(
  val data: T,
  val page: Int,
  val pageSize: Int,
  val totalItems: Long,
  val totalPages: Int,
  val hasNext: Boolean,
  val hasPrevious: Boolean,
)
