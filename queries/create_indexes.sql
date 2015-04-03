-- Creating indexes without a name, makes the system choose one for you
CREATE INDEX ON Movie (LOWER(Name));
CREATE INDEX ON Actor (LOWER(Name));
CREATE INDEX ON Actor (LOWER(Surname));