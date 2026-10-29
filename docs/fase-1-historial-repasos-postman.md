# Fase 1: historial y próximos repasos

Los dos endpoints requieren autenticación. En integración normal envía `Authorization: Bearer <JWT>`. En perfil `dev`/`test` también está habilitado el mecanismo local `X-Student-Id`.

## Historial de actividad

```http
GET /api/v1/subjects/10000000-0000-0000-0000-000000000001/history
Authorization: Bearer <JWT>
```

Respuesta: hasta 50 elementos, más recientes primero. Las evaluaciones incluyen diagnóstico, evaluación corta y examen global; también se incluyen repasos finalizados.

```json
[
  {
    "id": "4c0c2ed6-3c5d-4a95-bc83-c5d8a3c5d774",
    "type": "EVALUACION_CORTA",
    "conceptId": "20000000-0000-0000-0000-000000000001",
    "conceptName": "Primitivas",
    "score": 85.0,
    "passed": true,
    "date": "2026-10-28T15:20:00Z",
    "attempt": 1
  },
  {
    "id": "ce45c2f0-c3bc-44db-b5c2-dcde265eb701",
    "type": "REPASO",
    "conceptId": null,
    "conceptName": "Repaso",
    "score": 75.0,
    "passed": null,
    "date": "2026-10-27T13:00:00Z",
    "attempt": null
  }
]
```

## Próximos repasos

```http
GET /api/v1/subjects/10000000-0000-0000-0000-000000000001/reviews/upcoming
Authorization: Bearer <JWT>
```

Devuelve hasta 100 tarjetas activas del estudiante con fecha futura, ordenadas por fecha ascendente.

```json
[
  {
    "progressId": "2c004857-59db-46a8-a0d9-a2b02cc2b975",
    "conceptId": "20000000-0000-0000-0000-000000000001",
    "conceptName": "Primitivas",
    "nextReviewAt": "2026-10-30T15:20:00Z",
    "intervalDays": 2,
    "consecutiveLapses": 0
  }
]
```

Ambos endpoints responden `[]` si no hay resultados y `404` si la asignatura no existe. Los resultados siempre quedan limitados al estudiante autenticado.
