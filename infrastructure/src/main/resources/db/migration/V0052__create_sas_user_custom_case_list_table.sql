CREATE TABLE sas_user_custom_case_list
(
    id  UUID    NOT NULL PRIMARY KEY,
    sas_user_id UUID    NOT NULL REFERENCES sas_user (id),
    sas_case_id UUID    NOT NULL REFERENCES sas_case (id),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_sas_user_custom_case_list_sas_user_id_sas_case_id ON sas_user_custom_case_list (sas_user_id, sas_case_id);
