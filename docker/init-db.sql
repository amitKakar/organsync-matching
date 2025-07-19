-- OrganSync Matching Service Database Initialization
-- This script creates the necessary database and user for the matching service

-- Create database
CREATE DATABASE IF NOT EXISTS organsync_matching;

-- Create user if not exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'matching_user') THEN
        CREATE ROLE matching_user WITH LOGIN PASSWORD 'matching_password';
    END IF;
END
$$;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE organsync_matching TO matching_user;

-- Switch to the matching database
\c organsync_matching;

-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant permissions on schema
GRANT ALL ON SCHEMA public TO matching_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO matching_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO matching_user;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_matches_status ON matches(status);
CREATE INDEX IF NOT EXISTS idx_matches_hospital_id ON matches(hospital_id);
CREATE INDEX IF NOT EXISTS idx_matches_created_at ON matches(created_at);
CREATE INDEX IF NOT EXISTS idx_compatibility_donor_pair ON compatibility(donor_pair_id);
CREATE INDEX IF NOT EXISTS idx_compatibility_recipient_pair ON compatibility(recipient_pair_id);
CREATE INDEX IF NOT EXISTS idx_compatibility_score ON compatibility(compatibility_score);