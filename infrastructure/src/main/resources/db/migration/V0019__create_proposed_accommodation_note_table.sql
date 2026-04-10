CREATE TABLE proposed_accommodation_note(
  id                                UUID not null,
  proposed_accommodation_id         UUID not null,
  note                              TEXT not null,
  created_by_user_id                UUID not null,
  created_at                        TIMESTAMP WITH TIME ZONE not null,
  last_updated_by_user_id           UUID not null,
  last_updated_at                   TIMESTAMP WITH TIME ZONE not null,
  PRIMARY KEY(id),
  FOREIGN KEY (proposed_accommodation_id) REFERENCES proposed_accommodation(id)
);

CREATE INDEX idx_proposed_accommodation_note_proposed_accommodation_id on proposed_accommodation_note(proposed_accommodation_id);