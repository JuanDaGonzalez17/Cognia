# Fase 3: diagnóstico y perfil de conocimiento

El banco de diagnóstico es provisional y requiere validación docente. El perfil separa evidencia (respuestas) del dominio estimado. La estimación usa pesos provisionales configurados en `application.yaml`: dificultad 1 = 1.0, dificultad 2 = 1.5 y dificultad 3 = 2.0.

Aplica manualmente y en orden `seed-calculo-integral.sql`, `seed-actividades.sql` y `seed-diagnostico.sql`. La app no ejecuta schema ni seeds. Usa el estudiante temporal `30000000-0000-0000-0000-000000000001` y asignatura `10000000-0000-0000-0000-000000000001`.

## Iniciar

`POST /api/v1/subjects/10000000-0000-0000-0000-000000000001/diagnostics`

Header: `X-Student-Id: 30000000-0000-0000-0000-000000000001`.

La respuesta incluye sesión y actividades activas DIAGNOSTICO de la asignatura, sin claves correctas. UUID de actividades: `41000000-0000-0000-0000-000000000001` a `...0008`; opciones: `51000000-0000-0000-0000-000000000001` a `...0008`.

```json
{
  "session":{"id":"<session-uuid>","subjectId":"10000000-0000-0000-0000-000000000001","state":"EN_PROGRESO","score":null,"closedAt":null},
  "activities":[{"id":"41000000-0000-0000-0000-000000000001","conceptId":"20000000-0000-0000-0000-000000000001","code":"C01-D1","statement":"¿Cuál es una primitiva de 2x?","difficulty":1,"type":"SELECCION_MULTIPLE","options":[{"id":"51000000-0000-0000-0000-000000000001","text":"x²"},{"id":"51000000-0000-0000-0000-000000000002","text":"2x²"}]}]
}
```

## Responder

`POST /api/v1/diagnostics/<session-uuid>/answers/41000000-0000-0000-0000-000000000001`

```json
{"optionId":"51000000-0000-0000-0000-000000000001","seconds":8}
```

La respuesta confirma únicamente el registro, sin indicar acierto ni respuesta correcta: `{"id":"<answer-uuid>","activityId":"41000000-0000-0000-0000-000000000001","recorded":"Respuesta registrada"}`.

Para respuesta corta, la actividad `...0002` acepta `{ "answer": "x^3", "seconds": 12 }`.

## Finalizar y consultar perfil

`POST /api/v1/diagnostics/<session-uuid>/finish` completa la sesión, marca `diagnostico_completado` y devuelve el puntaje simple como porcentaje de respuestas correctas. La estimación por concepto sí pondera dificultad: `Σ(peso × aciertos) / Σ(peso)`. No hay dominio para conceptos sin respuestas.

`GET /api/v1/students/30000000-0000-0000-0000-000000000001/subjects/10000000-0000-0000-0000-000000000001/knowledge-profile`

Header `X-Student-Id` requerido. Cada concepto reporta `mastery`, `totalEvaluations` y `totalCorrect`; un perfil sin evidencias contiene `concepts: []`.

## Errores

| Estado | Caso |
|---|---|
| 400 | Falta `X-Student-Id` o UUID inválido |
| 404 | Estudiante, asignatura, actividad, sesión o perfil inexistente |
| 422 | Petición/respuesta inválida; finalizar sin respuestas |
| 409 | Sesión no está en progreso, actividad repetida, sesión de otro estudiante o diagnóstico ya completado |

Una sesión en curso se devuelve al volver a iniciar. En esta fase, un diagnóstico ya completado no puede iniciarse de nuevo.
