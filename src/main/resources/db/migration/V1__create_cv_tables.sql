CREATE TABLE cv_profile (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL
);

-- Profile names are unique regardless of case ("QA" and "qa" are the same profile).
CREATE UNIQUE INDEX uq_cv_profile_name ON cv_profile (LOWER(name));

-- A CV version is immutable: the exact PDF that may be sent to an application.
CREATE TABLE cv_version (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    profile_id        BIGINT       NOT NULL REFERENCES cv_profile (id),
    original_filename VARCHAR(255) NOT NULL,
    size_bytes        BIGINT       NOT NULL,
    sha256            VARCHAR(64)  NOT NULL CHECK (LENGTH(sha256) = 64),
    storage_key       VARCHAR(100) NOT NULL,
    notes             VARCHAR(500),
    uploaded_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_cv_version_sha256 UNIQUE (sha256)
);

CREATE INDEX ix_cv_version_profile ON cv_version (profile_id, uploaded_at DESC);
