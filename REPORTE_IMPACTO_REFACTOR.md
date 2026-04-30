# Reporte de impacto — Refactor de alineación entity↔DB

**Fecha:** 2026-04-22
**Proyecto:** TuristeAR backend (Spring Boot 3.5 + JPA + Supabase Postgres)
**Alcance:** Alinear entities Java con schema real de DB + aplicar Tarea 1 (campos nuevos)

---

## Resumen ejecutivo

El backend está **en estado de scaffolding** — prácticamente todos los controllers son stubs (`ResponseEntity.ok().build()`), no hay services implementados y los DTOs están vacíos. Esto hace que el riesgo del refactor sea **bajo**: no hay lógica de negocio que romper.

Los puntos de impacto real están concentrados en:
1. Los propios entities (`Itinerario`, `Actividad`, `Usuario`).
2. Los **query method names** de `ItinerarioRepository` y `ActividadRepository`, que Spring Data JPA parsea contra los nombres de campos del entity al arrancar.
3. Los `mappedBy` cruzados entre entities.

Con `ddl-auto=validate`, cualquier inconsistencia hace que la app **no arranque**, así que vamos a tener retroalimentación inmediata si algo queda desalineado.

---

## Estado del código

### Controllers — riesgo NULO

Todos los controllers son endpoints stub sin lógica:

| Archivo | Estado | Impacto del refactor |
|---|---|---|
| `ControllerAjustes.java` | Stubs | Ninguno |
| `ControllerAuth.java` | Stubs | Ninguno |
| `ControllerComunidad.java` | Stubs | Ninguno |
| `ControllerFavoritos.java` | Stubs | Ninguno |
| `ControllerItinerary.java` | Stubs | Ninguno |
| `ControllerPerfil.java` | Stubs | Ninguno |
| `TestController.java` | `/api/test/ping` → "pong" | Ninguno |

No hay código de controller que consuma los entities. **No hay que tocar nada acá.**

### Services y DTOs — riesgo NULO

Ambos directorios (`service/`, `dto/`, y subdirectorios `service/busqueda`, `service/generacion`, `service/itinerario`) están vacíos (solo `.gitkeep`). No hay código que refactorizar.

### Repositorios — riesgo MEDIO

Este es el único lugar donde el refactor requiere cambios.

#### `ItinerarioRepository.java`

```java
List<Itinerario> findByUsuarioIdUsuario(Long idUsuario);   // ROMPE
List<Itinerario> findByEsPublicoTrue();                     // OK
```

- **`findByUsuarioIdUsuario`**: el method name deriva de `itinerario.usuario.idUsuario`. Tras renombrar el campo en `Itinerario` de `usuario` → `creador`, este nombre no compila contra JPA y la app no arranca.
  **Fix:** renombrar a `findByCreadorIdUsuario(Long idUsuario)`.
- **`findByEsPublicoTrue`**: el campo `esPublico` y su columna `es_publico` existen y no cambian. Queda como está.

#### `ActividadRepository.java`

```java
List<Actividad> findByItinerarioIdItinerario(Long idItinerario);   // ROMPE
```

- El method name depende de `actividad.itinerario.idItinerario`. Tras eliminar el `@ManyToOne` directo de `Actividad` a `Itinerario` (la relación pasa a ser N:M vía `itinerario_items`), este method no compila.
  **Fix propuesto:** reemplazar por una query explícita con `@Query`:
  ```java
  @Query("SELECT i.actividad FROM ItinerarioItem i WHERE i.itinerario.idItinerario = :idItinerario")
  List<Actividad> findByItinerarioId(@Param("idItinerario") Long idItinerario);
  ```
  O alternativamente, mover este método a `ItinerarioItemRepository` (nuevo) como `findByItinerarioIdItinerario` devolviendo `List<ItinerarioItem>`. A decidir cuando lleguemos a Fase 7.

#### `PublicacionRepository.java`

```java
List<Publicacion> findByUsuarioIdUsuario(Long idUsuario);        // OK
List<Publicacion> findAllByOrderByCantFavoritosDesc();           // VERIFICAR
```

