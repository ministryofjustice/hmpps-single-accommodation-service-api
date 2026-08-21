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
  cas1ApplicationUrl: String,
  cas3ReferralUrl: String,
  crsUrl: String,
  submittedAt: String,
  requestSubmittedAt: String,
  expectedArrivalDate: String,
  expiresAt: String,
  cas1ApplicationStartedAt: String,
  startDate: String,
  endDate: String,
  dateApplied: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "PLACEMENT_BOOKED",
        "action": null,
        "link": "View application",
        "url": "$cas1ApplicationUrl",
        "linkType": "CAS1_VIEW_APPLICATION",
        "failureReasons": [],
        "blockingStatusReason": null
      },
      "cas1Application": {
        "uiUrl": "$cas1ApplicationUrl",
        "application": {
          "id": "$cas1ApplicationId",
          "status": "PLACEMENT_ALLOCATED",
          "createdAt": "$cas1ApplicationStartedAt",
          "createdBy": {
            "name": "Bob",
            "username": "Bob123",
            "staffCode": "123"
          },
          "submittedAt": "$submittedAt",
          "expiresAt": "$expiresAt"
        },
        "assessment": {
          "decision": "ACCEPTED",
          "rejectionRationale": null
        },
        "requestForPlacement": {
          "status": "PLACEMENT_BOOKED",
          "decision": "ACCEPTED",
          "rejectionReason": null,
          "submittedBy": {
            "name": "Bob",
            "username": "Bob123",
            "staffCode": "123"
          },
          "submittedAt": "$requestSubmittedAt",
          "withdrawalReason": null,
          "withdrawalDate": null,
          "expectedArrivalDate": "$expectedArrivalDate",
          "durationDays": 12
        },
        "placement": {
          "status": "UPCOMING",
          "actualArrivalDate": null,
          "actualDepartureDate": null,
          "cancellationReason": null,
          "premises": {
            "startDate": "$startDate",
            "endDate": "$endDate",
            "addressLine1": "Test House",
            "addressLine2": "Test Road",
            "town": "Test Town",
            "postcode": "Test Postcode"
          }
        },
        "placementHistory": [
          {
            "requestForPlacement": {
              "status": "PLACEMENT_BOOKED",
              "decision": "ACCEPTED",
              "rejectionReason": null,
              "submittedBy": {
                "name": "Bob",
                "username": "Bob123",
                "staffCode": "123"
              },
              "submittedAt": "$requestSubmittedAt",
              "withdrawalReason": null,
              "withdrawalDate": null,
              "expectedArrivalDate": "$expectedArrivalDate",
              "durationDays": 12
            },
            "placement": {
              "status": "CANCELLED",
              "actualArrivalDate": null,
              "actualDepartureDate": null,
              "cancellationReason": "Oops",
              "premises": {
                "startDate": "$startDate",
                "endDate": "$endDate",
                "addressLine1": "Test House",
                "addressLine2": "Test Road",
                "town": "Test Town",
                "postcode": "Test Postcode"
              }
            },
            "dateApplied": "$dateApplied"
          }
        ],
        "id": "$cas1ApplicationId",
        "applicationStatus": "PLACEMENT_ALLOCATED",
        "requestForPlacementStatus": "PLACEMENT_BOOKED",
        "placementStatus": "UPCOMING"
      }
    },
    "cas3":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View referral",
            "url":"$cas3ReferralUrl",
            "linkType":"CAS3_VIEW_REFERRAL",
            "failureReasons":[],
            "blockingStatusReason":null
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
            "action":{"type":"ADD_DTR_OUTCOME","startDate":null},
            "link":"Add outcome",
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
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
            "outcomeReason":null,
            "submissionNote":null,
            "outcomeNote":null
         }
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "url":"$crsUrl",
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         },
         "commissionedRehabilitativeServices":{
            "status":"LIVE",
            "submissionDate":"$crsSubmissionDate"
         }
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"COMPLETED",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason": null
         }
      },
      "caseActions":[
         {"type":"ADD_DTR_OUTCOME","startDate":null}
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
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         },
         "cas1Application":null
      },
      "cas3":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         },
         "cas3Application":null
      },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
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
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         },
         "commissionedRehabilitativeServices":null
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
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
  cas1ApplicationUrl: String,
  cas3ReferralUrl: String,
  crsUrl: String,
  cas1ApplicationStartedAt: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "ARRIVED",
        "action": null,
        "link": "View application",
        "url": "$cas1ApplicationUrl",
        "linkType": "CAS1_VIEW_APPLICATION",
        "failureReasons": [],
        "blockingStatusReason": null
      },
      "cas1Application": {
        "uiUrl": "$cas1ApplicationUrl",
        "application": {
          "id": "$cas1ApplicationId",
          "status": "PLACEMENT_ALLOCATED",
          "createdAt": "$cas1ApplicationStartedAt",
          "createdBy": {
            "name": "Test Tester",
            "username": "testTester",
            "staffCode": "1234"
          },
          "submittedAt": null,
          "expiresAt": null
        },
        "assessment": null,
        "requestForPlacement": {
          "status": "PLACEMENT_BOOKED",
          "decision": null,
          "rejectionReason": null,
          "submittedBy": null,
          "submittedAt": null,
          "withdrawalReason": null,
          "withdrawalDate": null,
          "expectedArrivalDate": null,
          "durationDays": null
        },
        "placement": {
          "status": "ARRIVED",
          "actualArrivalDate": null,
          "actualDepartureDate": null,
          "cancellationReason": null,
          "premises": null
        },
        "placementHistory": [],
        "id": "$cas1ApplicationId",
        "applicationStatus": "PLACEMENT_ALLOCATED",
        "requestForPlacementStatus": "PLACEMENT_BOOKED",
        "placementStatus": "ARRIVED"
      }
    },
      "cas3":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View referral",
            "url":"$cas3ReferralUrl",
            "linkType":"CAS3_VIEW_REFERRAL",
            "failureReasons":[],
            "blockingStatusReason":null
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
            "action":{"type":"ADD_DTR_OUTCOME","startDate":null},
            "link":"Add outcome",
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
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
            "outcomeReason":null,
            "submissionNote":null,
            "outcomeNote":null
         }
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "url":"$crsUrl",
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         },
         "commissionedRehabilitativeServices":{
            "status":"LIVE",
            "submissionDate":"$crsSubmissionDate"
         }
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[
               "SUITABLE_CAS1_APPLICATION",
               "SUITABLE_CAS3_APPLICATION"
            ],
            "blockingStatusReason":null
         }
      },
      "caseActions":[
         {"type":"ADD_DTR_OUTCOME","startDate":null}
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
  cas3ReferralUrl: String,
  crsUrl: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "NOT_ELIGIBLE",
        "action": null,
        "link": null,
        "url": null,
        "linkType": null,
        "failureReasons": [
          "S_TIER"
        ],
        "blockingStatusReason": null
      },
      "cas1Application": {
        "uiUrl": "https://cas1-ui/applications/$cas1ApplicationId",
        "application": {
          "id": "$cas1ApplicationId",
          "status": "REJECTED",
          "createdAt": "2023-01-01T12:00:00Z",
          "createdBy": {
            "name": "Test Tester",
            "username": "testTester",
            "staffCode": "1234"
          },
          "submittedAt": null,
          "expiresAt": null
        },
        "assessment": null,
        "requestForPlacement": null,
        "placement": null,
        "placementHistory": [],
        "id": "$cas1ApplicationId",
        "applicationStatus": "REJECTED",
        "requestForPlacementStatus": null,
        "placementStatus": null
      }
    },
