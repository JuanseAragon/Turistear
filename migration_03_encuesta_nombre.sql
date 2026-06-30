-- Agrega nombre opcional a las encuestas
ALTER TABLE encuestas
    ADD COLUMN IF NOT EXISTS nombre VARCHAR(120);
