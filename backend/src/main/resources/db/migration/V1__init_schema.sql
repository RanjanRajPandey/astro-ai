-- ============================================================================
-- V1__init_schema.sql
-- Initial Relational Schema for Astro-AI Platform (20 Core Entities)
-- Compatible with PostgreSQL 16 and H2 (MODE=PostgreSQL)
-- ============================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE locations (
    id UUID PRIMARY KEY,
    place_name VARCHAR(255) NOT NULL,
    country_code VARCHAR(16),
    latitude NUMERIC(10, 6) NOT NULL,
    longitude NUMERIC(10, 6) NOT NULL,
    timezone_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE birth_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    location_id UUID REFERENCES locations(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    date_of_birth DATE NOT NULL,
    time_of_birth TIME,
    birth_time_accurate BOOLEAN NOT NULL DEFAULT TRUE,
    place_of_birth VARCHAR(255) NOT NULL,
    gender VARCHAR(32) NOT NULL,
    latitude NUMERIC(10, 6) NOT NULL,
    longitude NUMERIC(10, 6) NOT NULL,
    timezone VARCHAR(100) NOT NULL,
    utc_offset_hours NUMERIC(5, 2) NOT NULL,
    utc_birth_time TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_birth_profiles_user_id ON birth_profiles(user_id);

CREATE TABLE charts (
    id UUID PRIMARY KEY,
    birth_profile_id UUID NOT NULL UNIQUE REFERENCES birth_profiles(id) ON DELETE CASCADE,
    ayanamsha_type VARCHAR(50) NOT NULL,
    ayanamsha_value NUMERIC(12, 8) NOT NULL,
    house_system VARCHAR(64) NOT NULL,
    node_type VARCHAR(32) NOT NULL,
    ascendant_sign VARCHAR(32) NOT NULL,
    ascendant_degree NUMERIC(10, 6) NOT NULL,
    calculation_version VARCHAR(64) NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE planet_positions (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL REFERENCES charts(id) ON DELETE CASCADE,
    planet VARCHAR(32) NOT NULL,
    longitude NUMERIC(12, 8) NOT NULL,
    latitude NUMERIC(12, 8) NOT NULL,
    speed_longitude NUMERIC(12, 8) NOT NULL,
    sign VARCHAR(32) NOT NULL,
    degree_in_sign NUMERIC(10, 6) NOT NULL,
    house_number INT NOT NULL,
    nakshatra VARCHAR(64) NOT NULL,
    pada INT NOT NULL,
    is_retrograde BOOLEAN NOT NULL,
    is_combust BOOLEAN NOT NULL,
    dignity VARCHAR(64) NOT NULL,
    relationships_json TEXT NOT NULL
);

CREATE INDEX idx_planet_positions_chart_id ON planet_positions(chart_id);

CREATE TABLE houses (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL REFERENCES charts(id) ON DELETE CASCADE,
    house_number INT NOT NULL,
    sign VARCHAR(32) NOT NULL,
    degree_cusp NUMERIC(10, 6) NOT NULL,
    degree_start NUMERIC(10, 6) NOT NULL,
    degree_end NUMERIC(10, 6) NOT NULL,
    lord_planet VARCHAR(32) NOT NULL,
    occupants_json TEXT NOT NULL
);

CREATE INDEX idx_houses_chart_id ON houses(chart_id);

CREATE TABLE nakshatra_placements (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL REFERENCES charts(id) ON DELETE CASCADE,
    body_name VARCHAR(32) NOT NULL,
    nakshatra_name VARCHAR(64) NOT NULL,
    nakshatra_index INT NOT NULL,
    pada INT NOT NULL,
    ruler_planet VARCHAR(32) NOT NULL,
    deity VARCHAR(100),
    gana VARCHAR(50),
    nadi VARCHAR(50),
    yoni VARCHAR(50)
);

CREATE INDEX idx_nakshatra_chart_id ON nakshatra_placements(chart_id);

CREATE TABLE divisional_charts (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL REFERENCES charts(id) ON DELETE CASCADE,
    varga_code VARCHAR(16) NOT NULL,
    division_number INT NOT NULL,
    ascendant_sign VARCHAR(32) NOT NULL,
    planet_placements_json TEXT NOT NULL,
    house_signs_json TEXT NOT NULL,
    CONSTRAINT uq_chart_varga UNIQUE (chart_id, varga_code)
);

CREATE TABLE dasha_periods (
    id UUID PRIMARY KEY,
    birth_profile_id UUID NOT NULL REFERENCES birth_profiles(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES dasha_periods(id) ON DELETE CASCADE,
    planet VARCHAR(32) NOT NULL,
    level INT NOT NULL,
    start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_dasha_profile_level_dates
    ON dasha_periods(birth_profile_id, level, start_date_time, end_date_time);

CREATE TABLE aspects (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL REFERENCES charts(id) ON DELETE CASCADE,
    source_planet VARCHAR(32) NOT NULL,
    source_house INT NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_identifier VARCHAR(64) NOT NULL,
    aspect_type VARCHAR(64) NOT NULL,
    rule_applied VARCHAR(255) NOT NULL,
    virupa_strength NUMERIC(8, 3) NOT NULL
);

CREATE INDEX idx_aspects_chart_id ON aspects(chart_id);

CREATE TABLE planet_strengths (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL REFERENCES charts(id) ON DELETE CASCADE,
    planet VARCHAR(32) NOT NULL,
    sthana_bala NUMERIC(10, 3) NOT NULL,
    dig_bala NUMERIC(10, 3) NOT NULL,
    kala_bala NUMERIC(10, 3) NOT NULL,
    chesta_bala NUMERIC(10, 3) NOT NULL,
    naisargika_bala NUMERIC(10, 3) NOT NULL,
    drik_bala NUMERIC(10, 3) NOT NULL,
    total_shadbala_virupas NUMERIC(10, 3) NOT NULL,
    total_shadbala_rupas NUMERIC(10, 3) NOT NULL,
    required_minimum_rupas NUMERIC(10, 3) NOT NULL,
    shadbala_ratio NUMERIC(10, 3) NOT NULL,
    vimshopaka_bala NUMERIC(10, 3) NOT NULL,
    strength_grade VARCHAR(32) NOT NULL
);

CREATE INDEX idx_planet_strengths_chart_id ON planet_strengths(chart_id);

CREATE TABLE house_strengths (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL REFERENCES charts(id) ON DELETE CASCADE,
    house_number INT NOT NULL,
    bhavadhipati_bala NUMERIC(10, 3) NOT NULL,
    bhava_dig_bala NUMERIC(10, 3) NOT NULL,
    bhava_drishti_bala NUMERIC(10, 3) NOT NULL,
    occupant_factor NUMERIC(10, 3) NOT NULL,
    total_bhava_bala_virupas NUMERIC(10, 3) NOT NULL,
    total_bhava_bala_rupas NUMERIC(10, 3) NOT NULL,
    strength_grade VARCHAR(32) NOT NULL
);

CREATE INDEX idx_house_strengths_chart_id ON house_strengths(chart_id);

CREATE TABLE yogas (
    id UUID PRIMARY KEY,
    chart_id UUID NOT NULL REFERENCES charts(id) ON DELETE CASCADE,
    yoga_code VARCHAR(64) NOT NULL,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(64) NOT NULL,
    definition TEXT NOT NULL,
    required_conditions_json TEXT NOT NULL,
    detected_conditions_json TEXT NOT NULL,
    planets_involved_json TEXT NOT NULL,
    houses_involved_json TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    strength VARCHAR(32) NOT NULL
);

CREATE INDEX idx_yogas_chart_id ON yogas(chart_id);

CREATE TABLE transits (
    id UUID PRIMARY KEY,
    birth_profile_id UUID NOT NULL REFERENCES birth_profiles(id) ON DELETE CASCADE,
    transit_timestamp_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    transit_positions_json TEXT NOT NULL,
    natal_interactions_json TEXT NOT NULL
);

CREATE INDEX idx_transits_profile_time ON transits(birth_profile_id, transit_timestamp_utc);

CREATE TABLE analysis_sessions (
    id UUID PRIMARY KEY,
    birth_profile_id UUID NOT NULL REFERENCES birth_profiles(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    question_category VARCHAR(64) NOT NULL,
    framework_version VARCHAR(32) NOT NULL,
    factors_considered_json TEXT NOT NULL,
    time_windows_json TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_analysis_sessions_profile_id ON analysis_sessions(birth_profile_id);

CREATE TABLE evidence_items (
    id UUID PRIMARY KEY,
    analysis_session_id UUID NOT NULL REFERENCES analysis_sessions(id) ON DELETE CASCADE,
    factor VARCHAR(150) NOT NULL,
    category VARCHAR(64) NOT NULL,
    observation TEXT NOT NULL,
    rule_reference TEXT NOT NULL,
    effect_description TEXT NOT NULL,
    classification VARCHAR(32) NOT NULL,
    importance VARCHAR(32) NOT NULL,
    source_engine VARCHAR(32) NOT NULL,
    raw_metrics_json TEXT
);

CREATE INDEX idx_evidence_session_class
    ON evidence_items(analysis_session_id, classification, importance);

CREATE TABLE reasoning_items (
    id UUID PRIMARY KEY,
    analysis_session_id UUID NOT NULL REFERENCES analysis_sessions(id) ON DELETE CASCADE,
    step_order INT NOT NULL,
    step_type VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    narrative TEXT NOT NULL,
    linked_evidence_ids_json TEXT NOT NULL
);

CREATE INDEX idx_reasoning_session_order ON reasoning_items(analysis_session_id, step_order);

CREATE TABLE chat_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    birth_profile_id UUID NOT NULL REFERENCES birth_profiles(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    rolling_summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_chat_sessions_user_profile ON chat_sessions(user_id, birth_profile_id);

CREATE TABLE chat_messages (
    id UUID PRIMARY KEY,
    chat_session_id UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    analysis_session_id UUID REFERENCES analysis_sessions(id) ON DELETE SET NULL,
    sender_role VARCHAR(32) NOT NULL,
    message_content TEXT NOT NULL,
    structured_why_this_answer_json TEXT,
    prompt_tokens INT NOT NULL DEFAULT 0,
    completion_tokens INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_chat_messages_session_created ON chat_messages(chat_session_id, created_at);
