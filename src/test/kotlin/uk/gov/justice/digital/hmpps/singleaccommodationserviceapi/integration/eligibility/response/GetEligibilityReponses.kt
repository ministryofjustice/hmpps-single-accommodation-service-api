package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

import java.util.UUID

fun expectedGetEligibilityResponse(crn: String, cas1ApplicationId: UUID, cas3ApplicationId: UUID): String = """
{
   "data":{
   "crn":"$crn",
   "cas1":{
      "serviceStatus":"SUBMITTED",
      "suitableApplicationId":"$cas1ApplicationId",
      "action":"Wait for approved premise (CAS1) assessment result",
      "link":"View application"
   },
   "cas3":{
      "serviceStatus":"NOT_SUBMITTED",
      "suitableApplicationId":"$cas3ApplicationId",
      "action":null,
      "link":"View referral"
   },
   "caseActions":[
      "Wait for approved premise (CAS1) assessment result"
   ]
   }
}
""".trimIndent()
