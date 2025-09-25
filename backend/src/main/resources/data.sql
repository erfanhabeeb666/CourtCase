-- Ensure unique index on users.email exists (idempotent)
-- This block uses information_schema to create the constraint only if absent
SET @idx_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'users'
    AND index_name = 'uq_users_email'
);

SET @create_sql := IF(@idx_exists = 0,
  'ALTER TABLE users ADD CONSTRAINT uq_users_email UNIQUE (email)',
  'SELECT 1' -- no-op
);

PREPARE stmt FROM @create_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
