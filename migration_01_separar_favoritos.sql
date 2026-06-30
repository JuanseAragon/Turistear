-- =============================================================
--  TuristeAR — Migración 01: separar Favoritos (bookmarks) de
--  Itinerarios de Usuario (copias propias)
--  Aplicada en Supabase el 2026-06-19.
--
--  Antes: itinerarios_usuario era una COPIA atada al sistema vía
--  FK obligatorio (itinerario_sistema_id) y tomaba de ahí sus datos.
--  Después:
--   - favoritos = bookmark simple de un template del sistema.
--   - itinerarios_usuario = copia independiente con datos propios.
--   - cada itinerario de usuario puede tener varias etiquetas.
--
--  Transaccional: si algún paso falla, Postgres revierte todo.
-- =============================================================

-- 1. Tabla NUEVA favoritos (bookmarks: usuario marca un template del sistema)
CREATE TABLE favoritos (
  id BIGSERIAL PRIMARY KEY,
  usuario_id BIGINT NOT NULL REFERENCES usuarios(id_usuario),
  itinerario_sistema_id BIGINT NOT NULL REFERENCES itinerarios_sistema(id_itinerario),
  CONSTRAINT uq_favoritos_usuario_sistema UNIQUE (usuario_id, itinerario_sistema_id)
);

-- 2. Poblar favoritos con los pares (usuario, sistema) de las copias existentes
INSERT INTO favoritos (usuario_id, itinerario_sistema_id)
SELECT DISTINCT usuario_id, itinerario_sistema_id FROM itinerarios_usuario;

-- 3. Agregar columnas propias a itinerarios_usuario (nullable por ahora)
ALTER TABLE itinerarios_usuario
  ADD COLUMN titulo VARCHAR(255),
  ADD COLUMN descripcion TEXT,
  ADD COLUMN provincia VARCHAR(255),
  ADD COLUMN foto_portada VARCHAR(512),
  ADD COLUMN duracion_dias INTEGER;

-- 4. Poblar las columnas nuevas desde el itinerario del sistema de origen
UPDATE itinerarios_usuario iu
SET titulo = s.titulo,
    descripcion = s.descripcion,
    provincia = s.provincia,
    foto_portada = s.foto_portada,
    duracion_dias = s.duracion_dias
FROM itinerarios_sistema s
WHERE iu.itinerario_sistema_id = s.id_itinerario;

-- 5. Tabla NUEVA join de etiquetas para itinerarios de usuario
CREATE TABLE itinerario_usuario_etiquetas (
  itinerario_usuario_id BIGINT NOT NULL REFERENCES itinerarios_usuario(id_itinerario_usuario) ON DELETE CASCADE,
  etiqueta_id BIGINT NOT NULL REFERENCES etiquetas(id),
  PRIMARY KEY (itinerario_usuario_id, etiqueta_id)
);

-- 6. Copiar las etiquetas del sistema a la nueva tabla join
INSERT INTO itinerario_usuario_etiquetas (itinerario_usuario_id, etiqueta_id)
SELECT iu.id_itinerario_usuario, ise.etiqueta_id
FROM itinerarios_usuario iu
JOIN itinerario_sistema_etiquetas ise ON ise.itinerario_sistema_id = iu.itinerario_sistema_id;

-- 7. Marcar NOT NULL las columnas obligatorias (descripcion y foto_portada quedan nullable)
ALTER TABLE itinerarios_usuario
  ALTER COLUMN titulo SET NOT NULL,
  ALTER COLUMN provincia SET NOT NULL,
  ALTER COLUMN duracion_dias SET NOT NULL;

-- 8. Eliminar la columna de origen (drop de la columna elimina su FK asociado)
ALTER TABLE itinerarios_usuario
  DROP COLUMN itinerario_sistema_id;