"cas3":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View referral",
            "url":"$cas3ReferralUrl",
            "linkType":"CAS3_VIEW_REFERRAL",
            "failureReasons":[],
            "blockingStatusReason":null
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
            "action":{"type":"ADD_DTR_OUTCOME","startDate":null},
            "link":"Add outcome",
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
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
            "outcomeReason":null,
            "submissionNote":null,
            "outcomeNote":null
         }
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "url":"$crsUrl",
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         },
         "commissionedRehabilitativeServices":{
            "status":"LIVE",
            "submissionDate":"$crsSubmissionDate"
         }
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"NOT_ELIGIBLE",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[
               "SUITABLE_CAS3_APPLICATION"
            ],
            "blockingStatusReason":null
         }
      },
      "caseActions":[
         {"type":"ADD_DTR_OUTCOME","startDate":null}
      ]
   }
}
""".trimIndent()

fun expectedGetEligibilityResponseCannotStartYet(
  crn: String,
  cas1ApplicationId: UUID,
  cas3ApplicationId: UUID,
  crsSubmissionDate: String,
  cas1ApplicationUrl: String,
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
            "url":"$cas1ApplicationUrl",
            "linkType":"CAS1_VIEW_APPLICATION",
            "failureReasons":[],
            "blockingStatusReason":null
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
            "serviceStatus":"CANNOT_START_YET",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":["DTR_REFERRAL_EXPIRED"],
            "blockingStatusReason":"SUBMIT_DTR_BEFORE_CAS3"
         },
         "cas3Application":{
            "id":"$cas3ApplicationId",
            "applicationStatus":"REJECTED",
            "assessmentStatus":null,
            "bookingStatus":null
         }
      },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"NOT_STARTED",
            "action":{"type":"ADD_DTR_REFERRAL_DETAILS","startDate":null},
            "link":"Add referral details",
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         },
         "caseId":null,
         "submission":null
      },
      "crs":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":null,
            "link":"View refer and monitor",
            "url":"$crsUrl",
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
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
            "linkType":null,
            "failureReasons":[
               "SUITABLE_CAS1_APPLICATION"
            ],
            "blockingStatusReason":null
         }
      },
      "caseActions":[
         {"type":"ADD_DTR_REFERRAL_DETAILS","startDate":null}
      ]
   }
}
""".trimIndent()
