# Cognia API

Cognia es una plataforma de aprendizaje adaptativo con repetición espaciada. Proporciona gestión curricular con grafos de dependencias (DAG), práctica formativa con retroalimentación inmediata, diagnóstico inicial de maestría, rutas de aprendizaje personalizadas con ordenación topológica y evaluaciones sumativas cortas con versionado y recálculo de rutas.

---

## 📊 Estado de Ejecución y Hallazgos

A fecha de **Octubre 2026**, el proyecto presenta los siguientes indicadores de avance actualizados:

* **Avance del Backend Core API (Fases 0 a 5):** **~98%** (Fases 0 a 3 y 5 completadas; las pruebas dedicadas de persistencia y controller de Fase 4 fueron ejecutadas por el usuario y pasaron. La suite completa queda pendiente).
* **Avance Global del Backend (incluyendo Fase 6):** **~90%** (Slices 1 y 2 implementados y verificados por el usuario; slice 3 de autenticación JWT implementado, pendiente de pruebas manuales y automatizadas).
* **Avance del Frontend (`FrontPI`):** **~45%** (Interfaz y flujos completos en prototipo React 19 + Tailwind CSS v4 con estado local/mock; pendiente capa de integración HTTP con la API).
* **Avance Global del Producto:** **~65% - 70%**.

### Tablero de Fases

| Fase | Título | Estado | % Avance | Hito / Criterio de Cierre |
| :--- | :--- | :---: | :---: | :--- |
| **Fase 0** | Preparación y Base | ✅ **Completada** | 100% | Configuración base, PostgreSQL/Supabase, Testcontainers, Schema unificado. |
| **Fase 1** | Currículo y Prerrequisitos | ✅ **Completada** | 100% | CRUD asignaturas/conceptos, validación acíclica de grafo y seed Cálculo Integral. |
| **Fase 2** | Actividades y Práctica | ✅ **Completada** | 100% | Actividades formativas (SM y RC), feedback inmediato, registro de intentos. |
| **Fase 3** | Diagnóstico y Dominio | ✅ **Completada** | 100% | Sesiones diagnósticas, cálculo de maestría inicial y auditoría en historial. |
| **Fase 4** | Ruta y Evaluación Corta | 🟡 **Implementada** | 95% | Rutas topológicas y evaluación corta listas. Pruebas de persistencia Testcontainers y controller MockMvc añadidas y confirmadas por el usuario; suite completa pendiente. |
| **Fase 5** | Repasos SM-2 y Examen Global | ✅ **Completada** | 100% | Algoritmo SM-2, cola diaria, amnesia conceptual con reapertura idempotente, examen global y tests de dominio, aplicación, persistencia y controller listos. |
| **Fase 6** | Transversales y Frontend | 🟡 **En Progreso** | ~55% | Slices 1 y 2 implementados y verificados por el usuario. Slice 3 implementado: registro, login, JWT y protección stateless; pendiente probar y completar compatibilidad X-Student-Id en el slice 4. Siguen pendientes integración HTTP Front-Back y CI/CD. |

---

## ⚠️ Hallazgos y Consideraciones Clave

1. **Gestión de Base de Datos y Schema:**
   - **Sin migraciones automáticas:** Hibernate opera con `ddl-auto: none` y `spring.sql.init.mode: never`. Las modificaciones al esquema deben aplicarse manualmente en Supabase/PostgreSQL.

   - **Constraint de Fase 4:** Confirmar que la restricción `ck_sesion_evaluacion_paso_requerido` esté aplicada en la base remota (ver [docs/fase-4-postman.md](docs/fase-4-postman.md)).

2. **Parámetros y Calibración Pedagógica (Validación Docente):**
   - Los umbrales de maestría y avance configurados en `src/main/resources/application.yaml` son provisionales:
     - `diagnostic.mastery.difficulty-weights`: `{1: 1.0, 2: 1.5, 3: 2.0}`
     - `learning-path.initial-mastery-threshold`: `0.80`
     - `learning-path.short-evaluation.approval-threshold`: `70%`
     - `learning-path.short-evaluation.consecutive-failures-before-recalculation`: `2`


