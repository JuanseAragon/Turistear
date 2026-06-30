-- =============================================================
--  TuristeAR — Rollback 02: eliminar tablas del módulo Grupos.
--  Orden de borrado: hijas primero para respetar las FKs.
-- =============================================================

DROP TABLE IF EXISTS votos CASCADE;
DROP TABLE IF EXISTS opciones_encuesta CASCADE;
DROP TABLE IF EXISTS encuestas CASCADE;
DROP TABLE IF EXISTS codigos_invitacion CASCADE;
DROP TABLE IF EXISTS miembros_grupo CASCADE;
DROP TABLE IF EXISTS grupos CASCADE;
