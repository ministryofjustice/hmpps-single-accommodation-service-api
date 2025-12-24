package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

fun expectedGetEligibilityResponse(crn: String): String = """
{
   "crn":"$crn",
   "cas1":{
      "serviceStatus":"NOT_ELIGIBLE",
      "suitableApplication":null,
      "actions":[],
      "caseStatus":"NO_ACTION_NEEDED"
   },
   "cas2Hdc":{
      "serviceStatus":"NOT_STARTED",
      "suitableApplication":null,
      "actions":[
         "Start HDC referral!!"
      ],
      "caseStatus":"ACTION_NEEDED"
   },
   "cas2PrisonBail":{
      "serviceStatus":"NOT_ELIGIBLE",
      "suitableApplication":null,
      "actions":[],
      "caseStatus":"NO_ACTION_NEEDED"
   },
   "cas2CourtBail":{
      "serviceStatus":"NOT_ELIGIBLE",
      "suitableApplication":null,
      "actions":[],
      "caseStatus":"NO_ACTION_NEEDED"
   },
   "cas3":{
      "serviceStatus":"UPCOMING",
      "suitableApplication":null,
      "actions":[
         "Start temporary accommodation referral in 2 days!!"
      ],
      "caseStatus":"ACTION_UPCOMING"
   },
   "caseActions":[
      "Start HDC referral!!",
      "Start temporary accommodation referral in 2 days!!"
   ],
   "caseStatus":"ACTION_NEEDED"
}
""".trimIndent()
