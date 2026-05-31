


CREATE TABLE roles (
    id    UUID PRIMARY KEY   ,                       -- Java generate UUIDv7
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO roles  VALUES
(
  '550e8400-e29b-41d4-a716-446655440000' ,'ROLE_USER', 'Người dùng thông thường'
),
(
  '550e8400-e29b-41d4-a716-446655440001' ,'ROLE_ADMIN', 'Quản trị viên - Quản lý users và cấu hình'
);

CREATE TABLE users (
    id         UUID PRIMARY KEY,                        -- Java generate UUIDv7
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,                   -- BCrypt hash
    email      VARCHAR(100) UNIQUE,
    full_name  VARCHAR(100),
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

