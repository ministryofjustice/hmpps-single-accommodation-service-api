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
  cas1Url: String,
  cas3Url: String,
  crsUrl: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "cas1":{
         "serviceResult":{
            "serviceStatus":"ARRIVED",
            "action":null,
            "link":"View application",
            "url":"$cas1Url",
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
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View referral",
            "url":"$cas3Url",
            "failureReasons":[]
         },
         "cas3Application":{
            "id":"$cas3ApplicationId",
            "applicationStatus":"SUBMITTED",
            "assessmentStatus":"UNALLOCATED",
            "bookingStatus":null
         }
      },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":"Add DTR referral outcome",
            "link":"Add outcome",
            "url":null,
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
            "withdrawalReasonOther":null,
            "outcomeReason":null
         }
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "url":"$crsUrl",
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
            "url":null,
            "failureReasons":[
               "SUITABLE_CAS1_APPLICATION",
               "SUITABLE_CAS3_APPLICATION"
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
            "url":null,
            "failureReasons":[]
         },
         "cas1Application":null
      },
      "cas3":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "url":null,
            "failureReasons":[]
         },
         "cas3Application":null
      },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "url":null,
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
            "url":null,
            "failureReasons":[]
         },
         "commissionedRehabilitativeServices":null
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "url":null,
            "failureReasons":[]
         }
      },
      "caseActions":[]
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
  cas1Url: String,
  cas3Url: String,
  crsUrl: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "cas1":{
         "serviceResult":{
            "serviceStatus":"ARRIVED",
            "action":null,
            "link":"View application",
            "url":"$cas1Url",
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
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View referral",
            "url":"$cas3Url",
            "failureReasons":[]
         },
         "cas3Application":{
            "id":"$cas3ApplicationId",
            "applicationStatus":"SUBMITTED",
            "assessmentStatus":"UNALLOCATED",
            "bookingStatus":null
         }
      },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":"Add DTR referral outcome",
            "link":"Add outcome",
            "url":null,
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
            "withdrawalReasonOther":null,
            "outcomeReason":null
         }
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "url":"$crsUrl",
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
            "url":null,
            "failureReasons":[
               "SUITABLE_CAS1_APPLICATION",
               "SUITABLE_CAS3_APPLICATION"
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
  cas3Url: String,
  crsUrl: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "cas1":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "url":null,
            "failureReasons":[
               "S_TIER"
            ]
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
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View referral",
            "url":"$cas3Url",
            "failureReasons":[]
         },
         "cas3Application":{
            "id":"$cas3ApplicationId",
            "applicationStatus":"SUBMITTED",
            "assessmentStatus":"UNALLOCATED",
            "bookingStatus":null
         }
      },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":"Add DTR referral outcome",
            "link":"Add outcome",
            "url":null,
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
            "withdrawalReasonOther":null,
            "outcomeReason":null
         }
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "url":"$crsUrl",
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
            "url":null,
            "failureReasons":[
               "SUITABLE_CAS3_APPLICATION"
            ]
         }
      },
      "caseActions":[
         "Add DTR referral outcome"
      ]
   }
}
""".trimIndent()
