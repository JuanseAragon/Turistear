-- Revierte cambios de migration_03
ALTER TABLE grupos
    DROP COLUMN IF EXISTS foto_portada;

ALTER TABLE grupos
    ALTER COLUMN nombre TYPE VARCHAR(255);

ALTER TABLE grupos
    ALTER COLUMN descripcion TYPE TEXT;