3. **Seguridad y Transición a Producción:**
   - La autenticación JWT y la protección stateless ya están implementadas. El slice 4 añadirá el fallback temporal `X-Student-Id` para dev/test y los tests históricos; después el frontend deberá enviar `Authorization: Bearer <token>`.

---

## Requisitos

- **Java 17 LTS** (toolchain configurado en `build.gradle.kts`)
- **Docker** en ejecución para pruebas de integración con Testcontainers

---

## Configurar Supabase (desarrollo)

El perfil `dev` es el predeterminado y se conecta a PostgreSQL mediante las variables `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`. Usa los valores de conexión de Supabase; la URL JDBC debe incluir `sslmode=require`.

Spring Boot no carga `.env` automáticamente. Define las tres variables en el entorno desde el que inicias Gradle o configúralas en el Run Configuration del IDE. En PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://<host>:<puerto>/postgres?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME = "<usuario de Supabase>"
$env:SPRING_DATASOURCE_PASSWORD = "<contraseña de Supabase>"
.\gradlew.bat bootRun
```

Si guardas esos valores en `Cognia/.env`, cópialos al entorno antes de ejecutar la aplicación. Nunca guardes credenciales reales en Git. La aplicación no carga ni modifica el schema ni ejecuta seeds al iniciar.

---

## Ejecución (Run)

```bash
./gradlew bootRun
```

En Windows PowerShell:
```powershell
.\gradlew.bat bootRun
```

La API queda disponible en `http://localhost:8080`.

### CORS y documentación OpenAPI

CORS está habilitado para las rutas `/api/**` desde los orígenes locales `http://localhost:5173`, `http://localhost:8443` y `http://localhost:3000`. Se permiten los métodos `GET`, `POST`, `PUT`, `PATCH`, `DELETE` y `OPTIONS`, con credenciales.

La especificación OpenAPI se publica en `http://localhost:8080/v3/api-docs` y Swagger UI en `http://localhost:8080/swagger-ui.html`. Swagger presenta el esquema Bearer JWT y el botón **Authorize**; el backend ya valida esos tokens y protege las rutas privadas.

### Registro y autenticación

- `POST /api/v1/auth/register` recibe `email`, `password` y `fullName` y crea una cuenta de estudiante.
- `POST /api/v1/auth/login` recibe `email` y `password`; devuelve `token`, `expiresIn` (segundos), `studentId` y `fullName`.
- `GET /api/v1/auth/me` requiere `Authorization: Bearer <token>`.

El token dura 24 horas. Define `COGNIA_JWT_SECRET` como una clave codificada en Base64 de al menos 32 bytes para mantener una clave estable entre reinicios y réplicas. Si se omite, el backend genera una clave aleatoria al arrancar, apropiada solo para desarrollo local; los tokens emitidos dejarán de ser válidos al reiniciar.

Los endpoints de las fases de aprendizaje ahora requieren Bearer JWT. La compatibilidad temporal con `X-Student-Id` en dev/test se añadirá en el Slice 4, por lo que el frontend que aún usa esa cabecera necesita ese slice antes de continuar el flujo.

### Endpoints Principales por Fase

- **Health check:**
  ```bash
  curl http://localhost:8080/actuator/health
  ```

- **Fase 1 (Currículo y Prerrequisitos):**
  ```bash
  curl http://localhost:8080/api/v1/subjects
  curl http://localhost:8080/api/v1/subjects/10000000-0000-0000-0000-000000000001
  curl http://localhost:8080/api/v1/subjects/10000000-0000-0000-0000-000000000001/concepts
  curl http://localhost:8080/api/v1/concepts/20000000-0000-0000-0000-000000000002/prerequisites
  ```

- **Fase 2 (Práctica Formativa):**
  ```bash
  curl http://localhost:8080/api/v1/concepts/20000000-0000-0000-0000-000000000001/practice-activities
  ```
  Permite filtro opcional `?difficulty=1..3` y registro de intentos en `POST /api/v1/activities/{activityId}/practice-attempts`. El endpoint requiere autenticación; los ejemplos de Postman con `X-Student-Id` vuelven a funcionar al completar el fallback dev/test del Slice 4. Detalles en [docs/fase-2-postman.md](docs/fase-2-postman.md).

