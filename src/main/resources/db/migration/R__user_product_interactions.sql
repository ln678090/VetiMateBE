CREATE TABLE IF NOT EXISTS user_favorite_products (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, product_id)
);

CREATE TABLE IF NOT EXISTS user_viewed_products (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    viewed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_user_favorite_products_user_id ON user_favorite_products(user_id);
CREATE INDEX IF NOT EXISTS idx_user_viewed_products_user_id ON user_viewed_products(user_id);
CREATE INDEX IF NOT EXISTS idx_user_viewed_products_viewed_at ON user_viewed_products(viewed_at DESC);
