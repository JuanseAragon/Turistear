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
-- =========================================================================
-- TuristeAR - Insert new system itineraries and matching activities
-- =========================================================================

-- 1. Insert Activities if they don't already exist
INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Caminata por Puerto Madero', 'Recorrido por el moderno barrio portuario de Buenos Aires, cruzando el Puente de la Mujer.', 'Buenos Aires', 'Puerto Madero s/n'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Caminata por Puerto Madero');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Tarde en el Cerro Otto', 'Ascenso en teleférico para disfrutar de la famosa confitería giratoria con vistas panorámicas.', 'Bariloche', 'Km 5 de Av. de los Pioneros'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Tarde en el Cerro Otto');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Cabalgata y Degustación en Valle de Uco', 'Cabalgata por senderos de viñedos con vistas a la Cordillera y degustación de vinos premium.', 'Valle de Uco', 'Ruta Provincial 89'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Cabalgata y Degustación en Valle de Uco');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Excursión a las Bodegas de Cafayate', 'Visita guiada a bodegas tradicionales de Cafayate aprendiendo sobre el cultivo del Torrontés.', 'Cafayate', 'Ruta Nacional 40'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Excursión a las Bodegas de Cafayate');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Gran Aventura en las Cataratas', 'Navegación en lancha rápida para experimentar los saltos de Iguazú desde la base.', 'Puerto Iguazú', 'Parque Nacional Iguazú'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Gran Aventura en las Cataratas');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Navegación al Glaciar Perito Moreno', 'Crucero panorámico frente a las inmensas paredes de hielo del Perito Moreno.', 'El Calafate', 'Puerto Bajo de las Sombras'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Navegación al Glaciar Perito Moreno');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Navegación al Faro Les Éclaireurs', 'Paseo en catamarán por el Canal Beagle visitando la isla de lobos y el faro.', 'Ushuaia', 'Puerto de Ushuaia'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Navegación al Faro Les Éclaireurs');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Paseo de los Colorados', 'Caminata corta por senderos de arcilla roja rodeando el Cerro de los Siete Colores.', 'Purmamarca', 'Purmamarca centro'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Paseo de los Colorados');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Paseo por el Bosque de Arrayanes', 'Caminata entre los troncos color canela del bosque único en la península de Quetrihué.', 'Villa La Angostura', 'Península de Quetrihué'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Paseo por el Bosque de Arrayanes');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Senderismo en La Cumbrecita', 'Trekking guiado por el bosque de abedules visitando la cascada y La Olla.', 'La Cumbrecita', 'La Cumbrecita centro'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Senderismo en La Cumbrecita');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Exploración del Cañón de Talampaya', 'Recorrido en vehículos del parque nacional entre paredones de arenisca roja y petroglifos.', 'Parque Nacional Talampaya', 'Ruta Nacional 76'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Exploración del Cañón de Talampaya');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Avistaje de Ballenas en Puerto Pirámides', 'Excursión embarcada para ver ballenas francas a pocos metros en el Golfo Nuevo.', 'Puerto Pirámides', 'Puerto Pirámides muelle'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Avistaje de Ballenas en Puerto Pirámides');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Cruce del Viaducto La Polvorilla', 'Travesía ferroviaria sobre el icónico viaducto del Tren a las Nubes a 4200 msnm.', 'San Antonio de los Cobres', 'RN 51'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Cruce del Viaducto La Polvorilla');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Avistaje de Yacarés en los Esteros', 'Safari fotográfico en lancha recorriendo las lagunas y embalsados de Iberá.', 'Colonia Carlos Pellegrini', 'Portal Laguna Iberá'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Avistaje de Yacarés en los Esteros');

INSERT INTO actividades (nombre, descripcion, localidad, direccion)
SELECT 'Recorrido por el Pucará de Tilcara', 'Visita arqueológica guiada por las ruinas de la fortaleza precolombina omaguaca.', 'Tilcara', 'Tilcara s/n'
WHERE NOT EXISTS (SELECT 1 FROM actividades WHERE nombre = 'Recorrido por el Pucará de Tilcara');


