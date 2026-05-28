-- =============================================================
--  TuristeAR — seed para testing local
--  Aplicar contra Supabase con: psql ... -f seed.sql
--  o pegando en el SQL Editor de Supabase.
--
--  Idempotente: se puede correr varias veces sin duplicar.
-- =============================================================

-- ---------------------------------------------------------------
--  1. Etiquetas (los 6 valores válidos del CHECK constraint)
-- ---------------------------------------------------------------
INSERT INTO etiquetas (nombre) VALUES
  ('NATURALEZA'),
  ('GASTRONOMIA'),
  ('AVENTURA'),
  ('CULTURA'),
  ('NOCHE'),
  ('COMPRA')
ON CONFLICT (nombre) DO NOTHING;

-- ---------------------------------------------------------------
--  2. Actividades (catálogo reutilizable)
-- ---------------------------------------------------------------
INSERT INTO actividades (nombre, descripcion, localidad, direccion) VALUES
  ('Caminata en el Cerro Catedral',
   'Trekking guiado de media jornada por senderos panorámicos',
   'Bariloche',
   'Av. Pioneros km 8'),
  ('Visita a Llao Llao',
   'Recorrido por el icónico hotel y sus bosques de arrayanes',
   'Bariloche',
   'Av. Bustillo km 25'),
  ('Cervecería Patagonia',
   'Cata de cervezas artesanales con vista al lago',
   'Bariloche',
   'Av. Bustillo km 24,7'),
  ('Tour de bodegas en Luján de Cuyo',
   'Visita a tres bodegas tradicionales con degustación',
   'Luján de Cuyo',
   'Ruta 7'),
  ('Cena en Siete Cocinas',
   'Restaurante con propuesta de cocina regional argentina',
   'Mendoza Capital',
   'Av. Juan B. Justo 880'),
  ('Parque General San Martín',
   'Paseo por el pulmón verde de la ciudad',
   'Mendoza Capital',
   'Av. Boulogne Sur Mer')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
--  3. Itinerarios del sistema (precargados por el equipo)
-- ---------------------------------------------------------------
INSERT INTO itinerarios_sistema
  (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias)
VALUES
  ('Aventura en Bariloche',
   'Tres días recorriendo la Patagonia argentina: trekking, vistas panorámicas y cervezas locales.',
   'RIO_NEGRO',
   '2026-07-10', '2026-07-12',
   'https://example.com/bariloche.jpg',
   3),
  ('Gastronomía mendocina',
   'Dos días dedicados a bodegas y mesa larga en Mendoza.',
   'MENDOZA',
   '2026-08-15', '2026-08-16',
   'https://example.com/mendoza.jpg',
   2)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
--  4. Junction itinerarios <-> etiquetas
-- ---------------------------------------------------------------
INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i
JOIN etiquetas e ON e.nombre IN ('NATURALEZA', 'AVENTURA')
WHERE i.titulo = 'Aventura en Bariloche'
ON CONFLICT DO NOTHING;

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i
JOIN etiquetas e ON e.nombre IN ('GASTRONOMIA', 'CULTURA')
WHERE i.titulo = 'Gastronomía mendocina'
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------
--  5. Items del itinerario "Aventura en Bariloche"
-- ---------------------------------------------------------------
INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '09:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Aventura en Bariloche'
  AND a.nombre = 'Caminata en el Cerro Catedral'
  AND NOT EXISTS (
    SELECT 1 FROM itinerario_sistema_items s
    WHERE s.itinerario_sistema_id = i.id_itinerario
      AND s.actividad_id = a.id_actividad
      AND s.dia = 1);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 2, TIME '10:30'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Aventura en Bariloche'
  AND a.nombre = 'Visita a Llao Llao'
  AND NOT EXISTS (
    SELECT 1 FROM itinerario_sistema_items s
    WHERE s.itinerario_sistema_id = i.id_itinerario
      AND s.actividad_id = a.id_actividad
      AND s.dia = 2);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 3, TIME '18:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Aventura en Bariloche'
  AND a.nombre = 'Cervecería Patagonia'
  AND NOT EXISTS (
    SELECT 1 FROM itinerario_sistema_items s
    WHERE s.itinerario_sistema_id = i.id_itinerario
      AND s.actividad_id = a.id_actividad
      AND s.dia = 3);

-- ---------------------------------------------------------------
--  6. Items del itinerario "Gastronomía mendocina"
-- ---------------------------------------------------------------
INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '11:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Gastronomía mendocina'
  AND a.nombre = 'Tour de bodegas en Luján de Cuyo'
  AND NOT EXISTS (
    SELECT 1 FROM itinerario_sistema_items s
    WHERE s.itinerario_sistema_id = i.id_itinerario
      AND s.actividad_id = a.id_actividad
      AND s.dia = 1);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '21:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Gastronomía mendocina'
  AND a.nombre = 'Cena en Siete Cocinas'
  AND NOT EXISTS (
    SELECT 1 FROM itinerario_sistema_items s
    WHERE s.itinerario_sistema_id = i.id_itinerario
      AND s.actividad_id = a.id_actividad
      AND s.dia = 1
      AND s.hora = TIME '21:00');

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 2, TIME '10:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Gastronomía mendocina'
  AND a.nombre = 'Parque General San Martín'
  AND NOT EXISTS (
    SELECT 1 FROM itinerario_sistema_items s
    WHERE s.itinerario_sistema_id = i.id_itinerario
      AND s.actividad_id = a.id_actividad
      AND s.dia = 2);
