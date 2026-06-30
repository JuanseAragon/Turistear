-- =============================================================
--  TuristeAR — Migración 04: itinerario compartido de grupo.
--  Crea itinerarios_grupo, itinerario_grupo_items y
--  asistencia_itinerario_grupo. Snapshot del ganador de una
--  encuesta, con edición colaborativa (propuesto/confirmado)
--  y asistencia por actividad.
--  Idempotente: usa CREATE TABLE IF NOT EXISTS / CREATE INDEX IF NOT EXISTS.
-- =============================================================

CREATE TABLE IF NOT EXISTS itinerarios_grupo (
    id_itinerario_grupo BIGSERIAL PRIMARY KEY,
    grupo_id BIGINT NOT NULL REFERENCES grupos(id_grupo) ON DELETE CASCADE,
    creador_id BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    encuesta_origen_id BIGINT REFERENCES encuestas(id_encuesta) ON DELETE SET NULL,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    provincia VARCHAR(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    foto_portada VARCHAR(512),
    duracion_dias INTEGER NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    CONSTRAINT uq_itinerarios_grupo_encuesta_origen UNIQUE (encuesta_origen_id)
);

CREATE INDEX IF NOT EXISTS idx_itinerarios_grupo_grupo_id ON itinerarios_grupo(grupo_id);

ALTER TABLE itinerarios_grupo ENABLE ROW LEVEL SECURITY;

-- Items del itinerario de grupo: edición colaborativa con estado propuesto/confirmado.
CREATE TABLE IF NOT EXISTS itinerario_grupo_items (
    id BIGSERIAL PRIMARY KEY,
    itinerario_grupo_id BIGINT NOT NULL REFERENCES itinerarios_grupo(id_itinerario_grupo) ON DELETE CASCADE,
    nombre_actividad VARCHAR(255) NOT NULL,
    descripcion TEXT,
    localidad VARCHAR(255),
    direccion VARCHAR(255),
    dia INTEGER NOT NULL,
    hora TIME,
    estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO',
    propuesto_por_id BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    fecha_creacion TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_itinerario_grupo_items_itinerario_id ON itinerario_grupo_items(itinerario_grupo_id);
CREATE INDEX IF NOT EXISTS idx_itinerario_grupo_items_propuesto_por_id ON itinerario_grupo_items(propuesto_por_id);

ALTER TABLE itinerario_grupo_items ENABLE ROW LEVEL SECURITY;

-- Asistencia por actividad y miembro ("voy / no voy"). Solo aplica a items CONFIRMADO.
CREATE TABLE IF NOT EXISTS asistencia_itinerario_grupo (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES itinerario_grupo_items(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    asiste BOOLEAN NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL,
    CONSTRAINT uq_asistencia_item_usuario UNIQUE (item_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_asistencia_item_id ON asistencia_itinerario_grupo(item_id);
CREATE INDEX IF NOT EXISTS idx_asistencia_usuario_id ON asistencia_itinerario_grupo(usuario_id);

ALTER TABLE asistencia_itinerario_grupo ENABLE ROW LEVEL SECURITY;
