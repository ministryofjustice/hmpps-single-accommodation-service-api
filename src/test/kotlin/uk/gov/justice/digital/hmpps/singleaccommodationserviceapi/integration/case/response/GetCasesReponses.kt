package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.integration.case.response

import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.mock.mockPhotoUrl
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.utils.TestData

@TestData
fun expectedGetCasesResponse(): String = """
  [
    {
      "name":"First Middle Last",
      "dateOfBirth":"2000-12-03",
      "crn":"X371199",
      "prisonNumber":"PRI1",
      "photoUrl":"$mockPhotoUrl",
      "tier":"C1",
      "riskLevel":"VERY_HIGH",
      "pncReference":"Some PNC Reference",
      "assignedTo": {
        "id":1,
        "name":"Team 1"
      },
      "currentAccommodation": {
        "type":"NO_FIXED_ABODE",
        "subType":"RENTED",
        "name":"ACCOMMODATION NAME A",
        "isSettled":true,
        "offenderReleaseType":"LICENCE",
        "startDate":"1970-01-01",
        "endDate":"1970-01-01",
        "address": {
          "line1":"123 Main Street",
          "line2":"Town A",
          "region":"Region A",
          "city":"City A",
          "postcode":"AB12 3CD"
        }
      },
      "nextAccommodation": {
        "type":"NO_FIXED_ABODE",
        "subType":"RENTED",
        "name":"ACCOMMODATION NAME A",
        "isSettled":true,
        "offenderReleaseType":"LICENCE",
        "startDate":"1970-01-01",
        "endDate":"1970-01-01",
        "address": {
          "line1":"123 Main Street",
          "line2":"Town A",
          "region":"Region A",
          "city":"City A",
          "postcode":"AB12 3CD"
        }
      }
    },
    {
      "name":"Zack Middle Smith",
      "dateOfBirth":"2000-12-03",
      "crn":"X968879",
      "prisonNumber":"PRI1",
      "photoUrl":"$mockPhotoUrl",
      "tier":"C1",
      "riskLevel":"MEDIUM",
      "pncReference":"Some PNC Reference",
      "assignedTo": {
        "id":1,
        "name":"Team 1"
      },
      "currentAccommodation": {
        "type":"CAS2",
        "subType":"LODGING",
        "name":"ACCOMMODATION NAME B",
        "isSettled":false,
        "offenderReleaseType":"BAIL",
        "startDate":"1970-01-01",
        "endDate":"1970-01-01",
        "address": {
          "line1":"123 Main Street",
          "line2":"Town B",
          "region":"Region B",
          "city":"City B",
          "postcode":"AB12 3CD"
        }
      },
      "nextAccommodation": {
        "type":"CAS2",
        "subType":"LODGING",
        "name":"ACCOMMODATION NAME B",
        "isSettled":false,
        "offenderReleaseType":"BAIL",
        "startDate":"1970-01-01",
        "endDate":"1970-01-01",
        "address": {
          "line1":"123 Main Street",
          "line2":"Town B",
          "region":"Region B",
          "city":"City B",
          "postcode":"AB12 3CD"
        }
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
      "photoUrl":"$mockPhotoUrl",
      "tier":"C1",
      "riskLevel":"MEDIUM",
      "pncReference":"Some PNC Reference",
      "assignedTo": {
        "id":1,
        "name":"Team 1"
      },
      "currentAccommodation": {
        "type":"CAS2",
        "subType":"LODGING",
        "name":"ACCOMMODATION NAME B",
        "isSettled":false,
        "offenderReleaseType":"BAIL",
        "startDate":"1970-01-01",
        "endDate":"1970-01-01",
        "address": {
          "line1":"123 Main Street",
          "line2":"Town B",
          "region":"Region B",
          "city":"City B",
          "postcode":"AB12 3CD"
        }
      },
      "nextAccommodation": {
        "type":"CAS2",
        "subType":"LODGING",
        "name":"ACCOMMODATION NAME B",
        "isSettled":false,
        "offenderReleaseType":"BAIL",
        "startDate":"1970-01-01",
        "endDate":"1970-01-01",
        "address": {
          "line1":"123 Main Street",
          "line2":"Town B",
          "region":"Region B",
          "city":"City B",
          "postcode":"AB12 3CD"
        }
      }
    }
  ]
""".trimIndent()
