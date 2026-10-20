# Plan de implementación - Cognia API

**Fecha de actualización:** 20 de Octubre, 2026 · @Alejandro  
**Estado General de Ejecución:** **~71.4% del Plan Global (5 de 7 Fases completadas)** / **83.3% del Backend Core API (5 de 6 Fases completadas)**

---

## 📊 Tablero de Control y Estado de Ejecución

| Fase | Título | Estado | % Avance | Hito / Criterio de Cierre |
| :--- | :--- | :---: | :---: | :--- |
| **Fase 0** | Preparación y Base | ✅ **Completada** | 100% | Configuración base, PostgreSQL/Supabase, Testcontainers, Schema unificado. |
| **Fase 1** | Currículo y Prerrequisitos | ✅ **Completada** | 100% | CRUD asignaturas/conceptos, validación acíclica de grafo y seed Cálculo Integral. |
| **Fase 2** | Actividades y Práctica | ✅ **Completada** | 100% | Actividades formativas, SM y RC, retroalimentación inmediata, intentos registrados. |
| **Fase 3** | Diagnóstico y Dominio | ✅ **Completada** | 100% | Sesiones diagnósticas, cálculo de maestría inicial y auditoría en historial. |
| **Fase 4** | Ruta y Evaluación Corta | ✅ **Completada** | 100% | Rutas topológicas, evaluación sumativa (3 ítems), avance/refuerzo y recálculo versionado. |
| **Fase 5** | Repasos SM-2 y Examen Global | ⏳ **Pendiente** | 0% | Motor SM-2 (Anki), cola diaria, amnesia conceptual (>=2 fallos) y examen global. |
| **Fase 6** | Transversales y Frontend | 🟡 **Parcial** | ~20% | `ProblemDetail` y validaciones listas. Pendiente: Spring Security/JWT, OpenAPI, React y CI. |

---

## Principio: slices verticales, no capas horizontales

Construyan por funcionalidad completa, de la base de datos al endpoint, y no la capa de dominio entera primero y los adaptadores después. Así cada fase termina con algo que corre y se prueba, y los errores de diseño aparecen en la primera slice y no en la última.

Dentro de cada slice el orden es siempre el mismo (es el skill `hexagonal-slice`):

1. **Dominio:** records con invariantes y reglas puras (política de dominio, validación de ciclos). Test JUnit sin Spring.
2. **Puertos:** interfaces de entrada (casos de uso, lado driving) y de salida (repositorios, lado driven). Solo contratos.
3. **Servicio de aplicación:** implementa el caso de uso. Test con fakes de los puertos de salida.
4. **Adaptador driven (persistencia):** cambios al schema si hacen falta, entidad JPA, mapper y repositorio. Test con Testcontainers.
5. **Adaptador driving (REST):** controller, DTOs y errores. Test con MockMvc.
6. **Cierre:** prueba de punta a punta de la slice y actualización del `README.md`.

Mientras no exista el paso 4, un adaptador en memoria permite probar el controller. La slice siguiente no empieza hasta que la actual tenga tests en verde y esté integrada.

---

## Detalle de Fases

### Fase 0 · Preparación ✅ `[COMPLETADA - 100%]`

Deja el repo listo para que el agente trabaje con contexto y sin sorpresas. Termina cuando la app arranca contra PostgreSQL/Supabase y `./gradlew test` pasa con Testcontainers.

- **Entregables implementados:**
  - `build.gradle.kts` configurado con Spring Boot 4.1.1, Java 17 LTS, Spring Data JPA, WebMVC, Validation, Actuator y Testcontainers PostgreSQL 2.0.5.
  - Perfiles `dev` (conectado a Supabase mediante variables de entorno) y `test` (con Testcontainers).
  - DDL canónico centralizado en `src/main/resources/db/migration/schema.sql` con `ddl-auto: none` y `spring.sql.init.mode: never`.
  - Configuración de variables de entorno seguras vía `.env` (excluido en `.gitignore`).

---

### Fase 1 · Currículo ✅ `[COMPLETADA - 100%]`

Primera slice vertical: persistencia real de asignaturas y conceptos, más el grafo dirigido acíclico de prerrequisitos.

- **Entregables implementados:**
  - **Dominio:** `Subject`, `Concept` (id UUID, `codigo`, `nivel`, orden) y `PrerequisiteGraphValidator` (rechazo estricto de ciclos, autoenlaces y aristas inválidas).
  - **Puertos:** `GetSubjectsUseCase`, `GetSubjectConceptsUseCase`, `GetConceptPrerequisitesUseCase`, `AddPrerequisiteUseCase`, `SubjectRepository`, `ConceptRepository`, `PrerequisiteRepository`.
  - **Persistencia (Driven):** Entidades JPA (`SubjectJpaEntity`, `ConceptJpaEntity`, `PrerequisiteJpaEntity`), mappers y adaptadores. Seed de Cálculo Integral (`seed-calculo-integral.sql`).
  - **REST (Driving):** `SubjectController`, `ConceptController`, `PrerequisiteController` con respuestas estandarizadas y gestión de errores RFC 7807 (`ProblemDetail`).
  - **Tests:** `CurriculumServiceTest`, `SubjectControllerTest`, `PrerequisiteGraphValidatorTest`, `ConceptTest`, `CurriculumPersistenceTest`.

