package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.privateaddress.response

fun expectedGetPrivateAddressesResponse(): String = """
[ {
  "id" : "6d9a38c4-a8f6-49d1-856d-972906f63361",
  "name" : null,
  "arrangementType" : "PRIVATE",
  "arrangementSubType" : "FRIENDS_OR_FAMILY",
  "arrangementSubTypeDescription" : null,
  "settledType" : "SETTLED",
  "offenderReleaseType" : null,
  "status" : "NOT_CHECKED_YET",
  "address" : {
    "postcode" : "RG26 5AG",
    "subBuildingName" : null,
    "buildingName" : null,
    "buildingNumber" : "4",
    "thoroughfareName" : "Dollis Green",
    "dependentLocality" : null,
    "postTown" : "Bramley",
    "county" : null,
    "country" : null,
    "uprn" : null
  },
  "startDate" : null,
  "endDate" : null,
  "createdAt" : "2026-01-08T13:27:15.120069Z"
}, {
  "id" : "f03aac3e-2f36-4003-a753-db571fe140b8",
  "name" : null,
  "arrangementType" : "PRIVATE",
  "arrangementSubType" : "FRIENDS_OR_FAMILY",
  "arrangementSubTypeDescription" : null,
  "settledType" : "SETTLED",
  "offenderReleaseType" : null,
  "status" : "NOT_CHECKED_YET",
  "address" : {
    "postcode" : "W1 8XX",
    "subBuildingName" : null,
    "buildingName" : null,
    "buildingNumber" : "11",
    "thoroughfareName" : "Piccadilly Circus",
    "dependentLocality" : null,
    "postTown" : "London",
    "county" : null,
    "country" : null,
    "uprn" : null
  },
  "startDate" : null,
  "endDate" : null,
  "createdAt" : "2026-01-05T10:07:15.120069Z"
} ]
""".trimIndent()
