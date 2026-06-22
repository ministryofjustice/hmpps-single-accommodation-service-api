package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCaseListResponse(): String = """
{
   "data":[
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN1",
         "prisonNumber":"PRI1",
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"Zack Middle Smith",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN2",
         "prisonNumber":"PRI2",
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":"MEDIUM",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN3",
         "prisonNumber":"PRI3",
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN4",
         "prisonNumber":"PRI4",
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN5",
         "prisonNumber":"PRI5",
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN6",
         "prisonNumber":"PRI6",
         "photoUrl":null,
         "tierScore":"A1",
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS referral",
            "Start an approved premises (CAS1) application",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN7",
         "prisonNumber":"PRI7",
         "photoUrl":null,
         "tierScore":"A1S",
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS referral"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN8",
         "prisonNumber":"PRI8",
         "photoUrl":null,
         "tierScore":"C1",
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS referral"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN9",
         "prisonNumber":"PRI9",
         "photoUrl":null,
         "tierScore":"B3",
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS referral"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN10",
         "prisonNumber":"PRI10",
         "photoUrl":null,
         "tierScore":"B3",
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN11",
         "prisonNumber":"PRI11",
         "photoUrl":null,
         "tierScore":"B3",
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Provide further information on an approved premises (CAS1) application"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN12",
         "prisonNumber":"PRI12",
         "photoUrl":null,
         "tierScore":"B3",
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Start an approved premises (CAS1) application",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN13",
         "prisonNumber":"PRI13",
         "photoUrl":null,
         "tierScore":"B3",
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Continue an approved premises (CAS1) application",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN14",
         "prisonNumber":"PRI14",
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN15",
         "prisonNumber":"PRI15",
         "photoUrl":null,
         "tierScore":"D3",
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN16",
         "prisonNumber":"PRI16",
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN17",
         "prisonNumber":"PRI17",
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":"First Middle Last",
         "dateOfBirth":"2000-12-03",
         "crn":"FAKECRN18",
         "prisonNumber":"PRI18",
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":"VERY_HIGH",
         "pncReference":"Some PNC Reference",
         "assignedTo":{
            "forename":"First",
            "surname":"Last",
            "username":"DELIUS_USER",
            "staffCode":"ABCD1234"
         },
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            "Add DTR referral details",
            "Submit a CRS accommodation referral",
            "Add and confirm proposed address"
         ],
         "userAccess":"FULL",
         "limitedAccess":false
      },
      {
         "name":null,
         "dateOfBirth":null,
         "crn":"FAKECRN19",
         "prisonNumber":null,
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":null,
         "pncReference":null,
         "assignedTo":null,
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            
         ],
         "userAccess":"LIMITED",
         "limitedAccess":true
      },
      {
         "name":null,
         "dateOfBirth":null,
         "crn":"FAKECRN20",
         "prisonNumber":null,
         "photoUrl":null,
         "tierScore":null,
         "riskLevel":null,
         "pncReference":null,
         "assignedTo":null,
         "currentAccommodation":null,
         "nextAccommodation":null,
         "status":null,
         "actions":[
            
         ],
         "userAccess":"LIMITED",
         "limitedAccess":true
      }
   ],
   "upstreamFailures":[
      {
        "endpoint": "getCorePersonRecordByCrn",
        "failureType": "UPSTREAM_HTTP_ERROR",
        "httpResponseStatus": "404 NOT_FOUND",
        "message": "404 Not Found: [no body]",
        "identifier": { "type": "CRN", "value": "FAKECRN16" }
      },
      {
         "endpoint":"getCorePersonRecordByCrn",
         "failureType":"UPSTREAM_HTTP_ERROR",
         "httpResponseStatus":"500 INTERNAL_SERVER_ERROR",
         "message":"500 Internal Server Error: [no body]",
         "identifier":{
            "type":"CRN",
            "value":"FAKECRN17"
         }
      }
   ]
}
""".trimIndent()