-- 2. Insert Itineraries if they don't already exist
INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Buenos Aires: Puerto Madero y Modernidad', 'Explorá el moderno barrio portuario porteño, sus restaurantes, vistas y el Puente de la Mujer.', 'CABA', '2026-10-01', '2026-10-02', 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/193_-_Buenos_Aires_-_Puerto_Madero_-_Janvier_2010.jpg/1200px-193_-_Buenos_Aires_-_Puerto_Madero_-_Janvier_2010.jpg', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Buenos Aires: Puerto Madero y Modernidad');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Bariloche: Vistas desde el Cerro Otto', 'Ascendé en teleférico al Cerro Otto para disfrutar de vistas únicas al Nahuel Huapi y merendar en su confitería.', 'RIO_NEGRO', '2026-07-20', '2026-07-21', 'https://upload.wikimedia.org/wikipedia/commons/c/cd/Cerro_Otto_-_Bariloche.jpg', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Bariloche: Vistas desde el Cerro Otto');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Mendoza: Enoturismo en Valle de Uco', 'Cabalgatas entre viñedos infinitos y degustaciones exclusivas de Malbec al pie de los Andes.', 'MENDOZA', '2026-09-05', '2026-09-06', 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/22/Ma%C3%B1anas_mendocinas.JPG/1200px-Ma%C3%B1anas_mendocinas.JPG', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Mendoza: Enoturismo en Valle de Uco');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Salta: Cafayate y Vinos de Altura', 'Descubrí los espectaculares cañones de la Quebrada de las Conchas y visitá bodegas boutique de Cafayate.', 'SALTA', '2026-10-15', '2026-10-16', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/Quebrada_de_las_Conchas_01.jpg/1280px-Quebrada_de_las_Conchas_01.jpg', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Salta: Cafayate y Vinos de Altura');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Iguazú: Selva y Aventura Náutica', 'Viví la adrenalina de navegar a la base de las Cataratas del Iguazú y recorrer las pasarelas selváticas.', 'MISIONES', '2026-08-12', '2026-08-14', 'https://upload.wikimedia.org/wikipedia/commons/f/f0/Cataratas027.jpg', 3, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Iguazú: Selva y Aventura Náutica');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'El Calafate: Navegación y Glaciar', 'Un recorrido embarcado frente a las majestuosas e imponentes paredes azules del Glaciar Perito Moreno.', 'SANTA_CRUZ', '2026-11-10', '2026-11-11', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/03/Glacial_iceberg_in_Argentina.jpg/1280px-Glacial_iceberg_in_Argentina.jpg', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'El Calafate: Navegación y Glaciar');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Ushuaia: Navegación por el Canal Beagle', 'Navegá saliendo del puerto austral hacia el icónico Faro Les Éclaireurs y observá lobos marinos.', 'TIERRA_DEL_FUEGO', '2026-11-20', '2026-11-21', 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Ushuaia_-_Costa_del_Canal_de_Beagle_2.JPG/1200px-Ushuaia_-_Costa_del_Canal_de_Beagle_2.JPG', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Ushuaia: Navegación por el Canal Beagle');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Jujuy: Purmamarca y Colores', 'Disfrutá del mágico entorno del Cerro de los Siete Colores y caminá por el sendero de los Colorados.', 'JUJUY', '2026-10-25', '2026-10-26', 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Cerro_de_los_siete_colores_Purmamarca_Jujuy.JPG/1200px-Cerro_de_los_siete_colores_Purmamarca_Jujuy.JPG', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Jujuy: Purmamarca y Colores');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Villa La Angostura: Bosque Arrayanes', 'Un recorrido lacustre hasta la península de Quetrihué para caminar en el único bosque puro de arrayanes.', 'NEUQUEN', '2026-07-28', '2026-07-29', 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/VillaLaAngostura089.JPG/1280px-VillaLaAngostura089.JPG', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Villa La Angostura: Bosque Arrayanes');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Córdoba: Senderismo La Cumbrecita', 'Trekking en el pintoresco pueblo peatonal, cruzando bosques de abedules hasta sus ollas de agua cristalina.', 'CORDOBA', '2026-09-15', '2026-09-16', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/04/La_Cumbrecita_2008-10-11.jpg/1280px-La_Cumbrecita_2008-10-11.jpg', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Córdoba: Senderismo La Cumbrecita');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'La Rioja: Parque Nacional Talampaya', 'Maravillate con los inmensos cañones rojos de Talampaya, los petroglifos prehispánicos y su fauna.', 'LA_RIOJA', '2026-09-22', '2026-09-23', 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Parque_Nacional_Talampaya_-_Petroglifos.jpg/1200px-Parque_Nacional_Talampaya_-_Petroglifos.jpg', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'La Rioja: Parque Nacional Talampaya');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Chubut: Avistaje de Ballenas', 'Embarcate en Puerto Pirámides para contemplar la imponente Ballena Franca Austral en su entorno natural.', 'CHUBUT', '2026-10-05', '2026-10-06', 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Ballenafranca%2Balvina.jpg/1200px-Ballenafranca%2Balvina.jpg', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Chubut: Avistaje de Ballenas');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Salta: Tren a las Nubes y Viaducto', 'Una travesía andina de altura sobre el increíble Viaducto La Polvorilla a más de 4200 metros.', 'SALTA', '2026-10-28', '2026-10-29', 'https://upload.wikimedia.org/wikipedia/commons/b/bd/Viaducto_La_Polvorilla_Tren_a_las_Nubes.JPG', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Salta: Tren a las Nubes y Viaducto');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Corrientes: Esteros del Iberá Salvaje', 'Navegación y avistaje de fauna autóctona (yacarés, ciervos, carpinchos) en uno de los humedales más grandes.', 'CORRIENTES', '2026-08-20', '2026-08-22', 'https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Los_Esteros_del_Iber%C3%A1._%22Portal_Cambyret%C3%A1%22._Corrientes.jpg/1200px-Los_Esteros_del_Iber%C3%A1._%22Portal_Cambyret%C3%A1%22._Corrientes.jpg', 3, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Corrientes: Esteros del Iberá Salvaje');

