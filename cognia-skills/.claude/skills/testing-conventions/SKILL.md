---
name: testing-conventions
description: Usar al escribir o ejecutar tests en Cognia API (dominio, servicios, controllers). Convenciones de JUnit 5, fakes de puertos y MockMvc en Spring Boot 4.
---

# Convenciones de tests

Estructura: los tests espejan el paquete de main bajo `src/test/java/com/cognia/api/...`.

## Qué test escribir según la capa

| Capa | Tipo de test | Spring |
|---|---|---|
| `domain/model` | JUnit 5 puro: invariantes válidas e inválidas | No |
| `application/service` | JUnit 5 con fake del puerto `out` (clase anónima o clase de test) | No |
| `adapter/in/web` | `@SpringBootTest` + `@AutoConfigureMockMvc` + `MockMvc` | Sí |
| `adapter/out/persistence` | Cuando exista BD: Testcontainers (PostgreSQL) | Sí |

## Patrones del repo

- Dominio: `assertThrows(IllegalArgumentException.class, () -> new Concept(...))` para el caso inválido, y un test de valores válidos.
- Servicio: `var service = new GetSubjectsService(fakeRepository);` sin mocks de framework; basta un fake que implemente la interfaz.
- Controller: `mockMvc.perform(get("/api/v1/..."))` con `status()` y `jsonPath(...)`. Cubre siempre el caso 200 y el 404.
- Nombres de método descriptivos en inglés: `rejectsDifficultyOutsideSupportedRange`.

## Imports de Spring Boot 4 (error frecuente)

- `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
- `org.springframework.boot.test.context.SpringBootTest`

No uses el paquete antiguo `org.springframework.boot.test.autoconfigure.web.servlet`.

## Cómo ejecutar (ahorra tiempo y tokens)

- Un test: `./gradlew test --tests "com.cognia.api.domain.model.ConceptTest"`
- Un método: `./gradlew test --tests "*ConceptTest.keepsValidConceptValues"`
- Suite completa solo antes de cerrar la tarea: `./gradlew test`
- Si falla, lee solo el mensaje del test fallido, no todo el log.
- Nunca afirmes que los tests pasaron si no los ejecutaste.

## Reglas

- Toda lógica nueva lleva test en el mismo cambio.
- La lógica de dominio y de aplicación debe poder probarse sin levantar servidor ni BD.
- Sin datos personales reales en fixtures.
