package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCasesResponse(): String = """
  [
   {
      "name":"First Middle Last",
      "dateOfBirth":"2000-12-03",
      "crn":"X371199",
      "prisonNumber":"PRI1",
      "photoUrl":"https://github.com/ministryofjustice/hmpps-single-accommodation-service-prototype/blob/main/app/assets/images/profile-placeholder-2.jpg",
      "tier":"C1",
      "riskLevel":"VERY_HIGH",
      "pncReference":"Some PNC Reference",
      "assignedTo":{
         "id":1,
         "name":"Team 1"
      },
      "currentAccommodation":{
         "id":"f3813060-59c7-48ff-8729-3ea6efbf375b",
         "arrangementType":"PRISON",
         "arrangementTypeDescription":null,
         "settledType":"TRANSIENT",
         "status":null,
         "address":{
            "postcode":null,
            "subBuildingName":null,
            "buildingName":"HMP Huntercombe",
            "buildingNumber":null,
            "thoroughfareName":null,
            "dependentLocality":null,
            "postTown":null,
            "county":null,
            "country":null,
            "uprn":null
         },
         "startDate":null,
         "endDate":null
      },
      "nextAccommodation":{
         "id":"b697a854-96af-4360-a715-189a78d4f70f",
         "arrangementType":"FRIENDS_OR_FAMILY",
         "arrangementTypeDescription":null,
         "settledType":"TRANSIENT",
         "status":"PASSED",
         "address":{
            "postcode":"RG26 5AG",
            "subBuildingName":null,
            "buildingName":null,
            "buildingNumber":"4",
            "thoroughfareName":"Dollis Green",
            "dependentLocality":null,
            "postTown":"Bramley",
            "county":null,
            "country":null,
            "uprn":null
         },
         "startDate":null,
         "endDate":null
      }
   },
   {
      "name":"Zack Middle Smith",
      "dateOfBirth":"2000-12-03",
      "crn":"X968879",
      "prisonNumber":"PRI1",
      "photoUrl":"https://github.com/ministryofjustice/hmpps-single-accommodation-service-prototype/blob/main/app/assets/images/profile-placeholder-2.jpg",
      "tier":"C1",
      "riskLevel":"MEDIUM",
      "pncReference":"Some PNC Reference",
      "assignedTo":{
         "id":1,
         "name":"Team 1"
      },
      "currentAccommodation":{
         "id":"f296b6a7-79c3-4d46-b5ed-683e72e9ae09",
         "arrangementType":"PRISON",
         "arrangementTypeDescription":null,
         "settledType":"TRANSIENT",
         "status":null,
         "address":{
            "postcode":null,
            "subBuildingName":null,
            "buildingName":"HMP Bullingdon",
            "buildingNumber":null,
            "thoroughfareName":null,
            "dependentLocality":null,
            "postTown":null,
            "county":null,
            "country":null,
            "uprn":null
         },
         "startDate":null,
         "endDate":null
      },
      "nextAccommodation":{
         "id":"fa75a728-1020-44d0-8bb6-343ca1197d2e",
         "arrangementType":"SOCIAL_RENTED",
         "arrangementTypeDescription":null,
         "settledType":"SETTLED",
         "status":"NOT_CHECKED_YET",
         "address":{
            "postcode":"W1 8XX",
            "subBuildingName":null,
            "buildingName":null,
            "buildingNumber":"11",
            "thoroughfareName":"Piccadilly Circus",
            "dependentLocality":null,
            "postTown":"London",
            "county":null,
            "country":null,
            "uprn":null
         },
         "startDate":null,
         "endDate":null
      }
   }
 ]
""".trimIndent()

fun expectedGetCasesWithFilterResponse(): String = """
  [
   {
      "name":"Zack Middle Smith",
      "dateOfBirth":"2000-12-03",
      "crn":"X968879",
      "prisonNumber":"PRI1",
      "photoUrl":"https://github.com/ministryofjustice/hmpps-single-accommodation-service-prototype/blob/main/app/assets/images/profile-placeholder-2.jpg",
      "tier":"C1",
      "riskLevel":"MEDIUM",
      "pncReference":"Some PNC Reference",
      "assignedTo":{
         "id":1,
         "name":"Team 1"
      },
      "currentAccommodation":{
         "id":"f296b6a7-79c3-4d46-b5ed-683e72e9ae09",
         "arrangementType":"PRISON",
         "arrangementTypeDescription":null,
         "settledType":"TRANSIENT",
         "status":null,
         "address":{
            "postcode":null,
            "subBuildingName":null,
            "buildingName":"HMP Bullingdon",
            "buildingNumber":null,
            "thoroughfareName":null,
            "dependentLocality":null,
            "postTown":null,
            "county":null,
            "country":null,
            "uprn":null
         },
         "startDate":null,
         "endDate":null
      },
      "nextAccommodation":{
         "id":"fa75a728-1020-44d0-8bb6-343ca1197d2e",
         "arrangementType":"SOCIAL_RENTED",
         "arrangementTypeDescription":null,
         "settledType":"SETTLED",
         "status":"NOT_CHECKED_YET",
         "address":{
            "postcode":"W1 8XX",
            "subBuildingName":null,
            "buildingName":null,
            "buildingNumber":"11",
            "thoroughfareName":"Piccadilly Circus",
            "dependentLocality":null,
            "postTown":"London",
            "county":null,
            "country":null,
            "uprn":null
         },
         "startDate":null,
         "endDate":null
      }
   }
  ]
""".trimIndent()
