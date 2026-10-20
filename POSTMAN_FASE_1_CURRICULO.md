# Fase 1 — Currículo: guía de Postman

## Qué incluye esta fase

- Persistencia PostgreSQL para asignaturas, conceptos y prerrequisitos, con entidades JPA separadas del dominio y mappers explícitos entre nombres españoles del schema y nombres ingleses del dominio.
- Validación pura del grafo: rechaza autoenlaces, prerrequisitos de otra asignatura y ciclos; también rechaza aristas duplicadas.
- Seed de ejemplo en `src/main/resources/db/seed-calculo-integral.sql`. Es provisional y requiere revisión docente. La aplicación no ejecuta el seed ni modifica el schema (`ddl-auto: none`).
- Pruebas de dominio, servicio, persistencia con Testcontainers y controllers con MockMvc. La suite completa pasó según la ejecución manual del usuario.

## Preparación

1. Inicia la API con el perfil y las variables de base de datos configurados:

   ```powershell
   .\gradlew.bat bootRun
   ```

2. En Postman, crea una variable `baseUrl` con valor `http://localhost:8080`.
3. Aplica manualmente el seed provisional a la base conectada si todavía no están sus filas. No ejecutes el seed contra un currículo compartido sin validarlo primero con un docente.

## Datos incluidos en el seed

| Variable sugerida | UUID | Código / relación |
|---|---|---|
| `subjectId` | `10000000-0000-0000-0000-000000000001` | `calculo-integral` |
| `conceptAntiderivatives` | `20000000-0000-0000-0000-000000000001` | `C0.1` — Funciones primitivas y antiderivadas |
| `conceptIntegrationRules` | `20000000-0000-0000-0000-000000000002` | `C1.1` — Reglas de integración |
| `conceptDefiniteIntegrals` | `20000000-0000-0000-0000-000000000003` | `C2.1` — Integrales definidas |
| `conceptImproperIntegrals` | `20000000-0000-0000-0000-000000000004` | `C3.1` — Integrales impropias |

El grafo provisional del seed es:

- `C1.1` requiere `C0.1`.
- `C2.1` requiere `C1.1`.
- `C3.1` requiere `C2.1`.

Las relaciones son solamente datos de prueba, no un currículo oficial.

## Endpoints

Todos usan la variable `{{baseUrl}}` y el prefijo `/api/v1`.

| Método | Ruta | Resultado esperado |
|---|---|---|
| `GET` | `/subjects` | Lista de asignaturas. `200 OK`. |
| `GET` | `/subjects/{{subjectId}}` | Una asignatura. `200 OK`; `404` si no existe. |
| `GET` | `/subjects/{{subjectId}}/concepts` | Conceptos ordenados por orden sugerido. `200 OK`; `404` si no existe la asignatura. |
| `GET` | `/concepts/{{conceptIntegrationRules}}/prerequisites` | Prerrequisitos directos del concepto. `200 OK`; `404` si no existe. |
| `POST` | `/concepts/{{conceptId}}/prerequisites` | Agrega un prerrequisito directo. `201 Created` si se guarda. |

### Consultar asignaturas

```http
GET {{baseUrl}}/api/v1/subjects
```

```http
GET {{baseUrl}}/api/v1/subjects/{{subjectId}}
```

La barra entre `subjects` y el UUID es obligatoria. La URL completa de ejemplo es:

```text
http://localhost:8080/api/v1/subjects/10000000-0000-0000-0000-000000000001
```

### Consultar conceptos de Cálculo Integral

```http
GET {{baseUrl}}/api/v1/subjects/{{subjectId}}/concepts
```

La respuesta incluye `id`, `subjectId`, `code`, `name`, `description`, `level`, `order` y `active`.

### Consultar prerrequisitos

```http
GET {{baseUrl}}/api/v1/concepts/{{conceptIntegrationRules}}/prerequisites
```

La respuesta contiene las aristas directas. Para `C1.1`, el `prerequisiteId` esperado es `conceptAntiderivatives`.

