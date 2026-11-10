ALTER TABLE external_referral RENAME COLUMN outcome_reason TO withdrawal_reason;

ALTER TABLE external_referral
  ADD COLUMN withdrawal_note TEXT;
