package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.eligibility.response

import java.util.UUID

fun expectedGetEligibilityResponse(
  crn: String,
  cas1ApplicationId: UUID,
  cas3ApplicationId: UUID,
  cas3ApplicationCreatedAt: String,
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
  expectedDepartureDate: String,
  expiresAt: String,
  cas1ApplicationStartedAt: String,
  startDate: String,
  endDate: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "PLACEMENT_CANCELLED",
        "action": {"type":"CREATE_PLACEMENT","startDate":null,"service":"CAS1"},
        "link": "Create new placement request",
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
          "status": "CANCELLED",
          "actualArrivalDate": null,
          "actualDepartureDate": null,
          "expectedDepartureDate": "$expectedDepartureDate",
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
        "placementHistory": [],
        "id": "$cas1ApplicationId",
        "applicationStatus": "PLACEMENT_ALLOCATED",
        "requestForPlacementStatus": "PLACEMENT_BOOKED",
        "placementStatus": "CANCELLED"
      }
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CANCELLED",
        "action": null,
        "link": "Start new application",
        "url": null,
        "linkType": "CAS2_START_APPLICATION",
        "failureReasons": [],
        "blockingStatusReason": null
      },
      "cas2Application": {
        "uiUrl": "https://cas2-ui/applications/e6b202ce-c214-4b87-98f7-111111111111",
        "id": "e6b202ce-c214-4b87-98f7-111111111111",
        "createdAt": "2024-01-01T12:00:00Z",
        "createdBy": {
          "name": "Anne",
          "username": "anne.smith",
          "deliusStaffCode": "ANNE",
          "nomisStaffId": null,
          "userType": "DELIUS"
        },
        "submittedApplication": {
          "latestAssessmentStatus": "CANCELLED",
          "offerDeclinedReason": null,
          "cancelledReason": "cancelled reason",
          "submittedAt": "2024-01-02T12:00:00Z",
          "markedAsArrived": null
        },
        "cohort":"INTENSIVE_SUPERVISION_COURTS"
      }
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "BOOKING_CANCELLED",
        "action": null,
        "link": "View referral",
        "url": "$cas3ReferralUrl",
        "linkType": "CAS3_VIEW_REFERRAL",
        "failureReasons": [],
        "blockingStatusReason":null
      },
      "cas3Application": {
        "id": "$cas3ApplicationId",
        "createdAt": "$cas3ApplicationCreatedAt",
        "applicationStatus": "SUBMITTED",
        "applicationSubmittedDate": "2023-01-01",
        "applicationSubmittedBy": {
          "name": "Test Tester",
          "username": "TestTester",
          "staffCode": "Test1234"
        },
        "applicationRejectedReason": "Oops",
        "assessmentStatus": "REJECTED",
        "bookingStatus": "CANCELLED",
        "cancellation": {
              "cancellationDate": "2023-01-03",
              "cancellationReason": "Mistake"
        },
        "bookingProvisionalOfferSentDate": "2023-01-02",
        "previousBookings": [
          {
            "bookingStatus": "CANCELLED",
            "cancellation": {
              "cancellationDate": "2023-01-03",
              "cancellationReason": "Mistake"
            }
          }
        ],
        "premises": {
          "name": "Test Premises",
          "startDate": "2023-01-04",
          "endDate": "2023-01-05",
          "addressLine1": "123 Test Street",
          "addressLine2": "Test Road",
          "town": "Test Town",
          "postcode": "Test Postcode"
        },
        "uiUrl": "$cas3ReferralUrl"
      }
    },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":{"type":"ADD_DTR_OUTCOME","startDate":null, "service": "DTR"},
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
            "failureReasons":["SUITABLE_CAS1_APPLICATION","SUITABLE_CAS3_APPLICATION"],
            "blockingStatusReason": null
         }
      },
      "ppc":{
         "serviceResult":{
            "serviceStatus":"PPC_NOT_STARTED",
            "action":{"type":"START_PPC","startDate":null,"service":"PPC"},
            "link":"Start public protection checker",
            "url":null,
            "linkType":"PPC_START_CHECKER",
            "failureReasons":[],
            "blockingStatusReason":null
         }
      },
      "caseActions":[
         {"type":"ADD_DTR_OUTCOME","startDate":null, "service": "DTR"},
         {"type":"START_PPC","startDate":null,"service":"PPC"},
         {"type":"CREATE_PLACEMENT","startDate":null, "service": "CAS1"}
      ]
   }
}
""".trimIndent()

fun expectedGetEligibilityUpstreamFailuresResponse(
  crn: String,
  upstreamUrl: String,
): String = """
{
   "data":{
      "crn":"$crn",
      "cas1":{
         "serviceResult":{
            "serviceStatus":"UPSTREAM_ERROR",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         },
         "cas1Application":null
      },
      "cas2":{
         "serviceResult":{
            "serviceStatus":"UPSTREAM_ERROR",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         },
         "cas2Application":null
      },
      "cas3":{
         "serviceResult":{
            "serviceStatus":"UPSTREAM_ERROR",
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
            "serviceStatus":"UPSTREAM_ERROR",
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
            "serviceStatus":"UPSTREAM_ERROR",
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
            "serviceStatus":"UPSTREAM_ERROR",
            "action":null,
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         }
      },
      "ppc":{
         "serviceResult":{
            "serviceStatus":"UPSTREAM_ERROR",
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
         "endpoint":"getTierV3ByCrn",
         "failureType":"UPSTREAM_HTTP_ERROR",
         "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
         "message": "500 Internal Server Error from GET $upstreamUrl",
         "identifier":null
      }
   ]
}
""".trimIndent()

fun expectedGetEligibilityResponseTierNotFound(
  crn: String,
  cas1ApplicationId: UUID,
  cas3ApplicationId: UUID,
  cas3ApplicationCreatedAt: String,
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
  cas1ApplicationSubmittedAt: String,
  cas1RequestForPlacementExpectedArrivalDate: String,
  cas1PlacementExpectedDepartureDate: String,
  cas1SubmittedAt: String,
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
          "submittedAt": "$cas1ApplicationSubmittedAt",
          "expiresAt": null
        },
        "assessment": null,
        "requestForPlacement": {
          "status": "PLACEMENT_BOOKED",
          "decision": null,
          "rejectionReason": null,
          "submittedBy":{"name":"Test Tester","username":"testTester","staffCode":"1234"},
          "submittedAt": "$cas1SubmittedAt",
          "withdrawalReason": null,
          "withdrawalDate": null,
          "expectedArrivalDate": "$cas1RequestForPlacementExpectedArrivalDate",
          "durationDays": null
        },
        "placement": {
          "status": "ARRIVED",
          "actualArrivalDate": null,
          "actualDepartureDate": null,
          "expectedDepartureDate": "$cas1PlacementExpectedDepartureDate",
          "cancellationReason": null,
          "premises": {
            "startDate": "2025-01-01",
            "endDate": "2025-07-01",
            "addressLine1": "TEST HOUSE",
            "addressLine2": null,
            "town": null,
            "postcode": "TEST POSTCODE"
          }
        },
        "placementHistory": [],
        "id": "$cas1ApplicationId",
        "applicationStatus": "PLACEMENT_ALLOCATED",
        "requestForPlacementStatus": "PLACEMENT_BOOKED",
        "placementStatus": "ARRIVED"
      }
    },
    "cas2": {
        "serviceResult": {
            "serviceStatus": "NOT_ELIGIBLE",
            "action": null,
            "link": null,
            "url": null,
            "linkType": null,
            "failureReasons": [
                "INVALID_TIER"
            ],
            "blockingStatusReason": null
        },
        "cas2Application": null
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "SUBMITTED",
        "action": null,
        "link": "View referral",
        "url": "$cas3ReferralUrl",
        "linkType": "CAS3_VIEW_REFERRAL",
        "failureReasons": [],
        "blockingStatusReason":null
      },
      "cas3Application": {
        "id": "$cas3ApplicationId",
        "createdAt": "$cas3ApplicationCreatedAt",
        "applicationStatus": "SUBMITTED",
        "applicationSubmittedDate": "2025-01-02",
        "applicationSubmittedBy": {
          "name": "Test Tester",
          "username": "TestTester",
          "staffCode": "Test1234"
        },
        "applicationRejectedReason": null,
        "assessmentStatus": "UNALLOCATED",
        "bookingStatus": null,
        "cancellation": null,
        "bookingProvisionalOfferSentDate": null,
        "previousBookings": [],
        "premises": null,
        "uiUrl": "$cas3ReferralUrl"
      }
    },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"SUBMITTED",
            "action":{"type":"ADD_DTR_OUTCOME","startDate":null, "service": "DTR"},
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
      "ppc":{
         "serviceResult":{
            "serviceStatus":"PPC_NOT_STARTED",
            "action":{"type":"START_PPC","startDate":null,"service":"PPC"},
            "link":"Start public protection checker",
            "url":null,
            "linkType":"PPC_START_CHECKER",
            "failureReasons":[],
            "blockingStatusReason":null
         }
      },
      "caseActions":[
         {"type":"ADD_DTR_OUTCOME","startDate":null, "service": "DTR"},
         {"type":"START_PPC","startDate":null,"service":"PPC"}
      ]
   }
}
""".trimIndent()

fun expectedGetEligibilityResponseCannotStartYet(
  crn: String,
  cas3ApplicationId: UUID,
  cas3ApplicationCreatedAt: String,
  cas3Url: String,
  crsSubmissionDate: String,
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
            "url": null,
            "linkType":null,
            "failureReasons":["MALE_NOT_HIGH_RISK_TIER"],
            "blockingStatusReason":null
         },
         "cas1Application": null
      },
      "cas2": {
        "serviceResult": {
          "serviceStatus": "NOT_STARTED",
          "action":{
          "type":"START_CAS2_REFERRAL",
          "startDate":null,"service":"CAS2"
          },
          "link":"Start application",
          "url": null,
          "linkType": "CAS2_START_APPLICATION",
          "failureReasons": [],
          "blockingStatusReason": null
        },
        "cas2Application": null
      },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CANNOT_START_YET",
        "action": null,
        "link": null,
        "url": null,
        "linkType": null,
        "failureReasons": [
          "DTR_REFERRAL_EXPIRED"
        ],
        "blockingStatusReason": "SUBMIT_DTR_BEFORE_CAS3"
      },
      "cas3Application": {
        "id": "$cas3ApplicationId",
        "createdAt": "$cas3ApplicationCreatedAt",
        "applicationStatus": "REJECTED",
        "applicationSubmittedDate": "2025-01-02",
        "applicationSubmittedBy": {
          "name": "Test Tester",
          "username": "TestTester",
          "staffCode": "Test1234"
        },
        "applicationRejectedReason": null,
        "assessmentStatus": null,
        "bookingStatus": null,
        "cancellation": null,
        "bookingProvisionalOfferSentDate": null,
        "previousBookings": [],
        "premises": null,
        "uiUrl": "$cas3Url"
      }
    },
      "dtr":{
         "serviceResult":{
            "serviceStatus":"NOT_STARTED",
            "action":{"type":"ADD_DTR_REFERRAL_DETAILS","startDate":null, "service": "DTR"},
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
            "status":"LIVE",
            "submissionDate":"$crsSubmissionDate"
         }
      },
      "pa":{
         "serviceResult":{
            "serviceStatus":"NOT_STARTED",
            "action":{"type":"ADD_AND_CONFIRM_PROPOSED_ADDRESS","startDate":null, "service": "PA"},
            "link":null,
            "url":null,
            "linkType":null,
            "failureReasons":[],
            "blockingStatusReason":null
         }
      },
      "ppc":{
         "serviceResult":{
            "serviceStatus":"PPC_NOT_STARTED",
            "action":{"type":"START_PPC","startDate":null,"service":"PPC"},
            "link":"Start public protection checker",
            "url":null,
            "linkType":"PPC_START_CHECKER",
            "failureReasons":[],
            "blockingStatusReason":null
         }
      },
      "caseActions":[
         {"type":"ADD_DTR_REFERRAL_DETAILS","startDate":null, "service": "DTR"},
         {"type":"START_PPC","startDate":null,"service":"PPC"},
         {"type": "START_CAS2_REFERRAL", "startDate": null, "service": "CAS2"},
         {"type":"ADD_AND_CONFIRM_PROPOSED_ADDRESS","startDate":null, "service": "PA"}
      ]
   }
}
""".trimIndent()

fun expectedGetEligibilityV2Response(
  crn: String,
  cas1CreatedAt: String,
  cas1SubmittedAt: String,
  cas1ExpiresAt: String,
  requestSubmittedAt: String,
  expectedArrivalDate: String,
  expectedDepartureDate: String,
  placementStartDate: String,
  placementEndDate: String,
  cas3CreatedAt: String,
  dtrId: String,
  areaId: String,
  areaName: String,
  dtrSubmissionDate: String,
  dtrCreatedAt: String,
  crsUrl: String,
  crsSubmissionDate: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "CAS1_PLACEMENT_CANCELLED",
        "actionStartDate": null,
        "url": "https://cas1-ui/applications/e6b202ce-c214-4b87-98f5-111111111111",
        "failureReasons": []
      },
      "actionPosition": 2,
      "cas1Application": {
        "uiUrl": "https://cas1-ui/applications/e6b202ce-c214-4b87-98f5-111111111111",
        "application": {
          "id": "e6b202ce-c214-4b87-98f5-111111111111",
          "status": "PLACEMENT_ALLOCATED",
          "createdAt": "$cas1CreatedAt",
          "createdBy": {
            "name": "Bob",
            "username": "Bob123",
            "staffCode": "123"
          },
          "submittedAt": "$cas1SubmittedAt",
          "expiresAt": "$cas1ExpiresAt"
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
          "status": "CANCELLED",
          "actualArrivalDate": null,
          "actualDepartureDate": null,
          "expectedDepartureDate": "$expectedDepartureDate",
          "cancellationReason": "Oops",
          "premises": {
            "startDate": "$placementStartDate",
            "endDate": "$placementEndDate",
            "addressLine1": "Test House",
            "addressLine2": "Test Road",
            "town": "Test Town",
            "postcode": "Test Postcode"
          }
        },
        "placementHistory": [],
        "id": "e6b202ce-c214-4b87-98f5-111111111111",
        "applicationStatus": "PLACEMENT_ALLOCATED",
        "requestForPlacementStatus": "PLACEMENT_BOOKED",
        "placementStatus": "CANCELLED"
      }
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CAS2_CANCELLED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas2Application": {
        "uiUrl": "https://cas2-ui/applications/e6b202ce-c214-4b87-98f7-111111111111",
        "id": "e6b202ce-c214-4b87-98f7-111111111111",
        "createdAt": "2024-01-01T12:00:00Z",
        "createdBy": {
          "name": "Anne",
          "username": "anne.smith",
          "deliusStaffCode": "ANNE",
          "nomisStaffId": null,
          "userType": "DELIUS"
        },
        "submittedApplication": {
          "latestAssessmentStatus": "CANCELLED",
          "offerDeclinedReason": null,
          "cancelledReason": "cancelled reason",
          "submittedAt": "2024-01-02T12:00:00Z",
          "markedAsArrived": null
        },
        "cohort": "INTENSIVE_SUPERVISION_COURTS"
      }
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CAS3_BOOKING_CANCELLED",
        "actionStartDate": null,
        "url": "https://cas3-ui/referrals/e6b202ce-c214-4b87-98f6-111111111111/full",
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas3Application": {
        "id": "e6b202ce-c214-4b87-98f6-111111111111",
        "createdAt": "$cas3CreatedAt",
        "applicationStatus": "SUBMITTED",
        "applicationSubmittedDate": "2023-01-01",
        "applicationSubmittedBy": {
          "name": "Test Tester",
          "username": "TestTester",
          "staffCode": "Test1234"
        },
        "applicationRejectedReason": "Oops",
        "assessmentStatus": "REJECTED",
        "bookingStatus": "CANCELLED",
        "cancellation": {
          "cancellationDate": "2023-01-03",
          "cancellationReason": "Mistake"
        },
        "bookingProvisionalOfferSentDate": "2023-01-02",
        "previousBookings": [
          {
            "bookingStatus": "CANCELLED",
            "cancellation": {
              "cancellationDate": "2023-01-03",
              "cancellationReason": "Mistake"
            }
          }
        ],
        "premises": {
          "name": "Test Premises",
          "startDate": "2023-01-04",
          "endDate": "2023-01-05",
          "addressLine1": "123 Test Street",
          "addressLine2": "Test Road",
          "town": "Test Town",
          "postcode": "Test Postcode"
        },
        "uiUrl": "https://cas3-ui/referrals/e6b202ce-c214-4b87-98f6-111111111111/full"
      }
    },
    "dtr": {
      "serviceResult": {
        "serviceStatus": "DTR_SUBMITTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 0,
      "caseId": "e6b202ce-c214-4b87-98f5-111111111112",
      "submission": {
        "id": "$dtrId",
        "localAuthority": {
          "localAuthorityAreaId": "$areaId",
          "localAuthorityAreaName": "$areaName"
        },
        "referenceNumber": "DTR-REF-001",
        "submissionDate": "$dtrSubmissionDate",
        "createdBy": "Test Data Setup User",
        "createdAt": "$dtrCreatedAt",
        "withdrawalReason": null,
        "withdrawalReasonOther": null,
        "outcomeReason": null,
        "submissionNote": null,
        "outcomeNote": null
      }
    },
    "crs": {
      "serviceResult": {
        "serviceStatus": "CRS_SUBMITTED",
        "actionStartDate": null,
        "url": "$crsUrl",
        "failureReasons": []
      },
      "actionPosition": -1,
      "commissionedRehabilitativeServices": {
        "status": "LIVE",
        "submissionDate": "$crsSubmissionDate"
      }
    },
    "pa": {
      "serviceResult": {
        "serviceStatus": "PA_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "SUITABLE_CAS1_APPLICATION",
          "SUITABLE_CAS3_APPLICATION"
        ]
      },
      "actionPosition": -1
    },
    "ppc": {
      "serviceResult": {
        "serviceStatus": "PPC_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 1
    }
  }
}
""".trimIndent()