---

### Fase 2 · Actividades y práctica ✅ `[COMPLETADA - 100%]`

Permite al estudiante practicar un concepto y recibir retroalimentación formativa inmediata sin alterar directamente su nota formal de avance.

- **Entregables implementados:**
  - **Dominio:** `Activity` con dificultad (1 a 3), uso (`PRACTICA`) y tipo (`SELECCION_MULTIPLE`, `RESPUESTA_CORTA`). `ActivityCorrectionService` para validación canónica de respuestas y retroalimentación específica sin filtrar claves.
  - **Puertos:** `GetPracticeActivitiesUseCase`, `AnswerPracticeActivityUseCase`, `ActivityRepository`, `PracticeAttemptRepository`.
  - **Persistencia (Driven):** Entidades JPA (`ActivityJpaEntity`, `ActivityOptionJpaEntity`, `ShortAnswerCriterionJpaEntity`, `PracticeAttemptJpaEntity`). Seed formativo (`seed-actividades.sql`).
  - **REST (Driving):** `GET /api/v1/concepts/{conceptId}/practice-activities`, `POST /api/v1/activities/{activityId}/practice-attempts`. Manejo temporal de estudiante mediante cabecera `X-Student-Id`.
  - **Documentación y pruebas:** [docs/fase-2-postman.md](docs/fase-2-postman.md), `PracticeServiceTest`, `PracticeControllerTest`, `PracticePersistenceTest`, `ActivityCorrectionServiceTest`.

---

### Fase 3 · Estudiante, diagnóstico y dominio ✅ `[COMPLETADA - 100%]`

Incorpora la adaptabilidad inicial: evalúa conocimientos previos (niveles 0 y 1) y estima el dominio conceptual inicial.

- **Entregables implementados:**
  - **Dominio:** `KnowledgeProfile`, `ConceptMastery` (0.0 a 1.0), `DiagnosticSession`, `DiagnosticAnswer`, `DiagnosticEvidence`. Interfaz `MasteryPolicy` con `RuleBasedMasteryPolicy` parametrizada por pesos configurables (dificultades 1: 1.0, 2: 1.5, 3: 2.0).
  - **Puertos:** `DiagnosticUseCases`, `DiagnosticRepository`, `StudentExistencePort`.
  - **Persistencia (Driven):** Entidades JPA (`KnowledgeProfileJpaEntity`, `ConceptMasteryJpaEntity`, `MasteryHistoryJpaEntity`, `DiagnosticSessionJpaEntity`, `DiagnosticAnswerJpaEntity`, `DiagnosticJpaRepositoryAdapter`). Trazabilidad con `origen_cambio = 'DIAGNOSTICO'`.
  - **REST (Driving):** `DiagnosticController` con endpoints para iniciar sesión, enviar respuestas atómicas, finalizar diagnóstico y consultar perfil de conocimiento.
  - **Documentación y pruebas:** [docs/fase-3-postman.md](docs/fase-3-postman.md), `seed-diagnostico.sql`, `DiagnosticServiceTest`, `DiagnosticControllerTest`, `RuleBasedMasteryPolicyTest`, `IncrementalMasteryTest`.

---

### Fase 4 · Ruta y evaluación corta ✅ `[COMPLETADA - 100%]`

Cierra el ciclo adaptativo principal: genera la ruta basada en el diagnóstico y administra evaluaciones cortas sumativas para decidir avance a `DOMINADO` o retroceso a `REFUERZO`, recalculando la ruta ante fallos reiterados.

- **Entregables implementados:**
  - **Dominio:** `LearningPath`, `PathStep` con estados (`BLOQUEADO`, `DISPONIBLE`, `EN_PROGRESO`, `REFUERZO`, `DOMINADO`). `LearningPathGenerator` con ordenación topológica y preservación de prerrequisitos. `ShortEvaluation` y `EvaluationAnswer`. Combinación incremental de evidencia y recálculo con versionado (`version` autoincremental, ruta previa en `RECALCULADA`).
  - **Puertos:** `LearningPathUseCases`, `LearningPathRepository`, `ShortEvaluationRepository`.
  - **Persistencia (Driven):** Entidades JPA (`LearningPathJpaEntity`, `PathStepJpaEntity`, `LearningPathJpaRepositoryAdapter`, `ShortEvaluationJpaRepositoryAdapter`).
  - **REST (Driving):** `LearningPathController` (`POST .../learning-path`, `GET .../learning-path`, `GET .../learning-path/next`, `POST .../short-evaluations`, `POST .../answers/{activityId}`, `POST .../finish`).
  - **Documentación y pruebas:** [docs/fase-4-postman.md](docs/fase-4-postman.md), `seed-evaluacion-corta.sql`, `LearningPathServiceTest`, `LearningPathGeneratorTest`.

---

### Fase 5 · Repasos y examen global ⏳ `[PENDIENTE - 0%]`

