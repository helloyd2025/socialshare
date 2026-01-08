-- Activate PostGIS, Trigram extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ISBN search optimization
CREATE INDEX IF NOT EXISTS idx_user_books_isbn ON user_books(isbn13);

-- Create GIST indexes (radius search optimization)
CREATE INDEX IF NOT EXISTS idx_user_locations_location ON user_locations USING GIST(location);

-- CREATE GIN indexes (text partial corresponding search)
-- book title
CREATE INDEX IF NOT EXISTS idx_books_title_trgm_gin ON books USING GIN (title gin_trgm_ops);
-- refresh token (now redis)
-- CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token_trgm_gin ON refresh_tokens USING GIN (token gin_trgm_ops);