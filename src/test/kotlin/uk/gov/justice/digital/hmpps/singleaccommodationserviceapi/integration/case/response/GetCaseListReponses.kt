package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCaseListResponse(): String = """
{
  "data": [
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN1",
    "prisonNumber": "PRI1",
    "photoUrl": null,
    "tierScore": "A1",
    "tier": "A1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Start approved premise (CAS1) application"
    ]
  },
  {
    "name": "Zack Middle Smith",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN2",
    "prisonNumber": "PRI2",
    "photoUrl": null,
    "tierScore": "A1",
    "tier": "A1",
    "riskLevel": "MEDIUM",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Start approved premise (CAS1) application"
    ]
  },
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN3",
    "prisonNumber": "PRI3",
    "photoUrl": null,
    "tierScore": "A1",
    "tier": "A1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": []
  },
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN4",
    "prisonNumber": "PRI4",
    "photoUrl": null,
    "tierScore": null,
    "tier": null,
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": []
  },
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN5",
    "prisonNumber": "PRI5",
    "photoUrl": null,
    "tierScore": null,
    "tier": null,
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Wait for approved premise (CAS1) placement request result"
    ]
  },
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN6",
    "prisonNumber": "PRI6",
    "photoUrl": null,
    "tierScore": "A1",
    "tier": "A1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Start approved premise (CAS1) application"
    ]
  },
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN7",
    "prisonNumber": "PRI7",
    "photoUrl": null,
    "tierScore": "A1S",
    "tier": "A1S",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Wait for approved premise (CAS1) assessment result"
    ]
  },
  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN8",
    "prisonNumber": "PRI8",
    "photoUrl": null,
    "tierScore": "C1",
    "tier": "C1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Wait for approved premise (CAS1) assessment result"
    ]
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN9",
    "prisonNumber": "PRI9",
    "photoUrl": null,
    "tierScore": "B3",
    "tier": "B3",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Wait for approved premise (CAS1) assessment result"
    ]
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN10",
    "prisonNumber": "PRI10",
    "photoUrl": null,
    "tierScore": "B3",
    "tier": "B3",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": []
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN11",
    "prisonNumber": "PRI11",
    "photoUrl": null,
    "tierScore": "B3",
    "tier": "B3",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Provide further information on approved premise (CAS1) application"
    ]
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN12",
    "prisonNumber": "PRI12",
    "photoUrl": null,
    "tierScore": "B3",
    "tier": "B3",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Start approved premise (CAS1) application"
    ]
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN13",
    "prisonNumber": "PRI13",
    "photoUrl": null,
    "tierScore": "B3",
    "tier": "B3",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Continue approved premise (CAS1) application"
    ]
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN14",
    "prisonNumber": "PRI14",
    "photoUrl": null,
    "tierScore": null,
    "tier": null,
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": []
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN15",
    "prisonNumber": "PRI15",
    "photoUrl": null,
    "tierScore": "D3",
    "tier": "D3",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": []
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN16",
    "prisonNumber": "PRI16",
    "photoUrl": null,
    "tierScore": null,
    "tier": null,
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": []
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN17",
    "prisonNumber": "PRI17",
    "photoUrl": null,
    "tierScore": null,
    "tier": null,
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": []
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN18",
    "prisonNumber": "PRI18",
    "photoUrl": null,
    "tierScore": null,
    "tier": null,
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": []
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN19",
    "prisonNumber": "PRI19",
    "photoUrl": null,
    "tierScore": null,
    "tier": null,
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": []
  },

  {
    "name": "First Middle Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN20",
    "prisonNumber": "PRI20",
    "photoUrl": null,
    "tierScore": "A1",
    "tier": "A1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "id": 1,
      "name": "First Middle Last",
      "username": "user1",
      "staffCode": "ABCD1234"
    },
    "currentAccommodation": null,
    "nextAccommodation": null,
    "status": null,
    "actions": [
      "Start approved premise (CAS1) application"
    ]
  }
  ],
  "upstreamFailures": []
}
""".trimIndent()
