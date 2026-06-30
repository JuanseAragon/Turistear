-- Agrega foto de portada al grupo y ajusta límites razonables
ALTER TABLE grupos
    ADD COLUMN IF NOT EXISTS foto_portada VARCHAR(512);

ALTER TABLE grupos
    ALTER COLUMN nombre TYPE VARCHAR(50);

ALTER TABLE grupos
    ALTER COLUMN descripcion TYPE VARCHAR(300);
