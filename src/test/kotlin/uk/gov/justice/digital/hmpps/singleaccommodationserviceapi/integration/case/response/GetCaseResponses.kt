package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCaseResponse(): String = """
{
  "data": {
    "forename": "First",
    "middleNames": "Middle",
    "surname": "Last",
    "dateOfBirth": "2000-12-03",
    "crn": "FAKECRN1",
    "prisonNumber": "PRI1",
    "photoUrl": null,
    "tierScore": "A1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "forename": "First",
      "surname": "Last",
      "username": "user1"
    },
    "userAccess": "FULL",
    "limitedAccess": false,
    "accommodationSummaries": null
  }
}
""".trimIndent()

@TestData
fun expectedGetCaseResponseSearch(): String = """
{
  "data": {
    "forename": "First",
    "middleNames": null,
    "surname": "Last",
    "dateOfBirth": "2000-12-03",
    "crn": "A123456",
    "prisonNumber": "PRI6",
    "photoUrl": null,
    "tierScore": "A1",
    "riskLevel": "VERY_HIGH",
    "pncReference": "Some PNC Reference",
    "assignedTo": {
      "forename": "First",
      "surname": "Last",
      "username": "user1"
    },
    "userAccess": "FULL",
    "limitedAccess": false,
    "accommodationSummaries": {
      "caseAccommodationStatus": "SETTLED",
      "caseAccommodationStatusDate": null,
      "currentAccommodation": {
        "crn": "X12345",
        "startDate": null,
        "endDate": null,
        "address": {
          "postcode": "SW1A 1AA",
          "subBuildingName": "The Sub-Building",
          "buildingName": "The Building",
          "buildingNumber": "123",
          "thoroughfareName": "The Road",
          "dependentLocality": "The Area",
          "postTown": "London",
          "county": "London",
          "country": "England",
          "uprn": "1234567890"
        },
        "status": {
          "code": "M",
          "description": "Main"
        },
        "type": {
          "code": "A02",
          "description": "Approved Premises"
        },
        "proposedAccommodationId": null
      },
      "nextAccommodation": {
        "crn": "X12345",
        "startDate": null,
        "endDate": null,
        "address": {
          "postcode": "SW1A 1AA",
          "subBuildingName": "The Sub-Building",
          "buildingName": "The Building",
          "buildingNumber": "123",
          "thoroughfareName": "The Road",
          "dependentLocality": "The Area",
          "postTown": "London",
          "county": "London",
          "country": "England",
          "uprn": "1234567890"
        },
        "status": {
          "code": "M",
          "description": "Main"
        },
        "type": {
          "code": "A02",
          "description": "Approved Premises"
        },
        "proposedAccommodationId": null
      }
    }
  }
}
""".trimIndent()
