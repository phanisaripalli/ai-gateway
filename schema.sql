-- AI Gateway Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Admins table
CREATE TABLE IF NOT EXISTS admins (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Projects table
CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    daily_token_limit BIGINT,
    daily_cost_limit DECIMAL(10, 4),
    monthly_token_limit BIGINT,
    monthly_cost_limit DECIMAL(10, 4),
    default_provider VARCHAR(50),
    default_capability VARCHAR(50) DEFAULT 'balanced',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- API Keys table
CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    key_prefix VARCHAR(20) NOT NULL,
    key_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    rate_limit_rpm INTEGER DEFAULT 60,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_api_keys_project_id ON api_keys(project_id);
CREATE INDEX IF NOT EXISTS idx_api_keys_key_prefix ON api_keys(key_prefix);

-- Provider Credentials table (encrypted API keys per project)
CREATE TABLE IF NOT EXISTS provider_credentials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL,
    encrypted_key TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, provider)
);

-- Requests table (request log)
CREATE TABLE IF NOT EXISTS requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id),
    api_key_id UUID REFERENCES api_keys(id),
    provider VARCHAR(50) NOT NULL,
    model VARCHAR(100) NOT NULL,
    capability VARCHAR(50),
    input_tokens INTEGER NOT NULL DEFAULT 0,
    output_tokens INTEGER NOT NULL DEFAULT 0,
    thinking_tokens INTEGER NOT NULL DEFAULT 0,
    cost_usd DECIMAL(10, 6) NOT NULL DEFAULT 0,
    latency_ms INTEGER,
    status VARCHAR(20) NOT NULL,
    error_code VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_requests_project_id ON requests(project_id);
CREATE INDEX IF NOT EXISTS idx_requests_created_at ON requests(created_at);

-- Usage Counters table (daily aggregates per project)
CREATE TABLE IF NOT EXISTS usage_counters (
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    total_tokens BIGINT DEFAULT 0,
    total_cost_usd DECIMAL(10, 4) DEFAULT 0,
    request_count INTEGER DEFAULT 0,
    PRIMARY KEY (project_id, date)
);

-- Provider Usage table (daily aggregates per provider)
CREATE TABLE IF NOT EXISTS provider_usage (
    provider VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    total_tokens BIGINT DEFAULT 0,
    total_cost DECIMAL(10, 4) DEFAULT 0,
    request_count INTEGER DEFAULT 0,
    PRIMARY KEY (provider, date)
);

CREATE INDEX IF NOT EXISTS idx_provider_usage_date ON provider_usage(date);
