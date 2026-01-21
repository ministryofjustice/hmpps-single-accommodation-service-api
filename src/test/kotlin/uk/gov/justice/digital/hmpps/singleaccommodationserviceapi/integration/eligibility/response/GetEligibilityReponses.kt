package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

import java.util.UUID

fun expectedGetEligibilityResponse(crn: String, cas1SuitableApplicationId: UUID): String = """
{
   "crn":"$crn",
   "cas1":{
      "serviceStatus":"SUBMITTED",
      "suitableApplicationId":"$cas1SuitableApplicationId",
      "action":{
         "text":"Await Assessment",
         "isUpcoming":true
      }
   },
   "cas2Hdc":{
      "serviceStatus":"NOT_STARTED",
      "suitableApplicationId":null,
      "action":{
         "text":"Start HDC referral!!",
         "isUpcoming":false
      }
   },
   "cas2PrisonBail":{
      "serviceStatus":"NOT_ELIGIBLE",
      "suitableApplicationId":null,
      "action":null
   },
   "cas2CourtBail":{
      "serviceStatus":"NOT_ELIGIBLE",
      "suitableApplicationId":null,
      "action":null
   },
   "cas3":{
      "serviceStatus":"UPCOMING",
      "suitableApplicationId":null,
      "action":{
         "text":"Start temporary accommodation referral in 2 days!!",
         "isUpcoming":true
      }
   },
   "caseActions":[
      "Await Assessment",
      "Start HDC referral!!",
      "Start temporary accommodation referral in 2 days!!"
   ],
   "caseStatus":"ACTION_NEEDED"
}
""".trimIndent()
