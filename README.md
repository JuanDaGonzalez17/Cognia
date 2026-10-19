# Cognia API

Cognia is an adaptive learning platform. The curriculum slice provides
subject and concept queries backed by PostgreSQL, plus prerequisite graph
queries and updates.

## Requisitos

- Java 17 (toolchain del proyecto)
- Docker en ejecución para los tests de integración con Testcontainers

## Configurar Neon (desarrollo)

El perfil `dev` es el predeterminado y usa la conexión JDBC directa de Neon.
No carga ni modifica el schema. Copia `.env.example` a `.env`, sustituye los
valores de ejemplo por tus credenciales y exporta estas variables en la shell
antes de iniciar la app:

```powershell
$env:NEON_JDBC_URL = "jdbc:postgresql://<host-directo>/<base>?sslmode=require"
$env:NEON_DB_USER = "<usuario>"
$env:NEON_DB_PASSWORD = "<contraseña>"
```

No guardes credenciales reales en Git. `.env` está ignorado.

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
```

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
para revisión docente. Aplícalo manualmente en Neon solo después de validarlo;
la aplicación no ejecuta schema ni seed al iniciar.

## Test and build

```bash
./gradlew test
./gradlew build
```

Los tests usan PostgreSQL 16 mediante Testcontainers y requieren Docker. Se
ejecutan con el perfil `test`; nunca toman la URL de Neon.

## Architecture

The application follows Hexagonal Architecture in a modular-monolith
structure:

```text
com.cognia.api
├── domain/model                 # Framework-independent curriculum model
├── application/port/in          # Use-case interfaces
├── application/port/out         # Outbound repository ports
├── application/service           # Use-case orchestration
└── adapter
    ├── in/web                    # REST entry adapter and response DTOs
    └── out/persistence            # JPA entities, mappers, and PostgreSQL adapters
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
  nombres en español, traducidos dentro del adaptador JPA.
- El seed de Cálculo Integral y sus aristas son provisionales y no representan
  un currículo oficial.
- Authentication, diagnostics, mastery calculation, learning-path adaptation,
  assessments, reviews, and the remaining product model are intentionally
  deferred until their concrete use cases and rules are defined.
