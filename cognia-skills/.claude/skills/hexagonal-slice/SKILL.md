---
name: hexagonal-slice
description: Usar al agregar una funcionalidad, caso de uso, endpoint o entidad nueva en Cognia API. Receta paso a paso de la arquitectura hexagonal del repo.
---

# Agregar una slice vertical (hexagonal)

Paquete base: `com.cognia.api`. Usa `Subject`/`Concept` como referencia del patrón; léelos solo si dudas.

## Orden de trabajo

1. **Dominio** `domain/model/Xxx.java`
   - `record` con constructor compacto que valida invariantes (`requireText`, rangos, `List.copyOf`).
   - Cero imports de Spring, JPA o web.
2. **Puerto de entrada** `application/port/in/XxxUseCase.java`: interfaz con los métodos del caso de uso.
3. **Puerto de salida** `application/port/out/XxxRepository.java`: interfaz que usa solo tipos de dominio (`Optional`, `List`).
4. **Servicio** `application/service/XxxService.java`
   - `@Service`, implementa el `UseCase`, inyección por constructor.
   - Aquí vive la orquestación y las reglas; no en el controller.
5. **Adaptador de salida** `adapter/out/persistence/...`: implementa el puerto (`@Repository`). Si usa JPA, la entidad ORM va separada del dominio con mapper explícito.
6. **Controller** `adapter/in/web/XxxController.java`
   - `@RestController`, ruta `/api/v1/...`, depende solo del `UseCase`.
   - DTOs como `record` anidados con `static from(domain)`.
   - `Optional` vacío -> `ResponseEntity.notFound().build()`.
7. **Tests**: ver skill `testing-conventions`.

## Reglas

- Las dependencias apuntan hacia adentro: adapter -> application -> domain. Nunca al revés.
- Controllers sin lógica de negocio. Las entidades ORM nunca se exponen como dominio ni como respuesta.
- IDs como `String` (slug), como en el código actual.
- Estilo: tabs, records, `var` en tests, sin Lombok.
- No crear clases vacías para entidades futuras; solo lo que usa la slice actual.
- Configuración por variables de entorno; nunca secretos en el repo.
- Si cambia la arquitectura, los comandos o el setup, actualiza `README.md`.
- Al terminar, reporta: archivos creados, cómo correr, resultado real de los tests y qué quedó diferido.
