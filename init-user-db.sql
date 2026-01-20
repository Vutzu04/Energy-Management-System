-- Create user database schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL PRIVILEGES ON SCHEMA public TO vutzu;

-- The users table will be created automatically by Hibernate
-- But we can seed some initial data after tables are created

-- Wait for table to exist and insert seed data
DO $$
BEGIN
  -- Wait a bit for Hibernate to create the table
  PERFORM pg_sleep(2);
  
  -- Try to insert test user if table exists
  IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'users') THEN
    INSERT INTO users (id, username, email, role)
    VALUES (
      '550e8400-e29b-41d4-a716-446655440001',
      'client001',
      'client001@example.com',
      'Client'
    )
    ON CONFLICT (username) DO NOTHING;
    
    INSERT INTO users (id, username, email, role)
    VALUES (
      '550e8400-e29b-41d4-a716-446655440002',
      'client002',
      'client002@example.com',
      'Client'
    )
    ON CONFLICT (username) DO NOTHING;
  END IF;
EXCEPTION WHEN OTHERS THEN
  -- Table might not exist yet, that's OK - Hibernate will create it
  NULL;
END $$;