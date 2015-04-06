-- Creating indexes without a name, makes the system choose one for you
CREATE UNIQUE INDEX ON Movie (LOWER(Name));
CREATE INDEX ON Actor (LOWER(Name));
CREATE INDEX ON Actor (LOWER(Surname));