### Agregar un prerrequisito válido

Este ejemplo añade una arista transitiva directa de `C3.1` a `C0.1`; no crea un ciclo ni duplica una arista existente.

```http
POST {{baseUrl}}/api/v1/concepts/{{conceptImproperIntegrals}}/prerequisites
Content-Type: application/json
```

```json
{
  "prerequisiteId": "20000000-0000-0000-0000-000000000001"
}
```

Respuesta esperada: `201 Created`.

### Comprobar errores

Los errores devuelven `ProblemDetail` con `title`, `status` y `detail`.

| Caso | Método y ruta | Body | Estado |
|---|---|---|---:|
| Concepto inexistente | `GET /concepts/00000000-0000-0000-0000-000000000099/prerequisites` | — | `404` |
| Autoenlace | `POST /concepts/{{conceptAntiderivatives}}/prerequisites` | `{"prerequisiteId":"20000000-0000-0000-0000-000000000001"}` | `422` |
| Arista duplicada del seed | `POST /concepts/{{conceptIntegrationRules}}/prerequisites` | `{"prerequisiteId":"20000000-0000-0000-0000-000000000001"}` | `409` |
| Ciclo | `POST /concepts/{{conceptAntiderivatives}}/prerequisites` | `{"prerequisiteId":"20000000-0000-0000-0000-000000000002"}` | `409` |
| Otra asignatura | `POST /concepts/{{conceptAntiderivatives}}/prerequisites` | `{"prerequisiteId":"20000000-0000-0000-0000-000000000005"}` | `422` |

Para comprobar el caso de otra asignatura, primero crea estas dos filas **solo en una base de prueba controlada** (la API no incluye endpoints para crear asignaturas ni conceptos):

```sql
INSERT INTO asignatura (id, codigo, nombre)
VALUES ('10000000-0000-0000-0000-000000000002', 'postman-test', 'Asignatura de prueba');

INSERT INTO concepto (id, asignatura_id, codigo, nombre, orden_sugerido)
VALUES ('20000000-0000-0000-0000-000000000005',
        '10000000-0000-0000-0000-000000000002', 'X1', 'Concepto externo de prueba', 0);
```

Después envía el caso indicado en la tabla. La restricción compuesta de la base también impide guardar esa arista aunque se intente saltar la validación de aplicación.

## Pruebas automatizadas

Para ejecutar la suite completa desde PowerShell:

```powershell
.\gradlew.bat test
```

Las pruebas de persistencia usan PostgreSQL con Testcontainers y cargan el schema y el seed; usan el perfil `test`, no Neon.

## Solución de problemas

- `GET /api/v1/subjects` responde `200` con `[]`: la base de datos a la que está conectada esta instancia no contiene asignaturas. El perfil de desarrollo usa `NEON_JDBC_URL`, `NEON_DB_USER` y `NEON_DB_PASSWORD`; confirma que Postman está consultando la instancia esperada y que el seed se aplicó a esa misma base/branch. La aplicación no lo ejecuta al arrancar.
- `GET /api/v1/subjects10000000-...` responde `404`: falta `/` antes del UUID. Usa `/api/v1/subjects/10000000-...`.
- Si usas el `subjectId` del seed y la ruta está bien formada, una base sin esa fila devuelve 404 con `ProblemDetail`; una ruta mal formada como la anterior cae en el 404 genérico de Spring.

Para verificar dónde quedó insertado el seed, ejecuta en el SQL Editor de Neon:

```sql
SELECT current_database(), current_schema(), current_user;
SELECT id, codigo, nombre
FROM public.asignatura
ORDER BY codigo;
```

Compara la branch/base seleccionada en Neon con el host y el nombre de base de `NEON_JDBC_URL`. El proyecto ignora `.env` y Spring Boot no lo carga automáticamente: exporta las tres variables en la terminal o configúralas en el Run Configuration del IDE, y reinicia la aplicación después de cambiarlas. No compartas los valores de usuario, contraseña ni URL completos.
