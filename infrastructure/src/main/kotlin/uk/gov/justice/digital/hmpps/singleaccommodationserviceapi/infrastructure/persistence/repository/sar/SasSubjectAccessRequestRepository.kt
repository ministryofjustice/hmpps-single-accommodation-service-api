package uk.gov.justice.digital.hmpps.singleaccommodationserviceapi.infrastructure.persistence.repository.sar

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class SasSubjectAccessRequestRepository(
  jdbcTemplate: NamedParameterJdbcTemplate,
) : SubjectAccessRequestRepositoryBase(jdbcTemplate) {

  fun getCasesJson(crn: String?, nomsNumber: String?, startDate: LocalDateTime?, endDate: LocalDateTime?): String? {
    val params = MapSqlParameterSource().addSarParameters(crn, nomsNumber, startDate, endDate)
    val result = jdbcTemplate.queryForMap(
      """
      select json_agg(alias) as json
      from (
        select
          sc.id,
          sc.tier_score,
          sc.cas1_application_id,
          sc.cas1_application_application_status,
          sc.cas1_application_placement_status,
          sc.cas1_application_request_for_placement_status
        from sas_case sc
        join sas_case_identifier sci on sci.case_id = sc.id
        where
          (:crn::text is null or (sci.identifier = :crn and sci.identifier_type = 'CRN'))
          and (:noms_number::text is null or (sci.identifier = :noms_number and sci.identifier_type = 'NOMS'))
      ) alias
      """.trimIndent(),
      params,
    )
    return toJsonString(result)
  }

  fun getCaseIdentifiersJson(crn: String?, nomsNumber: String?, startDate: LocalDateTime?, endDate: LocalDateTime?): String? {
    val params = MapSqlParameterSource().addSarParameters(crn, nomsNumber, startDate, endDate)
    val result = jdbcTemplate.queryForMap(
      """
      select json_agg(alias) as json
      from (
        select
          sci2.identifier,
          sci2.identifier_type,
          sci2.created_at
        from sas_case sc
        join sas_case_identifier sci on sci.case_id = sc.id
        join sas_case_identifier sci2 on sci2.case_id = sc.id
        where
          (:crn::text is null or (sci.identifier = :crn and sci.identifier_type = 'CRN'))
          and (:noms_number::text is null or (sci.identifier = :noms_number and sci.identifier_type = 'NOMS'))
          and (:start_date::date is null or sci2.created_at >= :start_date)
          and (:end_date::date is null or sci2.created_at <= :end_date)
      ) alias
      """.trimIndent(),
      params,
    )
    return toJsonString(result)
  }

  fun getDutyToReferJson(crn: String?, nomsNumber: String?, startDate: LocalDateTime?, endDate: LocalDateTime?): String? {
    val params = MapSqlParameterSource().addSarParameters(crn, nomsNumber, startDate, endDate)
    val result = jdbcTemplate.queryForMap(
      """
      select json_agg(alias) as json
      from (
        select
          dtr.id,
          dtr.reference_number,
          dtr.submission_date,
          dtr.status,
          laa.name as local_authority_area_name,
          dtr.created_at,
          dtr.last_updated_at
        from duty_to_refer dtr
        join sas_case sc on sc.id = dtr.case_id
        join sas_case_identifier sci on sci.case_id = sc.id
        join local_authority_area laa on laa.id = dtr.local_authority_area_id
        where
          (:crn::text is null or (sci.identifier = :crn and sci.identifier_type = 'CRN'))
          and (:noms_number::text is null or (sci.identifier = :noms_number and sci.identifier_type = 'NOMS'))
          and (:start_date::date is null or dtr.created_at >= :start_date)
          and (:end_date::date is null or dtr.created_at <= :end_date)
      ) alias
      """.trimIndent(),
      params,
    )
    return toJsonString(result)
  }

  fun getDutyToReferNotesJson(crn: String?, nomsNumber: String?, startDate: LocalDateTime?, endDate: LocalDateTime?): String? {
    val params = MapSqlParameterSource().addSarParameters(crn, nomsNumber, startDate, endDate)
    val result = jdbcTemplate.queryForMap(
      """
      select json_agg(alias) as json
      from (
        select
          dtrn.note,
          dtrn.created_at
        from duty_to_refer_note dtrn
        join duty_to_refer dtr on dtr.id = dtrn.duty_to_refer_id
        join sas_case sc on sc.id = dtr.case_id
        join sas_case_identifier sci on sci.case_id = sc.id
        where
          (:crn::text is null or (sci.identifier = :crn and sci.identifier_type = 'CRN'))
          and (:noms_number::text is null or (sci.identifier = :noms_number and sci.identifier_type = 'NOMS'))
          and (:start_date::date is null or dtrn.created_at >= :start_date)
          and (:end_date::date is null or dtrn.created_at <= :end_date)
      ) alias
      """.trimIndent(),
      params,
    )
    return toJsonString(result)
  }

  fun getProposedAccommodationJson(crn: String?, nomsNumber: String?, startDate: LocalDateTime?, endDate: LocalDateTime?): String? {
    val params = MapSqlParameterSource().addSarParameters(crn, nomsNumber, startDate, endDate)
    val result = jdbcTemplate.queryForMap(
      """
      select json_agg(alias) as json
      from (
        select
          pa.name,
          at.name as arrangement_type,
          ast.name as status,
          pa.start_date,
          pa.end_date,
          pa.postcode,
          pa.sub_building_name,
          pa.building_name,
          pa.building_number,
          pa.throughfare_name,
          pa.dependent_locality,
          pa.post_town,
          pa.county,
          pa.country,
          pa.uprn,
          pa.created_at,
          pa.last_updated_at
        from proposed_accommodation pa
        join sas_case sc on sc.id = pa.case_id
        join sas_case_identifier sci on sci.case_id = sc.id
        left join accommodation_type at on at.id = pa.accommodation_type_id
        left join accommodation_status ast on ast.id = pa.accommodation_status_id
        where
          (:crn::text is null or (sci.identifier = :crn and sci.identifier_type = 'CRN'))
          and (:noms_number::text is null or (sci.identifier = :noms_number and sci.identifier_type = 'NOMS'))
          and (:start_date::date is null or pa.created_at >= :start_date)
          and (:end_date::date is null or pa.created_at <= :end_date)
      ) alias
      """.trimIndent(),
      params,
    )
    return toJsonString(result)
  }

  fun getProposedAccommodationNotesJson(crn: String?, nomsNumber: String?, startDate: LocalDateTime?, endDate: LocalDateTime?): String? {
    val params = MapSqlParameterSource().addSarParameters(crn, nomsNumber, startDate, endDate)
    val result = jdbcTemplate.queryForMap(
      """
      select json_agg(alias) as json
      from (
        select
          pan.note,
          pan.created_at
        from proposed_accommodation_note pan
        join proposed_accommodation pa on pa.id = pan.proposed_accommodation_id
        join sas_case sc on sc.id = pa.case_id
        join sas_case_identifier sci on sci.case_id = sc.id
        where
          (:crn::text is null or (sci.identifier = :crn and sci.identifier_type = 'CRN'))
          and (:noms_number::text is null or (sci.identifier = :noms_number and sci.identifier_type = 'NOMS'))
          and (:start_date::date is null or pan.created_at >= :start_date)
          and (:end_date::date is null or pan.created_at <= :end_date)
      ) alias
      """.trimIndent(),
      params,
    )
    return toJsonString(result)
  }
}
