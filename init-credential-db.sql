-- Create credential database schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL PRIVILEGES ON SCHEMA public TO vutzu;

-- Insert the default admin user with BCrypt hashed password
-- Password: admin123
-- BCrypt hash of 'admin123': $2a$10$slYQmyNdGzin7olVN3p5aOnyDjf3T7z3y0gvB1w.mnNR97ufZQSDi
DO $$
BEGIN
  INSERT INTO credentials (id, username, password, role)
  VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'admin123',
    '$2a$10$slYQmyNdGzin7olVN3p5aOnyDjf3T7z3y0gvB1w.mnNR97ufZQSDi',
    'Administrator'
  )
  ON CONFLICT (username) DO NOTHING;
EXCEPTION WHEN OTHERS THEN
  -- Table might not exist yet, that's OK
  NULL;
END $$;
