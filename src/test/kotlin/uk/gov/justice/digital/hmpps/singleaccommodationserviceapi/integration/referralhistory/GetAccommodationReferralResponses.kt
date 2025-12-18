package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.referralhistory

import java.util.UUID

fun expectedGetReferralHistory(id1: UUID, id2: UUID, id3: UUID, id4: UUID): String = """
[
   {
      "id":"$id3",
      "type":"CAS2v2",
      "status":"ACCEPTED",
      "date":"2025-04-01T00:00:00Z"
   },
   {
      "id":"$id1",
      "type":"CAS1",
      "status":"PENDING",
      "date":"2025-03-01T00:00:00Z"
   },
   {
      "id":"$id4",
      "type":"CAS3",
      "status":"PENDING",
      "date":"2025-02-01T00:00:00Z"
   },
   {
      "id":"$id2",
      "type":"CAS2",
      "status":"PENDING",
      "date":"2025-01-01T00:00:00Z"
   }
]
""".trimIndent()