INSERT INTO itinerarios_sistema (titulo, descripcion, provincia, fecha_inicio, fecha_fin, foto_portada, duracion_dias, likes)
SELECT 'Jujuy: Tilcara y su Pucará', 'Recorré el sitio arqueológico prehispánico del Pucará de Tilcara con vistas hermosas de la Quebrada.', 'JUJUY', '2026-10-22', '2026-10-23', 'https://upload.wikimedia.org/wikipedia/commons/e/e6/Jujuy-Tilcara-Pucara-P3130003.JPG', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM itinerarios_sistema WHERE titulo = 'Jujuy: Tilcara y su Pucará');


-- 3. Link Itineraries to their Activities in itinerario_sistema_items
INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '10:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Buenos Aires: Puerto Madero y Modernidad'
  AND a.nombre = 'Caminata por Puerto Madero'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '11:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Bariloche: Vistas desde el Cerro Otto'
  AND a.nombre = 'Tarde en el Cerro Otto'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '10:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Mendoza: Enoturismo en Valle de Uco'
  AND a.nombre = 'Cabalgata y Degustación en Valle de Uco'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '09:30'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Salta: Cafayate y Vinos de Altura'
  AND a.nombre = 'Excursión a las Bodegas de Cafayate'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '10:30'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Iguazú: Selva y Aventura Náutica'
  AND a.nombre = 'Gran Aventura en las Cataratas'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '11:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'El Calafate: Navegación y Glaciar'
  AND a.nombre = 'Navegación al Glaciar Perito Moreno'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '09:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Ushuaia: Navegación por el Canal Beagle'
  AND a.nombre = 'Navegación al Faro Les Éclaireurs'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '10:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Jujuy: Purmamarca y Colores'
  AND a.nombre = 'Paseo de los Colorados'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '09:30'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Villa La Angostura: Bosque Arrayanes'
  AND a.nombre = 'Paseo por el Bosque de Arrayanes'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '10:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Córdoba: Senderismo La Cumbrecita'
  AND a.nombre = 'Senderismo en La Cumbrecita'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '10:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'La Rioja: Parque Nacional Talampaya'
  AND a.nombre = 'Exploración del Cañón de Talampaya'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '09:30'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Chubut: Avistaje de Ballenas'
  AND a.nombre = 'Avistaje de Ballenas en Puerto Pirámides'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '08:30'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Salta: Tren a las Nubes y Viaducto'
  AND a.nombre = 'Cruce del Viaducto La Polvorilla'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '10:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Corrientes: Esteros del Iberá Salvaje'
  AND a.nombre = 'Avistaje de Yacarés en los Esteros'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);