- `findByUsuarioIdUsuario` → la entity `Publicacion` no está en el scope del refactor. Mantiene su `@JoinColumn(name = "id_usuario")` y `publicaciones.id_usuario` existe en la DB. Sin cambios.
- `findAllByOrderByCantFavoritosDesc` → depende del campo `cantFavoritos` → columna `cant_favoritos`. **No verificamos si esta columna existe en `publicaciones` en la DB**; quedó fuera del SELECT inicial. Recomendación: correr una query de verificación sobre `publicaciones` antes de arrancar la Fase 2, para no sorprendernos.

#### `UsuarioRepository.java`

```java
Optional<Usuario> findByEmail(String email);    // OK
boolean existsByEmail(String email);            // OK
```

Sin cambios.

---

## Entities — cambios requeridos

### `Itinerario.java`

| Línea actual | Cambio | Motivo |
|---|---|---|
| `@JoinColumn(name = "id_usuario", nullable = false)` + `private Usuario usuario` | Renombrar a `@JoinColumn(name = "creador_id", nullable = false)` + `private Usuario creador` | DB real usa `creador_id`, no `id_usuario` |
| `@OneToMany(mappedBy = "itinerario") private List<Actividad> actividades` | Cambiar a `@OneToMany(mappedBy = "itinerario") private List<ItinerarioItem> items` | La relación Itinerario↔Actividad es N:M vía `itinerario_items` |
| (ausente) | Agregar `@Column(nullable = false) private String destino` | Existe en DB como NOT NULL |
| `private String guia` | Borrar campo | Se dropea de la DB (columna fantasma) |
| (ausente) | Agregar `@Column(name = "imagen_url") private String imagenUrl` | Tarea 1 |

### `Actividad.java`

| Línea actual | Cambio | Motivo |
|---|---|---|
| `@ManyToOne @JoinColumn(name = "id_itinerario", nullable = false) private Itinerario itinerario` | Borrar | No existe FK directa en la DB |
| `@Column(name = "fecha_hora_inicio") private LocalDateTime fechaHoraInicio` | Borrar | Se dropea de la DB (columna fantasma) |
| `@Column(name = "fecha_hora_final") private LocalDateTime fechaHoraFinal` | Borrar | Idem |
| (ausente) | Agregar `@OneToMany(mappedBy = "actividad") private List<ItinerarioItem> items` | Relación inversa del junction |
| (ausente) | Agregar `@OneToMany(mappedBy = "actividad") private List<ActividadEtiqueta> etiquetas` | Existe tabla, sin mapear |
| (ausente) | Agregar `@Column(name = "imagen_url") private String imagenUrl` | Tarea 1 |

### `Usuario.java`

| Línea actual | Cambio | Motivo |
|---|---|---|
| `@OneToMany(mappedBy = "usuario") private List<Itinerario> itinerarios` | Cambiar a `@OneToMany(mappedBy = "creador") private List<Itinerario> itinerarios` | El `mappedBy` referencia el campo renombrado |
| (ausente) | Agregar `@Column(name = "foto_url") private String fotoUrl` | Tarea 1 |
| (ausente, opcional) | Agregar `@OneToMany(mappedBy = "usuario") private List<Favorito> favoritos` | Si se quiere relación navegable |

### `Publicacion.java`

Sin cambios directos. Convive sin problemas con el refactor.

---

## Entities nuevas a crear

### `ItinerarioItem.java` (tabla `itinerario_items`)

PK: `id` (IDENTITY, ya es identity en DB).

Campos tras migración (Variante A confirmada):
- `id` (Long, PK, identity)
- `fecha_hora_inicio` (LocalDateTime) — se agrega en Fase 5
- `fecha_hora_final` (LocalDateTime) — se agrega en Fase 5
- `actividad_id` (FK → actividades)
- `itinerario_id` (FK → itinerarios)

`@ManyToOne` a `Actividad` y a `Itinerario`.

### `ActividadEtiqueta.java` (tabla `actividad_etiquetas`)