- **Fase 3 (Diagnóstico y Perfil de Maestría):**
  Diagnóstico inicial por asignatura y consulta de perfil. Respuestas atómicas sin revelar retroalimentación hasta el cierre. Endpoints, ejemplos y errores en [docs/fase-3-postman.md](docs/fase-3-postman.md).

- **Fase 4 (Ruta Adaptativa y Evaluación Corta):**
  Generación de ruta: `POST /api/v1/subjects/{subjectId}/learning-path`.  
  Consulta de ruta activa y siguiente concepto: `GET .../learning-path` y `GET .../learning-path/next`.  
  Ciclo de evaluación corta de 3 ejercicios: `POST .../short-evaluations`, `POST .../answers/{activityId}` y `POST .../finish`. Documentación y ejemplos en [docs/fase-4-postman.md](docs/fase-4-postman.md).

---

## Pruebas y Compilación (Test and Build)

```bash
./gradlew test
./gradlew build
```

Los tests de persistencia usan PostgreSQL 16 mediante Testcontainers y requieren Docker. Se ejecutan con el perfil `test` y nunca tocan la base remota.

### Pruebas específicas de Fase 4

Desde la carpeta `Cognia`, en PowerShell:

```powershell
.\gradlew.bat test --tests "com.cognia.api.domain.service.LearningPathGeneratorTest"
.\gradlew.bat test --tests "com.cognia.api.domain.service.IncrementalMasteryTest"
.\gradlew.bat test --tests "com.cognia.api.application.service.LearningPathServiceTest"
```

Estas clases cubren actualmente las reglas de dominio, la combinación incremental de dominio y el caso de recálculo sin cambios. Aún no existen pruebas de Fase 4 para persistencia con Testcontainers ni para el controller con MockMvc; su cobertura pendiente y los casos que deben incluir están descritos en [docs/fase-4-postman.md](docs/fase-4-postman.md). Las pruebas de persistencia usan un PostgreSQL local efímero en Docker, no Supabase.

---

## Arquitectura

El backend sigue los principios de Arquitectura Hexagonal (Puertos y Adaptadores) en una estructura de monolito modular:

```text
com.cognia.api
├── domain/model                 # Modelos de dominio puros (invariantes, sin dependencias)
├── domain/service               # Servicios de dominio puro (grafos, políticas de maestría)
├── application/port/in          # Contratos de casos de uso (driving ports)
├── application/port/out         # Contratos de repositorios y servicios externos (driven ports)
├── application/service          # Orquestación de casos de uso
└── adapter
    ├── in/web                   # Controladores REST, DTOs y manejo de errores (ProblemDetail)
    └── out/persistence          # Entidades JPA, repositorios Spring Data y mappers
```

Flujo estándar de ejecución:
`REST Controller -> Caso de Uso (Port In) -> Servicio de Aplicación -> Repositorio (Port Out) -> Adaptador JPA -> Base de Datos PostgreSQL`.

---

## Limitaciones Actuales y Próximos Pasos

- **Persistencia:** `ddl-auto=none` y `spring.sql.init.mode=never`; la aplicación no altera el esquema automáticamente.
- **Identificadores:** Rutas y entidades utilizan UUIDs; las columnas y tablas se mantienen en español en la base de datos y se traducen en el adaptador JPA.
- **Seeds:** Los archivos SQL en `src/main/resources/db/` son material provisional para revisión docente y pruebas.
- **Pendientes principales:**

  - Implementación de la **Fase 6** (Spring Security / JWT, OpenAPI, Frontend en React + TypeScript y contenedor Docker / CI).


## Fase 5: repasos SM-2 y examen global

Cola diaria: GET /api/v1/subjects/{subjectId}/reviews/due. Sesiones: /api/v1/subjects/{subjectId}/review-sessions y /api/v1/review-sessions/{sessionId}. Examen: /api/v1/subjects/{subjectId}/global-exams y /api/v1/global-exams/{examId}. Al aprobar la evaluación corta, las tarjetas activas se añaden al mazo dentro de la misma transacción. Para examen, la ruta de mayor versión debe estar COMPLETADA. La guía del flujo y los UUID provisionales están en [docs/fase-5-postman.md](docs/fase-5-postman.md).
