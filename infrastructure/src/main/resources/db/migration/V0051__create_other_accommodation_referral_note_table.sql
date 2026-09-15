CREATE TABLE other_accommodation_referral_note(
  id                                UUID not null,
  other_accommodation_referral_id   UUID not null,
  note                              TEXT not null,
  created_by_user_id                UUID not null,
  created_at                        TIMESTAMP WITH TIME ZONE not null,
  last_updated_by_user_id           UUID not null,
  last_updated_at                   TIMESTAMP WITH TIME ZONE not null,
  PRIMARY KEY(id),
  FOREIGN KEY (other_accommodation_referral_id) REFERENCES other_accommodation_referral(id)
);

CREATE INDEX idx_other_accommodation_referral_note_referral_id on other_accommodation_referral_note(other_accommodation_referral_id);
