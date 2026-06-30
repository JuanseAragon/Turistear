-- Revierte cambios de migration_04 (orden inverso a la creación, por las FKs)
DROP TABLE IF EXISTS asistencia_itinerario_grupo;
DROP TABLE IF EXISTS itinerario_grupo_items;
DROP TABLE IF EXISTS itinerarios_grupo;
