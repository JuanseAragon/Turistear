-- =============================================================
--  TuristeAR — Migración 02: tablas del módulo Grupos.
--  Crea las 6 tablas necesarias para grupos, membresías, códigos
--  de invitación, encuestas, opciones y votos.
--  Idempotente: usa CREATE TABLE IF NOT EXISTS / CREATE INDEX IF NOT EXISTS.
-- =============================================================

-- 1. Grupos
CREATE TABLE IF NOT EXISTS grupos (
    id_grupo BIGSERIAL PRIMARY KEY,
    creador_id BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    fecha_creacion TIMESTAMP NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_grupos_creador_id ON grupos(creador_id);

-- 2. Miembros de grupo
CREATE TABLE IF NOT EXISTS miembros_grupo (
    id BIGSERIAL PRIMARY KEY,
    grupo_id BIGINT NOT NULL REFERENCES grupos(id_grupo) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    rol VARCHAR(50) NOT NULL,
    fecha_union TIMESTAMP NOT NULL,
    CONSTRAINT uq_miembros_grupo_usuario UNIQUE (grupo_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_miembros_grupo_grupo_id ON miembros_grupo(grupo_id);
CREATE INDEX IF NOT EXISTS idx_miembros_grupo_usuario_id ON miembros_grupo(usuario_id);

-- 3. Códigos de invitación
CREATE TABLE IF NOT EXISTS codigos_invitacion (
    id BIGSERIAL PRIMARY KEY,
    grupo_id BIGINT NOT NULL REFERENCES grupos(id_grupo) ON DELETE CASCADE,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    fecha_generacion TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_codigos_invitacion_grupo_id ON codigos_invitacion(grupo_id);
CREATE INDEX IF NOT EXISTS idx_codigos_invitacion_codigo ON codigos_invitacion(codigo);

-- 4. Encuestas (sin la FK a opciones_encuesta todavía)
CREATE TABLE IF NOT EXISTS encuestas (
    id_encuesta BIGSERIAL PRIMARY KEY,
    grupo_id BIGINT NOT NULL REFERENCES grupos(id_grupo) ON DELETE CASCADE,
    creador_id BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    estado VARCHAR(50) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_cierre TIMESTAMP,
    opcion_ganadora_id BIGINT
);

CREATE INDEX IF NOT EXISTS idx_encuestas_grupo_id ON encuestas(grupo_id);

-- 5. Opciones de encuesta
CREATE TABLE IF NOT EXISTS opciones_encuesta (
    id BIGSERIAL PRIMARY KEY,
    encuesta_id BIGINT NOT NULL REFERENCES encuestas(id_encuesta) ON DELETE CASCADE,
    id_itinerario_sistema BIGINT,
    id_itinerario_usuario BIGINT,
    titulo_snapshot VARCHAR(255) NOT NULL,
    foto_portada_snapshot VARCHAR(512),
    propietario_id BIGINT NOT NULL REFERENCES usuarios(id_usuario)
);

CREATE INDEX IF NOT EXISTS idx_opciones_encuesta_encuesta_id ON opciones_encuesta(encuesta_id);

-- FK diferida de encuestas a opciones_encuesta (evita dependencia circular).
ALTER TABLE encuestas
    ADD CONSTRAINT IF NOT EXISTS fk_encuestas_opcion_ganadora
    FOREIGN KEY (opcion_ganadora_id) REFERENCES opciones_encuesta(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_encuestas_opcion_ganadora_id ON encuestas(opcion_ganadora_id);

-- 6. Votos
CREATE TABLE IF NOT EXISTS votos (
    id BIGSERIAL PRIMARY KEY,
    encuesta_id BIGINT NOT NULL REFERENCES encuestas(id_encuesta) ON DELETE CASCADE,
    opcion_id BIGINT NOT NULL REFERENCES opciones_encuesta(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    fecha_voto TIMESTAMP NOT NULL,
    CONSTRAINT uq_votos_encuesta_usuario UNIQUE (encuesta_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_votos_encuesta_id ON votos(encuesta_id);
CREATE INDEX IF NOT EXISTS idx_votos_opcion_id ON votos(opcion_id);
CREATE INDEX IF NOT EXISTS idx_votos_usuario_id ON votos(usuario_id);
