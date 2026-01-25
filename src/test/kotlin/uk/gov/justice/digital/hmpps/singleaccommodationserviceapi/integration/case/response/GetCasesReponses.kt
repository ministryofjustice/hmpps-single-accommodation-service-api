package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCasesResponse(): String = """
[ {
  "name" : "First Middle Last",
  "dateOfBirth" : "2000-12-03",
  "crn" : "X371199",
  "prisonNumber" : "PRI1",
  "photoUrl" : "https://github.com/ministryofjustice/hmpps-single-accommodation-service-prototype/blob/main/app/assets/images/profile-placeholder-2.jpg",
  "tier" : "C1",
  "riskLevel" : "VERY_HIGH",
  "pncReference" : "Some PNC Reference",
  "assignedTo" : {
    "id" : 1,
    "name" : "Team 1"
  },
  "currentAccommodation" : {
    "id" : "f3813060-59c7-48ff-8729-3ea6efbf375b",
    "name" : "HMP Huntercombe",
    "arrangementType" : "PRISON",
    "arrangementSubType" : null,
    "arrangementSubTypeDescription" : null,
    "settledType" : "TRANSIENT",
    "offenderReleaseType" : null,
    "status" : null,
    "address" : {
      "postcode" : null,
      "subBuildingName" : null,
      "buildingName" : null,
      "buildingNumber" : null,
      "thoroughfareName" : null,
      "dependentLocality" : null,
      "postTown" : null,
      "county" : null,
      "country" : null,
      "uprn" : null
    },
    "startDate" : null,
    "endDate" : null,
    "createdAt" : "2023-01-02T11:07:09Z"
  },
  "nextAccommodation" : {
    "id" : "b697a854-96af-4360-a715-189a78d4f70f",
    "name" : null,
    "arrangementType" : "PRIVATE",
    "arrangementSubType" : "FRIENDS_OR_FAMILY",
    "arrangementSubTypeDescription" : null,
    "settledType" : "TRANSIENT",
    "offenderReleaseType" : null,
    "status" : "PASSED",
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
    "createdAt" : "2026-01-20T16:07:20Z"
  }
}, {
  "name" : "Zack Middle Smith",
  "dateOfBirth" : "2000-12-03",
  "crn" : "X968879",
  "prisonNumber" : "PRI1",
  "photoUrl" : "https://github.com/ministryofjustice/hmpps-single-accommodation-service-prototype/blob/main/app/assets/images/profile-placeholder-2.jpg",
  "tier" : "C1",
  "riskLevel" : "MEDIUM",
  "pncReference" : "Some PNC Reference",
  "assignedTo" : {
    "id" : 1,
    "name" : "Team 1"
  },
  "currentAccommodation" : {
    "id" : "f296b6a7-79c3-4d46-b5ed-683e72e9ae09",
    "name" : "HMP Bullingdon",
    "arrangementType" : "PRISON",
    "arrangementSubType" : null,
    "arrangementSubTypeDescription" : null,
    "settledType" : "TRANSIENT",
    "offenderReleaseType" : null,
    "status" : null,
    "address" : {
      "postcode" : null,
      "subBuildingName" : null,
      "buildingName" : null,
      "buildingNumber" : null,
      "thoroughfareName" : null,
      "dependentLocality" : null,
      "postTown" : null,
      "county" : null,
      "country" : null,
      "uprn" : null
    },
    "startDate" : null,
    "endDate" : null,
    "createdAt" : "2021-11-11T14:35:11Z"
  },
  "nextAccommodation" : {
    "id" : "fa75a728-1020-44d0-8bb6-343ca1197d2e",
    "name" : null,
    "arrangementType" : "PRIVATE",
    "arrangementSubType" : "SOCIAL_RENTED",
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
    "createdAt" : "2026-01-15T12:19:20Z"
  }
} ]
""".trimIndent()

fun expectedGetCasesWithFilterResponse(): String = """
 [ {
  "name" : "Zack Middle Smith",
  "dateOfBirth" : "2000-12-03",
  "crn" : "X968879",
  "prisonNumber" : "PRI1",
  "photoUrl" : "https://github.com/ministryofjustice/hmpps-single-accommodation-service-prototype/blob/main/app/assets/images/profile-placeholder-2.jpg",
  "tier" : "C1",
  "riskLevel" : "MEDIUM",
  "pncReference" : "Some PNC Reference",
  "assignedTo" : {
    "id" : 1,
    "name" : "Team 1"
  },
  "currentAccommodation" : {
    "id" : "f296b6a7-79c3-4d46-b5ed-683e72e9ae09",
    "name" : "HMP Bullingdon",
    "arrangementType" : "PRISON",
    "arrangementSubType" : null,
    "arrangementSubTypeDescription" : null,
    "settledType" : "TRANSIENT",
    "offenderReleaseType" : null,
    "status" : null,
    "address" : {
      "postcode" : null,
      "subBuildingName" : null,
      "buildingName" : null,
      "buildingNumber" : null,
      "thoroughfareName" : null,
      "dependentLocality" : null,
      "postTown" : null,
      "county" : null,
      "country" : null,
      "uprn" : null
    },
    "startDate" : null,
    "endDate" : null,
    "createdAt" : "2021-11-11T14:35:11Z"
  },
  "nextAccommodation" : {
    "id" : "fa75a728-1020-44d0-8bb6-343ca1197d2e",
    "name" : null,
    "arrangementType" : "PRIVATE",
    "arrangementSubType" : "SOCIAL_RENTED",
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
    "createdAt" : "2026-01-15T12:19:20Z"
  }
} ]
""".trimIndent()
