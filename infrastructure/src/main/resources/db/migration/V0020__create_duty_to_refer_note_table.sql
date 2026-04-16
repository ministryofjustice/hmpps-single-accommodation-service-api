CREATE TABLE duty_to_refer_note(
  id                                UUID not null,
  duty_to_refer_id                  UUID not null,
  note                              TEXT not null,
  created_by_user_id                UUID not null,
  created_at                        TIMESTAMP WITH TIME ZONE not null,
  last_updated_by_user_id           UUID not null,
  last_updated_at                   TIMESTAMP WITH TIME ZONE not null,
  PRIMARY KEY(id),
  FOREIGN KEY (duty_to_refer_id) REFERENCES duty_to_refer(id)
);

CREATE INDEX idx_duty_to_refer_note_duty_to_refer_id on duty_to_refer_note(duty_to_refer_id);
