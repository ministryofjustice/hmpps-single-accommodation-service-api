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
  crsSubmissionDate: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "cas1":{
         "serviceResult":{
            "serviceStatus":"ARRIVED",
            "action":null,
            "link":"View application",
            "failureReasons":[]
         },
         "cas1Application":{
            "id":"$cas1ApplicationId",
            "applicationStatus":"PLACEMENT_ALLOCATED",
            "requestForPlacementStatus":"PLACEMENT_BOOKED",
            "placementStatus":"ARRIVED"
         }
      },
      "cas3":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":["INVALID_CURRENT_ACCOMMODATION_TYPE"]
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
            "action":"Add DTR referral outcome",
            "link":"Add outcome",
            "failureReasons":[]
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
            "createdAt":"$createdAt",
            "withdrawalReason":null,
            "withdrawalReasonOther":null
         }
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "failureReasons":[]
         },
         "commissionedRehabilitativeServices":{
            "status":"COMPLETED",
            "submissionDate":"$crsSubmissionDate"
         }
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":[
               "SUITABLE_CAS1_APPLICATION"
            ]
         }
      },
      "caseActions":[
         "Add DTR referral outcome"
      ]
   }
}
""".trimIndent()

fun expectedGetEligibilityUpstreamFailuresResponse(
  crn: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "cas1":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":[]
         },
         "cas1Application":null
      },
      "cas3":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":[]
         },
         "cas3Application":null
      },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":[]
         },
         "caseId":null,
         "submission":null
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":[]
         },
         "commissionedRehabilitativeServices":null
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":[]
         }
      },
      "caseActions":[
         
      ]
   },
   "upstreamFailures":[
      {
         "endpoint":"getTierByCrn",
         "failureType":"UPSTREAM_HTTP_ERROR",
         "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
         "message":"500 Internal Server Error: [no body]",
         "identifier":null
      }
   ]
}
""".trimIndent()

fun expectedGetEligibilityResponseTierNotFound(
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
  crsSubmissionDate: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "cas1":{
         "serviceResult":{
            "serviceStatus":"ARRIVED",
            "action":null,
            "link":"View application",
            "failureReasons":[]
         },
         "cas1Application":{
            "id":"$cas1ApplicationId",
            "applicationStatus":"PLACEMENT_ALLOCATED",
            "requestForPlacementStatus":"PLACEMENT_BOOKED",
            "placementStatus":"ARRIVED"
         }
      },
      "cas3":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":["INVALID_CURRENT_ACCOMMODATION_TYPE"]
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
            "action":"Add DTR referral outcome",
            "link":"Add outcome",
            "failureReasons":[]
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
            "createdAt":"$createdAt",
            "withdrawalReason":null,
            "withdrawalReasonOther":null
         }
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "failureReasons":[]
         },
         "commissionedRehabilitativeServices":{
            "status":"COMPLETED",
            "submissionDate":"$crsSubmissionDate"
         }
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":[
               "SUITABLE_CAS1_APPLICATION"
            ]
         }
      },
      "caseActions":[
         "Add DTR referral outcome"
      ]
   }
}
""".trimIndent()

fun expectedGetEligibilityNotEligibleSTierFail(
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
  crsSubmissionDate: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "cas1":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":["S_TIER"]
         },
         "cas1Application":{
            "id":"$cas1ApplicationId",
            "applicationStatus":"REJECTED",
            "requestForPlacementStatus":null,
            "placementStatus":null
         }
      },
      "cas3":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "failureReasons":["INVALID_CURRENT_ACCOMMODATION_TYPE"]
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
            "action":"Add DTR referral outcome",
            "link":"Add outcome",
            "failureReasons":[]
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
            "createdAt":"$createdAt",
            "withdrawalReason":null,
            "withdrawalReasonOther":null
         }
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "failureReasons":[]
         },
         "commissionedRehabilitativeServices":{
            "status":"COMPLETED",
            "submissionDate":"$crsSubmissionDate"
         }
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"NOT_STARTED",
            "action":"Add and confirm proposed address",
            "link":null,
            "failureReasons":[]
         }
      },
      "caseActions":[
         "Add DTR referral outcome",
         "Add and confirm proposed address"
      ]
   }
}
""".trimIndent()
