# TuristeAR — Plan de Pruebas

Smoke test end-to-end de los endpoints refactorizados. Asume que:

- La app corre en `http://localhost:8080` (`./mvnw spring-boot:run`)
- La DB de Supabase ya tiene el seed aplicado (etiquetas, actividades, 2 itinerarios del sistema con items y tags)

## 0. Arrancar la app

```bash
cd C:\Proyecto_Facultad\turistear-backend\turistear-backend
./mvnw spring-boot:run
```

Si Hibernate protesta con `Schema-validation: missing column [...]` o similar, es que una entidad Java no matchea con Supabase. Revisar el log y comparar con `@Column(name = ...)`. En este punto NO debería haber errores — verificamos columna por columna.

Swagger queda en `http://localhost:8080/swagger-ui.html`.

---

## 1. Auth (público)

### Register

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan@test.com",
    "contrasenia": "Password123"
  }'
```

Respuesta esperada (200):
```json
{
  "token": "eyJhbGciOi...",
  "idUsuario": 1,
  "nombre": "Juan",
  "email": "juan@test.com"
}
```

**Guardá el `token`**. Para el resto de los requests, exportalo:

```bash
TOKEN="eyJhbGciOi..."
```

### Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@test.com",
    "contrasenia": "Password123"
  }'
```

---

## 2. Itinerarios del sistema (requiere JWT)

### Listar todo

```bash
curl http://localhost:8080/itinerario/explorar \
  -H "Authorization: Bearer $TOKEN"
```

Devuelve 2 itinerarios (Bariloche, Mendoza). Sin items — para detalle se usa el endpoint por id.

### Filtrar por categoría

```bash
curl "http://localhost:8080/itinerario/explorar?categoria=AVENTURA" \
  -H "Authorization: Bearer $TOKEN"
```

Debe devolver solo "Aventura en Bariloche".

### Ranking (por veces guardado como favorito)

```bash
curl "http://localhost:8080/itinerario/explorar?ordenar=favoritos" \
  -H "Authorization: Bearer $TOKEN"
```

Al principio están todos en 0 — el orden es estable pero arbitrario. Después de marcar favoritos cambia.

### Buscar por preferencias

```bash
curl "http://localhost:8080/itinerario/buscar?provincia=MENDOZA&tags=GASTRONOMIA&fechaInicio=2026-08-01&fechaFin=2026-08-31" \
  -H "Authorization: Bearer $TOKEN"
```

Debe devolver "Gastronomía mendocina".

### Detalle (con items)

```bash
curl http://localhost:8080/itinerario/1 \
  -H "Authorization: Bearer $TOKEN"
```

Ajustá el `1` con el id real (lo ves en la lista de explorar). Debe traer titulo, descripcion, items con sus actividades, etiquetas.

---

## 3. Favoritos (CRUD completo de copias del usuario)

### Agregar a favoritos (crea copia profunda)

```bash
curl -X POST http://localhost:8080/favoritos/1 \
  -H "Authorization: Bearer $TOKEN"
```

Devuelve la copia recién creada con sus items. Las fechas vienen heredadas del itinerario del sistema (2026-07-10 a 2026-07-12 para Bariloche). **Guardá el `idItinerarioUsuario`** que se devuelve.

```bash
ID_FAV=1   # el id de la copia
```

Intentar guardarlo otra vez devuelve 409 (ya está en favoritos).

### Listar mis favoritos

```bash
curl http://localhost:8080/favoritos \
  -H "Authorization: Bearer $TOKEN"
```

### Favorito activo

```bash
curl http://localhost:8080/favoritos/activo \
  -H "Authorization: Bearer $TOKEN"
```

Devuelve el favorito de fecha más próxima (o en curso). Si todas las fechas ya pasaron → 404.

### Detalle

```bash
curl http://localhost:8080/favoritos/$ID_FAV \
  -H "Authorization: Bearer $TOKEN"
```

### Actualizar fechas

```bash
curl -X PUT http://localhost:8080/favoritos/$ID_FAV \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fechaInicio": "2026-09-01", "fechaFin": "2026-09-03"}'
```

Si mandás `fechaFin < fechaInicio` debe responder 400.

### Agregar item nuevo

```bash
curl -X POST http://localhost:8080/favoritos/$ID_FAV/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombreActividad": "Mate a la mañana frente al lago",
    "descripcion": "Improvisado, llevamos bizcochitos",
    "localidad": "Bariloche",
    "direccion": "Costanera",
    "dia": 1,
    "hora": "08:00"
  }'
```

Devuelve 201 con el item creado y su `id`. Guardá el id:

```bash
ITEM_ID=4   # el id del nuevo item
```

### Editar item

```bash
curl -X PUT http://localhost:8080/favoritos/$ID_FAV/items/$ITEM_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombreActividad": "Mate y facturas frente al lago",
    "descripcion": "Compramos en la panadería de la esquina",
    "localidad": "Bariloche",
    "direccion": "Costanera",
    "dia": 1,
    "hora": "08:30"
  }'
```

### Eliminar item

```bash
curl -X DELETE http://localhost:8080/favoritos/$ID_FAV/items/$ITEM_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Eliminar favorito completo

```bash
curl -X DELETE http://localhost:8080/favoritos/$ID_FAV \
  -H "Authorization: Bearer $TOKEN"
```

CASCADE en Supabase + orphanRemoval en JPA borran los items. Verificar que al listar favoritos ya no aparece.

---

## 4. Validaciones a chequear

| Caso | Endpoint | Resultado esperado |
|---|---|---|
| Token inválido o expirado | Cualquiera con JWT | 401 |
| Acceder a favorito de otro usuario | GET/PUT/DELETE `/favoritos/{id}` con `{id}` ajeno | 404 (no 403, por diseño) |
| `idItinerarioSistema` inexistente | POST `/favoritos/9999` | 404 |
| Guardar mismo itinerario 2 veces | POST `/favoritos/{id}` repetido | 409 |
| `fechaFin < fechaInicio` en PUT | PUT `/favoritos/{id}` | 400 |
| Categoría inválida | `?categoria=FOO` | 400 (Spring rechaza el enum) |
| Activo sin viajes futuros | GET `/favoritos/activo` | 404 |

---

## 5. Lo que vale la pena chequear en Swagger UI

`http://localhost:8080/swagger-ui.html` muestra todos los endpoints generados, con los DTOs de request/response. Útil para:

- Ver el shape exacto de cada response
- Probar endpoints sin armar curl
- Confirmar que el "Authorize" funciona — pegás `Bearer <token>` y se aplica a todas las requests

---

## 6. Datos seed cargados

| Tabla | Cantidad |
|---|---|
| etiquetas | 6 (NATURALEZA, GASTRONOMIA, AVENTURA, CULTURA, NOCHE, COMPRA) |
| actividades | 6 (3 Bariloche + 3 Mendoza) |
| itinerarios_sistema | 2 (Aventura en Bariloche, Gastronomía mendocina) |
| itinerario_sistema_items | 6 (3 por itinerario) |
| itinerario_sistema_etiquetas | 4 (2 tags por itinerario) |
