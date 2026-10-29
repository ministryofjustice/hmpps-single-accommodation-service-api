package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.unit.case

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.AccommodationSummariesDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseAccommodationStatus
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.CaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.dtos.PeopleType
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.common.factories.buildCaseDto
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.filtered
import uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.query.case.sortCases
import java.time.LocalDate

class CaseListSortingTest {
  private val today: LocalDate = LocalDate.of(2026, 10, 1)

  private fun case(
    crn: String,
    status: CaseAccommodationStatus?,
    statusDate: LocalDate? = null,
    forename: String = "First",
    surname: String = "Last",
    withSummaries: Boolean = true,
  ): CaseDto = buildCaseDto(
    crn = crn,
    forename = forename,
    surname = surname,
    accommodationSummaries = if (withSummaries) {
      AccommodationSummariesDto(caseAccommodationStatus = status, caseAccommodationStatusDate = statusDate)
    } else {
      null
    },
  )

  @Nested
  inner class Filtered {
    private val settled = case("SETTLED1", CaseAccommodationStatus.SETTLED)
    private val transient = case("TRANSIENT1", CaseAccommodationStatus.TRANSIENT)
    private val nfa = case("NFA1", CaseAccommodationStatus.NO_FIXED_ABODE)
    private val risk = case("RISK1", CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE)
    private val noStatus = case("NONE1", null)
    private val noSummaries = case("NOSUM1", null, withSummaries = false)
    private val all = listOf(settled, transient, nfa, risk, noStatus, noSummaries)

    @Test
    fun `returns all cases when people type is null`() {
      assertThat(all.filtered(null)).containsExactlyElementsOf(all)
    }

    @Test
    fun `HOUSED returns only settled cases`() {
      assertThat(all.filtered(PeopleType.HOUSED)).containsExactly(settled)
    }

    @Test
    fun `NFA_RISK returns every case that is not settled, including those without a status`() {
      assertThat(all.filtered(PeopleType.NFA_RISK))
        .containsExactly(transient, nfa, risk, noStatus, noSummaries)
    }

    @Test
    fun `returns empty list for empty input`() {
      assertThat(emptyList<CaseDto>().filtered(PeopleType.HOUSED)).isEmpty()
    }
  }

  @Nested
  inner class SortCases {
    private fun List<CaseDto>.crns() = map { it.crn }

    @Test
    fun `returns empty list for empty input`() {
      assertThat(emptyList<CaseDto>().sortCases(today)).isEmpty()
    }

    @Test
    fun `orders by status sort order with missing status first`() {
      val cases = listOf(
        case("C_SETTLED", CaseAccommodationStatus.SETTLED),
        case("C_TRANSIENT", CaseAccommodationStatus.TRANSIENT),
        case("C_NFA", CaseAccommodationStatus.NO_FIXED_ABODE),
        case("C_RISK", CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE),
        case("C_NONE", null, withSummaries = false),
      )

      assertThat(cases.sortCases(today).crns())
        .containsExactly("C_NONE", "C_RISK", "C_NFA", "C_TRANSIENT", "C_SETTLED")
    }

    @Test
    fun `sorts RISK_OF_NO_FIXED_ABODE by soonest status date`() {
      val cases = listOf(
        case("LATER", CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, today.plusDays(5)),
        case("SOON", CaseAccommodationStatus.RISK_OF_NO_FIXED_ABODE, today.plusDays(1)),
      )

      assertThat(cases.sortCases(today).crns()).containsExactly("SOON", "LATER")
    }

    @Test
    fun `sorts NO_FIXED_ABODE by oldest status date`() {
      val cases = listOf(
        case("NEWER", CaseAccommodationStatus.NO_FIXED_ABODE, today.minusDays(1)),
        case("OLDER", CaseAccommodationStatus.NO_FIXED_ABODE, today.minusDays(5)),
      )

      assertThat(cases.sortCases(today).crns()).containsExactly("OLDER", "NEWER")
    }

    @Test
    fun `for TRANSIENT and SETTLED places missing date first, then future dates, then today and past dates`() {
      CaseAccommodationStatus.entries
        .filter { it == CaseAccommodationStatus.TRANSIENT || it == CaseAccommodationStatus.SETTLED }
        .forEach { status ->
          val cases = listOf(
            case("PAST", status, today.minusDays(1)),
            case("TODAY", status, today),
            case("FUTURE_FAR", status, today.plusDays(3)),
            case("FUTURE_NEAR", status, today.plusDays(1)),
            case("NO_DATE", status, null),
            case("PAST_OLD", status, today.minusDays(4)),
          )

          assertThat(cases.sortCases(today).crns())
            .containsExactly("NO_DATE", "FUTURE_NEAR", "FUTURE_FAR", "PAST_OLD", "PAST", "TODAY")
        }
    }

    @Test
    fun `uses today parameter to decide whether a date is in the future`() {
      val statusDate = LocalDate.of(2026, 10, 10)
      val cases = listOf(
        case("B", CaseAccommodationStatus.SETTLED, statusDate),
        case("A", CaseAccommodationStatus.SETTLED, LocalDate.of(2026, 10, 5)),
      )

      // statusDate and 5th Oct both in the future relative to 1st Oct -> oldest first
      assertThat(cases.sortCases(today).crns()).containsExactly("A", "B")
      // relative to 7th Oct, 5th is past and 10th is future -> future first
      assertThat(cases.sortCases(LocalDate.of(2026, 10, 7)).crns()).containsExactly("B", "A")
    }

    @Test
    fun `breaks ties by surname, forename ignoring case, then crn`() {
      val status = CaseAccommodationStatus.NO_FIXED_ABODE
      val date = today.minusDays(1)
      val cases = listOf(
        case("CRN5", status, date, forename = "zulu", surname = "Beta"),
        case("CRN3", status, date, forename = "Beta", surname = "beta"),
        case("CRN4", status, date, forename = "Zulu", surname = "Beta"),
        case("CRN2", status, date, forename = "Zulu", surname = "alpha"),
      )

      assertThat(cases.sortCases(today).crns()).containsExactly("CRN2", "CRN3", "CRN4", "CRN5")
    }
  }
}
