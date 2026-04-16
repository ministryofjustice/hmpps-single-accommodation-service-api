package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.mutation.domain.exceptions

const val NOTE_IS_EMPTY_KEY = "noteEmpty"
const val NOTE_GREATER_THAN_MAX_LENGTH_KEY = "noteGreaterThanMaxLength"

class NoteIsEmptyException : DomainException(NOTE_IS_EMPTY_KEY)
class NoteIsGreaterThanMaxLengthException : DomainException(NOTE_GREATER_THAN_MAX_LENGTH_KEY)
