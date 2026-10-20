# Cognia API

Cognia is an adaptive learning platform. The curriculum slice provides
subject and concept queries backed by PostgreSQL, plus prerequisite graph
queries and updates.

## Requisitos

- Java 17 (toolchain del proyecto)
- Docker en ejecuciÃ³n para los tests de integraciÃ³n con Testcontainers

## Configurar Supabase (desarrollo)

El perfil `dev` es el predeterminado y se conecta a PostgreSQL mediante las
variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y
`SPRING_DATASOURCE_PASSWORD`. Usa los valores de conexión que proporciona
Supabase; la URL JDBC debe incluir `sslmode=require`. Supabase ofrece conexión
directa y conexiones mediante pooler: usa el host, puerto y usuario indicados
para el modo elegido (la conexión directa normalmente usa el puerto `5432`).

Spring Boot no carga `.env` automáticamente. Define las tres variables en el
entorno desde el que inicias Gradle, o configúralas en el Run Configuration del
IDE. En PowerShell, por ejemplo:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://<host>:<puerto>/postgres?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME = "<usuario de Supabase>"
$env:SPRING_DATASOURCE_PASSWORD = "<contraseña de Supabase>"
.\gradlew.bat bootRun
```

Si guardas esos valores en `Cognia/.env`, cópialos al entorno antes de ejecutar
la aplicación; editar el archivo por sí solo no los activa. No guardes ni
compartas credenciales reales en Git o en documentación. La aplicación no
carga ni modifica el schema ni ejecuta seeds al iniciar.

## Run

```bash
./gradlew bootRun
```

En Windows PowerShell, usa `.\gradlew.bat bootRun`.

The API is available at `http://localhost:8080`.

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/subjects
curl http://localhost:8080/api/v1/subjects/10000000-0000-0000-0000-000000000001
curl http://localhost:8080/api/v1/subjects/10000000-0000-0000-0000-000000000001/concepts
curl http://localhost:8080/api/v1/concepts/20000000-0000-0000-0000-000000000002/prerequisites
curl http://localhost:8080/api/v1/concepts/20000000-0000-0000-0000-000000000001/practice-activities
```

La Fase 2 agrega `GET /api/v1/concepts/{conceptId}/practice-activities` (filtro opcional `?difficulty=1..3`) y `POST /api/v1/activities/{activityId}/practice-attempts`. El POST recibe `X-Student-Id` temporal y JSON con `optionId` para selecciÃ³n mÃºltiple o `answer` para respuesta corta (opcional `seconds`). La consulta no revela respuestas correctas ni criterios. Ejemplos completos, UUIDs fijos del seed y errores: [docs/fase-2-postman.md](docs/fase-2-postman.md).

La Fase 3 agrega diagnóstico inicial por asignatura y consulta del perfil. Usa `X-Student-Id` temporal; las respuestas del diagnóstico se registran sin devolver corrección hasta el cierre. El dominio se estima por concepto con pesos provisionales configurados en `application.yaml` (1.0, 1.5 y 2.0 por dificultad 1, 2 y 3). Aplica manualmente `src/main/resources/db/seed-diagnostico.sql` después de los seeds previos. Endpoints, ejemplos y errores: [docs/fase-3-postman.md](docs/fase-3-postman.md).

La Fase 4 incorpora rutas personalizadas y evaluación corta por concepto. Los umbrales están en `application.yaml` y son provisionales. Aplica manualmente `db/seed-evaluacion-corta.sql`; para una base existente, aplica el `ALTER TABLE` indicado en [docs/fase-4-postman.md](docs/fase-4-postman.md). Flujo, estados y ejemplos: [docs/fase-4-postman.md](docs/fase-4-postman.md).

Aplica manualmente `src/main/resources/db/seed-actividades.sql` despuÃ©s del seed curricular; es material provisional de desarrollo que debe validar un docente. No hay migraciones ni inicializaciÃ³n automÃ¡tica del schema o seeds.

Para agregar un prerrequisito:

```bash
curl -X POST http://localhost:8080/api/v1/concepts/20000000-0000-0000-0000-000000000002/prerequisites \
  -H 'Content-Type: application/json' \
  -d '{"prerequisiteId":"20000000-0000-0000-0000-000000000001"}'
```

El endpoint devuelve `201` al guardar, `404` si no existe la asignatura o
concepto, `422` para autoenlaces o conceptos de otra asignatura y `409` para
ciclos o aristas duplicadas. Los errores usan `ProblemDetail`.

`src/main/resources/db/seed-calculo-integral.sql` es un ejemplo provisional
para revisiÃ³n docente. AplÃ­calo manualmente en Supabase solo despuÃ©s de validarlo;
la aplicaciÃ³n no ejecuta schema ni seed al iniciar.

## Test and build

```bash
./gradlew test
./gradlew build
```

Los tests usan PostgreSQL 16 mediante Testcontainers y requieren Docker. Se
ejecutan con el perfil `test`; nunca toman la URL de Supabase.

## Architecture

The application follows Hexagonal Architecture in a modular-monolith
structure:

```text
com.cognia.api
â”œâ”€â”€ domain/model                 # Framework-independent curriculum model
â”œâ”€â”€ application/port/in          # Use-case interfaces
â”œâ”€â”€ application/port/out         # Outbound repository ports
â”œâ”€â”€ application/service           # Use-case orchestration
â””â”€â”€ adapter
    â”œâ”€â”€ in/web                    # REST entry adapter and response DTOs
    â””â”€â”€ out/persistence            # JPA entities, mappers, and PostgreSQL adapters
```

The request flow is:
`REST controller -> use case -> repository port -> JPA adapter -> response DTO`.

Domain records validate their own essential invariants. The controller maps
domain objects to API response DTOs, so HTTP and persistence concerns do not
leak into the domain.

## Current limitations and assumptions

- Hibernate usa `ddl-auto=none` y `spring.sql.init.mode=never`; la app no
  modifica el schema.
- Las consultas usan UUID en las rutas; las tablas y columnas conservan sus
  nombres en espaÃ±ol, traducidos dentro del adaptador JPA.
- El seed de CÃ¡lculo Integral y sus aristas son provisionales y no representan
  un currÃ­culo oficial.
- Authentication, learning-path adaptation, short assessments, reviews, and
  the remaining product model are intentionally deferred. Diagnostic mastery
  weights and seed content are provisional pending teacher validation.