INSERT INTO itinerario_sistema_items (itinerario_sistema_id, actividad_id, dia, hora)
SELECT i.id_itinerario, a.id_actividad, 1, TIME '10:00'
FROM itinerarios_sistema i, actividades a
WHERE i.titulo = 'Jujuy: Tilcara y su Pucará'
  AND a.nombre = 'Recorrido por el Pucará de Tilcara'
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_items s WHERE s.itinerario_sistema_id = i.id_itinerario);


-- 4. Link Itineraries to their tags in itinerario_sistema_etiquetas
INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Buenos Aires: Puerto Madero y Modernidad' AND e.nombre IN ('CULTURA', 'COMPRA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Bariloche: Vistas desde el Cerro Otto' AND e.nombre IN ('NATURALEZA', 'AVENTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Mendoza: Enoturismo en Valle de Uco' AND e.nombre IN ('GASTRONOMIA', 'NATURALEZA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Salta: Cafayate y Vinos de Altura' AND e.nombre IN ('GASTRONOMIA', 'CULTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Iguazú: Selva y Aventura Náutica' AND e.nombre IN ('NATURALEZA', 'AVENTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'El Calafate: Navegación y Glaciar' AND e.nombre IN ('NATURALEZA', 'AVENTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Ushuaia: Navegación por el Canal Beagle' AND e.nombre IN ('NATURALEZA', 'AVENTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Jujuy: Purmamarca y Colores' AND e.nombre IN ('NATURALEZA', 'CULTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Villa La Angostura: Bosque Arrayanes' AND e.nombre IN ('NATURALEZA', 'CULTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Córdoba: Senderismo La Cumbrecita' AND e.nombre IN ('NATURALEZA', 'AVENTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'La Rioja: Parque Nacional Talampaya' AND e.nombre IN ('NATURALEZA', 'CULTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Chubut: Avistaje de Ballenas' AND e.nombre IN ('NATURALEZA', 'AVENTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Salta: Tren a las Nubes y Viaducto' AND e.nombre IN ('CULTURA', 'NATURALEZA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Corrientes: Esteros del Iberá Salvaje' AND e.nombre IN ('NATURALEZA', 'AVENTURA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);

INSERT INTO itinerario_sistema_etiquetas (itinerario_sistema_id, etiqueta_id)
SELECT i.id_itinerario, e.id
FROM itinerarios_sistema i, etiquetas e
WHERE i.titulo = 'Jujuy: Tilcara y su Pucará' AND e.nombre IN ('CULTURA', 'NATURALEZA')
  AND NOT EXISTS (SELECT 1 FROM itinerario_sistema_etiquetas s WHERE s.itinerario_sistema_id = i.id_itinerario AND s.etiqueta_id = e.id);
