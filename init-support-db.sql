-- Database and user are already created by Docker with POSTGRES_DB and POSTGRES_USER
-- Just ensure proper privileges
-- Grant schema privileges
GRANT ALL PRIVILEGES ON SCHEMA public TO support_user;

-- Create tables
CREATE TABLE IF NOT EXISTS support_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keyword VARCHAR(255) NOT NULL,
    pattern TEXT,
    response TEXT NOT NULL,
    priority INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    username VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    response TEXT,
    response_type VARCHAR(50),
    is_from_admin BOOLEAN DEFAULT FALSE,
    matched_rule_id UUID REFERENCES support_rules(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    response_time TIMESTAMP
);

-- Create indices
CREATE INDEX idx_chat_messages_user_id ON chat_messages(user_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX idx_support_rules_enabled ON support_rules(enabled);
CREATE INDEX idx_support_rules_priority ON support_rules(priority);

-- Insert default support rules
INSERT INTO support_rules (keyword, pattern, response, priority, enabled) VALUES
('consumption', '.*reduce.*consumption.*', 'To reduce your energy consumption, try these tips: 1) Turn off appliances when not in use, 2) Use LED bulbs, 3) Adjust thermostat settings, 4) Use power strips, 5) Schedule usage during off-peak hours', 100, true),
('alert', '.*overconsumption.*|.*alert.*', 'You have exceeded your energy consumption limit. Please check your device usage and consider reducing consumption. An administrator will contact you shortly if needed.', 90, true),
('billing', '.*bill.*|.*charge.*|.*invoice.*', 'For billing inquiries, please contact our billing department at billing@energymanagement.com or call 1-800-ENERGY-1', 85, true),
('payment', '.*pay.*|.*payment.*', 'You can make payments through our online portal, by phone, or by mail. Please visit our website for more information.', 80, true),
('account', '.*account.*|.*profile.*', 'To manage your account settings, please log in to your dashboard and navigate to the Account section', 75, true),
('hello', '.*hello.*|.*hi.*|.*hey.*', 'Hello! Welcome to our Energy Management System. How can we help you today?', 70, true),
('thank', '.*thank.*|.*thanks.*|.*appreciate.*', 'Thank you for contacting us! We appreciate your feedback.', 65, true),
('help', '.*help.*|.*support.*|.*assist.*', 'I am here to help! Please tell me what issue you are experiencing and I will do my best to assist you.', 60, true);

-- Grant table privileges to user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO support_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO support_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO support_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO support_user;