La tabla tiene PK compuesta implícita por (`actividad_id`, `etiqueta`). No hay PK explícita declarada, lo que es inusual — tendríamos que confirmar si hay un `UNIQUE` constraint o si está totalmente desnormalizada. **Acción recomendada:** query adicional en Supabase antes de Fase 4 para confirmar la PK/UNIQUE de esta tabla.

Probable estructura final:
- `@IdClass(ActividadEtiquetaId.class)` o `@Embeddable`
- Dos campos `@Id`: `actividadId` (Long) y `etiqueta` (String)

### `Favorito.java` (tabla `favoritos`)

PK compuesta: (`usuario_id`, `itinerario_id`).

- `@IdClass(FavoritoId.class)`
- `@Id @ManyToOne Usuario usuario` (`@JoinColumn(name = "usuario_id")`)
- `@Id @ManyToOne Itinerario itinerario` (`@JoinColumn(name = "itinerario_id")`)
- `@Column(name = "fecha_agregado", nullable = false) private LocalDateTime fechaAgregado` — agregada en Fase 6 (Tarea 1)

---

## Orden de ejecución propuesto

Este orden minimiza el tiempo en que la app queda inarrancable:

1. **Verificaciones previas** (1 query SQL): confirmar estructura de `publicaciones` y PK/UNIQUE de `actividad_etiquetas`.
2. **Fase 5 primero (parcial)**: migraciones en Supabase de las columnas fantasma y `creador_id` NOT NULL. Después de esto, con `ddl-auto=validate`, la app sigue arrancando porque los entities todavía esperan los campos viejos.
   - ⚠️ En realidad no — si dropeamos `fecha_hora_inicio` de `actividades` pero el entity aún lo tiene mapeado, `validate` falla al arrancar. El orden importa.
3. **Secuencia correcta**: modificar entities y aplicar migración DB al mismo tiempo (en el mismo commit), con la app apagada. Reencender con `ddl-auto=validate` para verificar.

Estrategia de ejecución detallada (a refinar en Fase 2+):

- **Paso 1:** Modificar los 3 entities (Itinerario, Actividad, Usuario) + crear los 3 nuevos (ItinerarioItem, ActividadEtiqueta, Favorito) + actualizar repositories. App apagada.
- **Paso 2:** Aplicar las migraciones de Fase 5 en Supabase (DROPs + ALTERs + cambio `itinerario_items`).
- **Paso 3:** Intentar arrancar la app. Si arranca con `ddl-auto=validate`, el refactor está completo y alineado.
- **Paso 4:** Aplicar Tarea 1 (Fase 6) como paso separado y más tranquilo. Siempre DB primero (migration), después agregar campos en entities.

---

## Riesgos identificados

| Riesgo | Severidad | Mitigación |
|---|---|---|
| App no arranca tras refactor por typo en `mappedBy` o `@JoinColumn` | Alta | `ddl-auto=validate` da error claro en startup; iterar rápido |
| `actividad_etiquetas` sin PK explícita dificulta el `@IdClass` | Media | Verificar constraints antes de Fase 4; si está desnormalizada, agregar PK compuesta primero |
| `publicaciones.cant_favoritos` podría no existir en la DB real | Baja | Verificar con SELECT antes de Fase 2 |
| Dato existente en `itinerarios` (1 fila) con `creador_id=1` → NOT NULL sin drama | Nulo | Confirmado |
| Refactor sube a main sin tests | Alta | Hay 0 tests; sugerir agregar al menos smoke test de arranque |

---

## Queries de verificación pendientes antes de Fase 2

```sql
-- Confirmar estructura de publicaciones (para el método cantFavoritos)
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'publicaciones'
ORDER BY ordinal_position;

-- Confirmar constraints de actividad_etiquetas (PK, UNIQUE)
SELECT tc.constraint_type, tc.constraint_name, kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_schema = 'public' AND tc.table_name = 'actividad_etiquetas';
```

---

## Checklist final antes de arrancar Fase 2

- [ ] Queries de verificación arriba ejecutadas y resultados compartidos.
- [ ] Decisión sobre cómo resolver `ActividadEtiqueta` si no tiene PK explícita.
- [ ] Confirmación de que `publicaciones.cant_favoritos` existe.
- [ ] Aprobación del orden de ejecución.