fun expectedGetEligibilityV2Cas3BlockedResponse(
  crn: String,
  cas3CreatedAt: String,
  crsUrl: String,
  crsSubmissionDate: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "CAS1_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "MALE_NOT_HIGH_RISK_TIER"
        ]
      },
      "actionPosition": -1,
      "cas1Application": null
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CAS2_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 2,
      "cas2Application": null
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CAS3_CANNOT_START_YET_SUBMIT_DTR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "DTR_REFERRAL_EXPIRED"
        ]
      },
      "actionPosition": -1,
      "cas3Application": {
        "id": "e6b202ce-c214-4b87-98f6-111111111111",
        "createdAt": "$cas3CreatedAt",
        "applicationStatus": "REJECTED",
        "applicationSubmittedDate": "2025-01-02",
        "applicationSubmittedBy": {
          "name": "Test Tester",
          "username": "TestTester",
          "staffCode": "Test1234"
        },
        "applicationRejectedReason": null,
        "assessmentStatus": null,
        "bookingStatus": null,
        "cancellation": null,
        "bookingProvisionalOfferSentDate": null,
        "previousBookings": [],
        "premises": null,
        "uiUrl": "https://cas3-ui/referrals/e6b202ce-c214-4b87-98f6-111111111111/full"
      }
    },
    "dtr": {
      "serviceResult": {
        "serviceStatus": "DTR_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 0,
      "caseId": null,
      "submission": null
    },
    "crs": {
      "serviceResult": {
        "serviceStatus": "CRS_SUBMITTED",
        "actionStartDate": null,
        "url": "$crsUrl",
        "failureReasons": []
      },
      "actionPosition": -1,
      "commissionedRehabilitativeServices": {
        "status": "LIVE",
        "submissionDate": "$crsSubmissionDate"
      }
    },
    "pa": {
      "serviceResult": {
        "serviceStatus": "PA_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 3
    },
    "ppc": {
      "serviceResult": {
        "serviceStatus": "PPC_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 1
    }
  }
}
""".trimIndent()

fun expectedGetEligibilityV2TierNotFoundResponse(
  crn: String,
  cas1CreatedAt: String,
  cas1SubmittedAt: String,
  requestSubmittedAt: String,
  expectedArrivalDate: String,
  expectedDepartureDate: String,
  cas3CreatedAt: String,
  dtrId: String,
  areaId: String,
  areaName: String,
  dtrSubmissionDate: String,
  dtrCreatedAt: String,
  crsUrl: String,
  crsSubmissionDate: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "CAS1_ARRIVED",
        "actionStartDate": null,
        "url": "https://cas1-ui/applications/e6b202ce-c214-4b87-98f5-111111111111",
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas1Application": {
        "uiUrl": "https://cas1-ui/applications/e6b202ce-c214-4b87-98f5-111111111111",
        "application": {
          "id": "e6b202ce-c214-4b87-98f5-111111111111",
          "status": "PLACEMENT_ALLOCATED",
          "createdAt": "$cas1CreatedAt",
          "createdBy": {
            "name": "Test Tester",
            "username": "testTester",
            "staffCode": "1234"
          },
          "submittedAt": "$cas1SubmittedAt",
          "expiresAt": null
        },
        "assessment": null,
        "requestForPlacement": {
          "status": "PLACEMENT_BOOKED",
          "decision": null,
          "rejectionReason": null,
          "submittedBy": {
            "name": "Test Tester",
            "username": "testTester",
            "staffCode": "1234"
          },
          "submittedAt": "$requestSubmittedAt",
          "withdrawalReason": null,
          "withdrawalDate": null,
          "expectedArrivalDate": "$expectedArrivalDate",
          "durationDays": null
        },
        "placement": {
          "status": "ARRIVED",
          "actualArrivalDate": null,
          "actualDepartureDate": null,
          "expectedDepartureDate": "$expectedDepartureDate",
          "cancellationReason": null,
          "premises": {
            "startDate": "2025-01-01",
            "endDate": "2025-07-01",
            "addressLine1": "TEST HOUSE",
            "addressLine2": null,
            "town": null,
            "postcode": "TEST POSTCODE"
          }
        },
        "placementHistory": [],
        "id": "e6b202ce-c214-4b87-98f5-111111111111",
        "applicationStatus": "PLACEMENT_ALLOCATED",
        "requestForPlacementStatus": "PLACEMENT_BOOKED",
        "placementStatus": "ARRIVED"
      }
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CAS2_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "INVALID_TIER"
        ]
      },
      "actionPosition": -1,
      "cas2Application": null
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CAS3_SUBMITTED",
        "actionStartDate": null,
        "url": "https://cas3-ui/referrals/e6b202ce-c214-4b87-98f6-111111111111/full",
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas3Application": {
        "id": "e6b202ce-c214-4b87-98f6-111111111111",
        "createdAt": "$cas3CreatedAt",
        "applicationStatus": "SUBMITTED",
        "applicationSubmittedDate": "2025-01-02",
        "applicationSubmittedBy": {
          "name": "Test Tester",
          "username": "TestTester",
          "staffCode": "Test1234"
        },
        "applicationRejectedReason": null,
        "assessmentStatus": "UNALLOCATED",
        "bookingStatus": null,
        "cancellation": null,
        "bookingProvisionalOfferSentDate": null,
        "previousBookings": [],
        "premises": null,
        "uiUrl": "https://cas3-ui/referrals/e6b202ce-c214-4b87-98f6-111111111111/full"
      }
    },
    "dtr": {
      "serviceResult": {
        "serviceStatus": "DTR_SUBMITTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 0,
      "caseId": "e6b202ce-c214-4b87-98f5-111111111112",
      "submission": {
        "id": "$dtrId",
        "localAuthority": {
          "localAuthorityAreaId": "$areaId",
          "localAuthorityAreaName": "$areaName"
        },
        "referenceNumber": "DTR-REF-001",
        "submissionDate": "$dtrSubmissionDate",
        "createdBy": "Test Data Setup User",
        "createdAt": "$dtrCreatedAt",
        "withdrawalReason": null,
        "withdrawalReasonOther": null,
        "outcomeReason": null,
        "submissionNote": null,
        "outcomeNote": null
      }
    },
    "crs": {
      "serviceResult": {
        "serviceStatus": "CRS_SUBMITTED",
        "actionStartDate": null,
        "url": "$crsUrl",
        "failureReasons": []
      },
      "actionPosition": -1,
      "commissionedRehabilitativeServices": {
        "status": "LIVE",
        "submissionDate": "$crsSubmissionDate"
      }
    },
    "pa": {
      "serviceResult": {
        "serviceStatus": "PA_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "SUITABLE_CAS1_APPLICATION",
          "SUITABLE_CAS3_APPLICATION"
        ]
      },
      "actionPosition": -1
    },
    "ppc": {
      "serviceResult": {
        "serviceStatus": "PPC_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 1
    }
  }
}
""".trimIndent()

fun expectedGetEligibilityV2TierErrorResponse(
  crn: String,
  wireMockBaseUrl: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "CAS1_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas1Application": null
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CAS2_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas2Application": null
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CAS3_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas3Application": null
    },
    "dtr": {
      "serviceResult": {
        "serviceStatus": "DTR_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "caseId": null,
      "submission": null
    },
    "crs": {
      "serviceResult": {
        "serviceStatus": "CRS_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "commissionedRehabilitativeServices": null
    },
    "pa": {
      "serviceResult": {
        "serviceStatus": "PA_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1
    },
    "ppc": {
      "serviceResult": {
        "serviceStatus": "PPC_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1
    }
  },
  "upstreamFailures": [
    {
      "endpoint": "getTierV3ByCrn",
      "failureType": "UPSTREAM_HTTP_ERROR",
      "httpResponseStatus": "500 INTERNAL_SERVER_ERROR",
      "message": "500 Internal Server Error from GET $wireMockBaseUrl/v3/crn/$crn/tier",
      "identifier": null
    }
  ]
}
""".trimIndent()

fun expectedGetEligibilityV2FailureReasonResponse(
  crn: String,
  cas3CreatedAt: String,
  dtrId: String,
  areaId: String,
  areaName: String,
  dtrSubmissionDate: String,
  dtrCreatedAt: String,
  crsUrl: String,
  crsSubmissionDate: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "CAS1_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "MALE_NOT_HIGH_RISK_TIER"
        ]
      },
      "actionPosition": -1,
      "cas1Application": {
        "uiUrl": "https://cas1-ui/applications/e6b202ce-c214-4b87-98f5-111111111111",
        "application": {
          "id": "e6b202ce-c214-4b87-98f5-111111111111",
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
        "id": "e6b202ce-c214-4b87-98f5-111111111111",
        "applicationStatus": "REJECTED",
        "requestForPlacementStatus": null,
        "placementStatus": null
      }
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CAS2_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "HAS_NO_ROSH_LEVEL"
        ]
      },
      "actionPosition": -1,
      "cas2Application": null
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CAS3_SUBMITTED",
        "actionStartDate": null,
        "url": "https://cas3-ui/referrals/e6b202ce-c214-4b87-98f6-111111111111/full",
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas3Application": {
        "id": "e6b202ce-c214-4b87-98f6-111111111111",
        "createdAt": "$cas3CreatedAt",
        "applicationStatus": "SUBMITTED",
        "applicationSubmittedDate": "2025-01-02",
        "applicationSubmittedBy": {
          "name": "Test Tester",
          "username": "TestTester",
          "staffCode": "Test1234"
        },
        "applicationRejectedReason": null,
        "assessmentStatus": "UNALLOCATED",
        "bookingStatus": null,
        "cancellation": null,
        "bookingProvisionalOfferSentDate": null,
        "previousBookings": [],
        "premises": null,
        "uiUrl": "https://cas3-ui/referrals/e6b202ce-c214-4b87-98f6-111111111111/full"
      }
    },
    "dtr": {
      "serviceResult": {
        "serviceStatus": "DTR_SUBMITTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 0,
      "caseId": "e6b202ce-c214-4b87-98f5-111111111112",
      "submission": {
        "id": "$dtrId",
        "localAuthority": {
          "localAuthorityAreaId": "$areaId",
          "localAuthorityAreaName": "$areaName"
        },
        "referenceNumber": "DTR-REF-001",
        "submissionDate": "$dtrSubmissionDate",
        "createdBy": "Test Data Setup User",
        "createdAt": "$dtrCreatedAt",
        "withdrawalReason": null,
        "withdrawalReasonOther": null,
        "outcomeReason": null,
        "submissionNote": null,
        "outcomeNote": null
      }
    },
    "crs": {
      "serviceResult": {
        "serviceStatus": "CRS_SUBMITTED",
        "actionStartDate": null,
        "url": "$crsUrl",
        "failureReasons": []
      },
      "actionPosition": -1,
      "commissionedRehabilitativeServices": {
        "status": "LIVE",
        "submissionDate": "$crsSubmissionDate"
      }
    },
    "pa": {
      "serviceResult": {
        "serviceStatus": "PA_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "SUITABLE_CAS3_APPLICATION"
        ]
      },
      "actionPosition": -1
    },
    "ppc": {
      "serviceResult": {
        "serviceStatus": "PPC_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 1
    }
  }
}
""".trimIndent()

fun expectedGetEligibilityV2NoPrisonerResponse(
  crn: String,
  cas3CreatedAt: String,
  crsUrl: String,
  crsSubmissionDate: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "CAS1_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "HAS_NO_ROSH_LEVEL"
        ]
      },
      "actionPosition": -1,
      "cas1Application": null
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CAS2_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "HAS_NO_ROSH_LEVEL"
        ]
      },
      "actionPosition": -1,
      "cas2Application": null
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CAS3_SUBMITTED",
        "actionStartDate": null,
        "url": "https://cas3-ui/referrals/e6b202ce-c214-4b87-98f6-111111111111/full",
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas3Application": {
        "id": "e6b202ce-c214-4b87-98f6-111111111111",
        "createdAt": "$cas3CreatedAt",
        "applicationStatus": "SUBMITTED",
        "applicationSubmittedDate": "2025-01-02",
        "applicationSubmittedBy": {
          "name": "Test Tester",
          "username": "TestTester",
          "staffCode": "Test1234"
        },
        "applicationRejectedReason": null,
        "assessmentStatus": "UNALLOCATED",
        "bookingStatus": null,
        "cancellation": null,
        "bookingProvisionalOfferSentDate": null,
        "previousBookings": [],
        "premises": null,
        "uiUrl": "https://cas3-ui/referrals/e6b202ce-c214-4b87-98f6-111111111111/full"
      }
    },
    "dtr": {
      "serviceResult": {
        "serviceStatus": "DTR_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 0,
      "caseId": null,
      "submission": null
    },
    "crs": {
      "serviceResult": {
        "serviceStatus": "CRS_SUBMITTED",
        "actionStartDate": null,
        "url": "$crsUrl",
        "failureReasons": []
      },
      "actionPosition": -1,
      "commissionedRehabilitativeServices": {
        "status": "LIVE",
        "submissionDate": "$crsSubmissionDate"
      }
    },
    "pa": {
      "serviceResult": {
        "serviceStatus": "PA_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "SUITABLE_CAS3_APPLICATION"
        ]
      },
      "actionPosition": -1
    },
    "ppc": {
      "serviceResult": {
        "serviceStatus": "PPC_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 1
    }
  }
}
""".trimIndent()

fun expectedGetEligibilityV2ApplicationsNotFoundResponse(
  crn: String,
  wireMockBaseUrl: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "CAS1_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas1Application": null
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CAS2_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas2Application": null
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CAS3_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas3Application": null
    },
    "dtr": {
      "serviceResult": {
        "serviceStatus": "DTR_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "caseId": null,
      "submission": null
    },
    "crs": {
      "serviceResult": {
        "serviceStatus": "CRS_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "commissionedRehabilitativeServices": null
    },
    "pa": {
      "serviceResult": {
        "serviceStatus": "PA_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1
    },
    "ppc": {
      "serviceResult": {
        "serviceStatus": "PPC_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1
    }
  },
  "upstreamFailures": [
    {
      "endpoint": "getCas1CurrentApplication",
      "failureType": "UPSTREAM_HTTP_ERROR",
      "httpResponseStatus": "404 NOT_FOUND",
      "message": "404 Not Found from GET $wireMockBaseUrl/cas1/external/cases/$crn/applications/current",
      "identifier": null
    },
    {
      "endpoint": "getCas2CurrentApplication",
      "failureType": "UPSTREAM_HTTP_ERROR",
      "httpResponseStatus": "404 NOT_FOUND",
      "message": "404 Not Found from GET $wireMockBaseUrl/cas2/external/cases/$crn/applications/current",
      "identifier": null
    },
    {
      "endpoint": "getCas3CurrentApplication",
      "failureType": "UPSTREAM_HTTP_ERROR",
      "httpResponseStatus": "404 NOT_FOUND",
      "message": "404 Not Found from GET $wireMockBaseUrl/cas3/external/cases/$crn/applications/current",
      "identifier": null
    }
  ]
}
""".trimIndent()

