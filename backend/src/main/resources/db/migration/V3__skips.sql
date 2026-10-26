-- A skip gives up an attempt without a guess, so there is no guess row to count. It is kept as a counter.
ALTER TABLE playthrough_round ADD COLUMN skips smallint NOT NULL DEFAULT 0;
