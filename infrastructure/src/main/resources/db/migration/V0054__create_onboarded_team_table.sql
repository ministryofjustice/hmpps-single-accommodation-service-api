CREATE TABLE onboarded_team
(
    team_code    TEXT                     NOT NULL PRIMARY KEY,
    onboarded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
