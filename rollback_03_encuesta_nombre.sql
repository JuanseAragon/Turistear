-- Rollback: quita el nombre de las encuestas
ALTER TABLE encuestas
    DROP COLUMN IF EXISTS nombre;