Incorpora la retención a largo plazo inspirada en Anki (algoritmo SM-2) y la evaluación sumativa de cierre de la asignatura.

- **Alcance a construir:**
  - **Dominio:** Algoritmo SM-2 como servicio puro independiente de frameworks (cálculo de nuevo Intervalo, Factor de Facilidad $\ge 1.30$, Repeticiones y clasificación de calificación 0 a 5). Detección de "amnesia conceptual" ($\ge 2$ fallos consecutivos) que dispara el recálculo adaptativo de la ruta a estado `REFUERZO`. Generación y selección de preguntas para el Examen Global ($X$).
  - **Puertos:** Casos de uso para obtener cola de repaso del día (`fecha_proximo_repaso <= now()`), registrar calificación de tarjeta SM-2, cerrar sesión de repaso; iniciar, responder y finalizar examen global. Repositorios de tarjetas, progreso de repaso y sesiones de repaso.
  - **Driven (Persistencia):** Entidades JPA y repositorios para las tablas ya presentes en `schema.sql`: `tarjeta_repaso`, `progreso_repaso`, `sesion_repaso` y `detalle_respuesta_repaso`. Seed con tarjetas de repaso para los conceptos de Cálculo Integral.
  - **Driving (REST):** Endpoints para la cola diaria de repaso, envío de calificación SM-2 y ciclo de vida del examen global.

---

### Fase 6 · Transversales y Frontend 🟡 `[PARCIAL - ~20%]`

Aspectos no funcionales, seguridad, documentación interactiva y cliente web.

- **Completado:**
  - Manejo uniforme de errores RFC 7807 (`ProblemDetail`) y captura global en `ApiExceptionHandler`.
  - Validaciones de entrada en DTOs con Bean Validation.
- **Pendiente:**
  - **Autenticación y autorización:** Migrar del header provisional `X-Student-Id` a autenticación real con Spring Security (JWT / OAuth2 / Sesión).
  - **Documentación de API:** Integración de `springdoc-openapi` para Swagger UI interactivo.
  - **Frontend:** Aplicación SPA en React + TypeScript (visualización del grafo DAG con React Flow, renderizado matemático KaTeX, ejecutor de flashcards Anki y flujo de evaluación).
  - **Despliegue y CI/CD:** Dockerfile multi-etapa, pipeline de GitHub Actions para ejecución de tests y despliegue del servicio.

---

## ⚠️ Consideraciones Críticas a Tomar en Cuenta

### 1. Gestión de Base de Datos y Schema
- **Inexistencia de migraciones automáticas:** El proyecto no utiliza Flyway ni Liquibase en tiempo de ejecución (`spring.sql.init.mode: never`, `ddl-auto: none`). Toda alteración de base de datos se debe aplicar manualmente en Supabase/PostgreSQL.
- **Validación del DDL existente:** Aunque las tablas de repaso (`tarjeta_repaso`, `progreso_repaso`, `sesion_repaso`, `detalle_respuesta_repaso`) ya están en el script local `schema.sql`, se debe verificar que la base remota de desarrollo (Supabase) las tenga creadas antes de iniciar la Fase 5.
- **Constraint pendiente de Fase 4:** Confirmar que en la base remota se haya ejecutado la restricción:
  ```sql
  ALTER TABLE sesion_evaluacion
  ADD CONSTRAINT ck_sesion_evaluacion_paso_requerido
  CHECK (tipo_evaluacion <> 'EVALUACION_CORTA' OR paso_ruta_id IS NOT NULL);
  ```

### 2. Decisiones Pedagógicas y Calibración de Umbrales (Docente)
- Los umbrales actuales configurados en `application.yaml` son provisionales:
  - `diagnostic.mastery.difficulty-weights`: `{1: 1.0, 2: 1.5, 3: 2.0}`
  - `learning-path.initial-mastery-threshold`: `0.80`
  - `learning-path.short-evaluation.approval-threshold`: `70%`
  - `learning-path.short-evaluation.consecutive-failures-before-recalculation`: `2`
- **Decisión Fase 5 (Impacto del repaso):** Se debe definir con el docente si una sesión de repaso SM-2 exitosa o fallida altera la métrica de `ConceptMastery` en `perfil_conocimiento` o si únicamente actualiza el agendamiento y el estado del paso de ruta.
- **Decisión Fase 5 (Amnesia conceptual):** Definir si el umbral de 2 fallos consecutivos se evalúa por tarjeta individual o por concepto agregado.

### 3. Seguridad y Deuda Técnica
- **Header `X-Student-Id`:** Es una solución provisional de desarrollo. En producción, la identidad debe resolverse desde el contexto de seguridad autenticado (`SecurityContextHolder`).
- **Banco de Semillas:** Los scripts de seed (`seed-calculo-integral.sql`, `seed-actividades.sql`, `seed-diagnostico.sql`, `seed-evaluacion-corta.sql`) contienen ítems prototipo de prueba. El banco oficial completo (312 actividades) debe consolidarse formalmente antes de una prueba con usuarios reales.
