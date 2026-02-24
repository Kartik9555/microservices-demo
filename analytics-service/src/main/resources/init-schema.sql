DROP SCHEMA IF EXISTS analytics CASCADE;

CREATE SCHEMA analytics;

CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS analytics.twitter_analytics CASCADE;

CREATE TABLE analytics.twitter_analytics
(
    id          uuid                                         NOT NULL,
    word        character varying COLLATE pg_catalog.default NOT NULL,
    word_count  bigint                                       NOT NULL,
    record_date time with time zone,
    CONSTRAINT twitter_analytics_pkey PRIMARY KEY (id)
) TABLESPACE pg_default;

ALTER TABLE analytics.twitter_analytics OWNER to postgres;

-- DROP INDEX analytics."INDX_WORD_BY_DATE";

CREATE INDEX "INDX_WORD_BY_DATE" ON analytics.twitter_analytics USING btree (word COLLATE pg_catalog.default ASC NULLS LAST, record_date DESC NULLS LAST) TABLESPCAE pg_default;

