-- Initialize monitoring database schema and grant privileges
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL PRIVILEGES ON SCHEMA public TO vutzu;

-- Tables will be created by Hibernate on first run

