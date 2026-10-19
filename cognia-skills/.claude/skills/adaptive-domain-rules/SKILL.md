---
name: adaptive-domain-rules
description: Usar al modelar o implementar lógica del dominio adaptativo de Cognia (diagnóstico, dominio conceptual, rutas, evaluaciones, repasos). Resume las reglas de producto sin leer PROJECT_CONTEXT.md completo.
---

# Reglas del dominio adaptativo

Resumen de `PROJECT_CONTEXT.md`. Si algo no está aquí y es crítico, consulta ese archivo solo en la sección necesaria.

## Flujo del producto

Diagnóstico -> perfil de conocimiento -> ruta personalizada -> práctica (con feedback) -> evaluación por concepto -> actualización de dominio y ruta -> repasos espaciados -> evaluación global.

## Distinciones que no se mezclan

- **Práctica vs. evaluación**: la práctica es formativa (feedback, sin nota formal). Solo la evaluación por concepto decide si el estudiante avanza.
- **Evidencia vs. dominio estimado**: un puntaje (82/100) es evidencia; el dominio (0.82) es la estimación del sistema, normalizada de 0.0 a 1.0.
- **Concepto vs. paso de ruta**: el concepto es contenido estático de la asignatura; el paso de ruta es estado del estudiante (pendiente, en curso, en refuerzo, completado). Dominio y estado del paso son cosas distintas.
- **Grafo vs. ruta**: los prerrequisitos son datos estáticos del currículo. La ruta es dinámica, puede ramificarse, recalcularse o revisitar un concepto. No la reduzcas a un entero `position`.
- **Aprendizaje vs. retención**: dominar un concepto una vez no garantiza retención. Los repasos se programan y registran aparte de la ruta inicial.

## Reglas de adaptación (MVP, umbrales configurables)

- El diagnóstico alimenta el perfil inicial y la ruta.
- Evaluación por concepto aprobada (umbral configurable): actualiza dominio y avanza.
- Evaluación fallida: refuerzo con actividades de dificultad similar o menor y nuevo intento.
- Repaso (recuerdo activo): si falla, se agenda otro intento; si falla de nuevo, se recalcula la ruta y se vuelve al concepto para refuerzo.
- Evaluación global: disponible al completar la ruta requerida.
- El anteproyecto incluye el motor basado en reglas y BKT experimental opcional. Los repasos espaciados y la evaluación global son alcance adicional del repo, no están en el anteproyecto.

## Reglas de implementación

- El algoritmo de dominio va detrás de una interfaz (por ejemplo `MasteryPolicy`) para poder reemplazarlo (reglas hoy, BKT después). Cálculo en domain/application, nunca en SQL ni triggers.
- Cada recomendación de actividad debe ser trazable: registrar por qué se eligió (dominio, dificultad, prerrequisito).
- Garantizar que ninguna recomendación viole prerrequisitos.
- Validar que el grafo de prerrequisitos no tenga ciclos y que concepto y prerrequisito pertenezcan a la misma asignatura.
- Versionar rutas: recalcular no borra historial.
- No asumir regresión lineal para agregar dificultad; no fijar umbrales sin validar.
- No codificar "Cálculo Integral" en el dominio; es solo seed data. El grafo de prerrequisitos debe validarlo un docente antes de considerarlo oficial.
- Implementar solo lo que pide la tarea actual; lo demás sigue diferido.
