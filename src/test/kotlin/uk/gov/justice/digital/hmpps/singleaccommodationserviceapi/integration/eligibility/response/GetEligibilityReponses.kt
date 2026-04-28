package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

import java.util.UUID

fun expectedGetEligibilityResponse(
  crn: String,
  cas1ApplicationId: UUID,
  cas3ApplicationId: UUID,
  dutyToReferCaseId: UUID,
  dutyToReferId: UUID,
  localAuthorityAreaId: UUID,
  localAuthorityAreaName: String,
  submissionDate: String,
  referenceNumber: String,
  createdBy: String,
  createdAt: String,
): String = """
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
      "dtr":{
         "serviceStatus":"SUBMITTED",
         "suitableApplicationId":null,
         "action":"Add DTR outcome",
         "link":"Add outcome"
      },
      "caseActions":[
         "Add DTR outcome",
         "Wait for approved premise (CAS1) assessment result"
      ],
      "dutyToRefer":{
         "caseId":"$dutyToReferCaseId",
         "crn":"$crn",
         "status":"SUBMITTED",
         "submission":{
            "id":"$dutyToReferId",
            "localAuthority":{
               "localAuthorityAreaId":"$localAuthorityAreaId",
               "localAuthorityAreaName":"$localAuthorityAreaName"
            },
            "referenceNumber":"$referenceNumber",
            "submissionDate":"$submissionDate",
            "createdBy":"$createdBy",
            "createdAt":"$createdAt"
         }
      }
   }
}
""".trimIndent()
