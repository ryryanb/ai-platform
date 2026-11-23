-- Existing conversations predate authentication and have no trustworthy owner.
-- Keep them unassigned rather than guessing which FinTech customer owns them.
ALTER TABLE conversations ADD COLUMN customer_id VARCHAR(100);

CREATE INDEX idx_conversations_customer_updated
    ON conversations (customer_id, updated_at DESC);