fun expectedGetEligibilityV2ApplicationsNoContentResponse(
  crn: String,
  crsUrl: String,
  crsSubmissionDate: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "CAS1_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "MALE_NOT_HIGH_RISK_TIER"
        ]
      },
      "actionPosition": -1,
      "cas1Application": null
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CAS2_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "INVALID_TIER"
        ]
      },
      "actionPosition": -1,
      "cas2Application": null
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CAS3_NOT_ELIGIBLE",
        "actionStartDate": null,
        "url": null,
        "failureReasons": [
          "HAS_NO_ROSH_LEVEL"
        ]
      },
      "actionPosition": -1,
      "cas3Application": null
    },
    "dtr": {
      "serviceResult": {
        "serviceStatus": "DTR_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 0,
      "caseId": null,
      "submission": null
    },
    "crs": {
      "serviceResult": {
        "serviceStatus": "CRS_SUBMITTED",
        "actionStartDate": null,
        "url": "$crsUrl",
        "failureReasons": []
      },
      "actionPosition": -1,
      "commissionedRehabilitativeServices": {
        "status": "LIVE",
        "submissionDate": "$crsSubmissionDate"
      }
    },
    "pa": {
      "serviceResult": {
        "serviceStatus": "PA_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 2
    },
    "ppc": {
      "serviceResult": {
        "serviceStatus": "PPC_NOT_STARTED",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": 1
    }
  }
}
""".trimIndent()

fun expectedGetEligibilityV2PremisesNotFoundResponse(
  crn: String,
  wireMockBaseUrl: String,
): String = """
{
  "data": {
    "crn": "$crn",
    "cas1": {
      "serviceResult": {
        "serviceStatus": "CAS1_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas1Application": null
    },
    "cas2": {
      "serviceResult": {
        "serviceStatus": "CAS2_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas2Application": null
    },
    "cas3": {
      "serviceResult": {
        "serviceStatus": "CAS3_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "cas3Application": null
    },
    "dtr": {
      "serviceResult": {
        "serviceStatus": "DTR_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "caseId": null,
      "submission": null
    },
    "crs": {
      "serviceResult": {
        "serviceStatus": "CRS_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1,
      "commissionedRehabilitativeServices": null
    },
    "pa": {
      "serviceResult": {
        "serviceStatus": "PA_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1
    },
    "ppc": {
      "serviceResult": {
        "serviceStatus": "PPC_UPSTREAM_ERROR",
        "actionStartDate": null,
        "url": null,
        "failureReasons": []
      },
      "actionPosition": -1
    }
  },
  "upstreamFailures": [
    {
      "endpoint": "getCas1ResidentPlacement",
      "failureType": "UPSTREAM_HTTP_ERROR",
      "httpResponseStatus": "404 NOT_FOUND",
      "message": "404 Not Found from GET $wireMockBaseUrl/cas1/external/cases/$crn/residence",
      "identifier": null
    },
    {
      "endpoint": "getCas3CurrentPremises",
      "failureType": "UPSTREAM_HTTP_ERROR",
      "httpResponseStatus": "404 NOT_FOUND",
      "message": "404 Not Found from GET $wireMockBaseUrl/cas3/external/cases/$crn/premises/current",
      "identifier": null
    }
  ]
}
""".trimIndent()
