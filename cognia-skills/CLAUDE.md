# Cognia API

Plataforma de aprendizaje adaptativo. Backend Spring Boot 4 + Java 25 + Gradle (Kotlin DSL). Arquitectura hexagonal en monolito modular.

## Comandos

- Ejecutar: `./gradlew bootRun` (http://localhost:8080)
- Tests: `./gradlew test` (uno solo: `./gradlew test --tests "*NombreTest"`)
- Build: `./gradlew build`

## Estructura (`com.cognia.api`)

- `domain/model`: records de dominio sin frameworks
- `application/port/in` y `application/port/out`: casos de uso y puertos
- `application/service`: orquestación de casos de uso
- `adapter/in/web`: controllers REST y DTOs
- `adapter/out/persistence`: implementaciones de repositorios

## Reglas fijas

- Las dependencias apuntan hacia adentro. El dominio no importa Spring, JPA ni web.
- Los controllers no contienen lógica de negocio.
- Estilo: tabs, records, inyección por constructor, sin Lombok.
- Todo cambio con lógica lleva tests; nunca afirmes que pasaron sin ejecutarlos.
- Sin secretos en el repo; configuración por variables de entorno.
- Tablas y columnas se nombran en español; el dominio se modela en inglés y la traducción vive dentro del adaptador de persistencia.
- El schema se versiona como `schema.sql`, sin migraciones; los cambios se editan primero ahí.
- Actualiza `README.md` si cambian comandos, arquitectura o setup.
- Cambios pequeños: una slice por tarea.

## Skills del proyecto

- `hexagonal-slice`: al agregar funcionalidad nueva.
- `testing-conventions`: al escribir o correr tests.
- `adaptive-domain-rules`: al tocar lógica adaptativa (diagnóstico, dominio, rutas, repasos).

Contexto de producto completo: `PROJECT_CONTEXT.md` (leer solo si hace falta).
