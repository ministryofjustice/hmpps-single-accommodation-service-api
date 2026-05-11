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
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":"Wait for approved premise (CAS1) assessment result",
            "link":"View application"
         },
         "cas1Application":{
            "id":"$cas1ApplicationId",
            "applicationStatus":"AWAITING_ASSESSMENT",
            "requestForPlacementStatus":null,
            "placementStatus":null
         }
      },
      "cas3":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null
         },
         "cas3Application":{
            "id":"$cas3ApplicationId",
            "applicationStatus":"IN_PROGRESS",
            "assessmentStatus":null,
            "bookingStatus":null
         }
      },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":"Add DTR outcome",
            "link":"Add outcome"
         },
         "caseId":"$dutyToReferCaseId",
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
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"NOT_STARTED",
            "action":"Complete CRS Referral",
            "link":"View refer and monitor"
         },
         "commissionedRehabilitativeServices":null
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"COMPLETED",
            "action":null,
            "link":null
         }
      },
      "caseActions":[
         "Add DTR outcome",
         "Complete CRS Referral",
         "Wait for approved premise (CAS1) assessment result"
      ]
   }
}
""".trimIndent()
