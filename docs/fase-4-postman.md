# Fase 4: ruta y evaluación corta (provisional)

El banco de evaluación, sus claves y los umbrales son provisionales y deben validarse con un docente. Aplicar manualmente `seed-evaluacion-corta.sql` después de los seeds de currículo y diagnóstico. En la base ya existente, agregar la restricción indicada al final de esta guía.

La combinación incremental propuesta es `nuevo = (dominioAnterior × evaluacionesPrevias + aciertosPonderadosNuevos) / (evaluacionesPrevias + pesoTotalNuevo)`. Ejemplo: dominio `0.60`, cinco evidencias previas y tres aciertos nuevos (dos de dificultad 3 y uno de dificultad 2): `(0.60×5 + 2×2.0 + 1×1.5) / (5 + 2×2.0 + 1×1.5) = 0.8095`. `total_evaluaciones` y `total_aciertos` siguen contando respuestas; como el schema no almacena peso histórico, las evidencias previas se resumen por su promedio y conteo, una aproximación simple para esta fase. Umbral de aprobación (70) decide el estado de ruta; no sustituye el umbral inicial de dominio (0.80). El paso aprobado se preserva DOMINADO al recalcular, aunque el dominio estimado sea menor.

La ruta topológica respeta el grafo y desempata por `orden_sugerido`. Aun con dominio sobre 0.80, el paso queda BLOQUEADO si falta dominar un prerrequisito. Se conserva el dominio del perfil, pero no se recomienda ni se salta el concepto: al aprobar los prerrequisitos, el paso pasa a DOMINADO si conserva evidencia sobre umbral; si no, DISPONIBLE.

## Flujo esperado

1. Completar diagnóstico según [Fase 3](fase-3-postman.md).
2. Generar ruta: `POST /api/v1/subjects/{subjectId}/learning-path`, con `X-Student-Id` requerido. Ejemplo para la asignatura de seed: `POST /api/v1/subjects/10000000-0000-0000-0000-000000000001/learning-path`.
3. Consultar `GET /api/v1/subjects/{subjectId}/learning-path` y el siguiente paso en `GET /api/v1/subjects/{subjectId}/learning-path/next`.
4. Iniciar evaluación: `POST /api/v1/subjects/{subjectId}/path-steps/{stepId}/short-evaluations`; devuelve sesión y actividades activas del concepto, sin claves. Si ya hay una sesión EN_PROGRESO para ese paso, devuelve esa misma sesión. Se rechaza un paso BLOQUEADO; también se puede evaluar uno que inició DOMINADO por su perfil. Ese paso pasa a EN_PROGRESO y el resultado lo deja DOMINADO si aprueba o REFUERZO si falla.
5. Responder: `POST /api/v1/short-evaluations/{sessionId}/answers/{activityId}`, body `{"optionId":"52000000-0000-0000-0000-000000000001","seconds":20}` (para respuesta corta, `{"answer":"2x²","seconds":20}`). La respuesta solo confirma registro. Finalizar con `POST /api/v1/short-evaluations/{sessionId}/finish`; devuelve puntaje, aprobación, estado nuevo del paso y si se recalculó, sin detalle por pregunta.
6. Aprobar desbloquea dependientes. Fallar deja REFUERZO; recomienda práctica a dificultad igual o menor que la máxima dificultad evaluada.
7. Dos fallos consecutivos en sesiones del paso de la ruta actual disparan recálculo. La ruta anterior queda RECALCULADA antes de crear nueva versión. Los pasos DOMINADO por evaluación se preservan; si el nuevo resultado no cambia ruta ni estados, no se crea versión. La nueva ruta hace que el contador por paso vuelva a cero.

Ejemplo de cierre fallido: `{"sessionId":"<uuid>","score":50.0,"approved":false,"stepState":"REFUERZO","recalculated":false}` en el primer fallo y `recalculated:true` al alcanzar el umbral de fallos. Generar otra ruta mientras existe una ACTIVA devuelve 409; consulta la ruta existente o espera a su finalización/recálculo.

UUID provisionales: asignatura `10000000-0000-0000-0000-000000000001`, estudiante temporal `30000000-0000-0000-0000-000000000001`; conceptos `20000000-0000-0000-0000-000000000001` a `...0004`; actividades evaluación `42000000-0000-0000-0000-000000000001` a `...0008`; opciones `52000000-0000-0000-0000-000000000001` a `...0008`.

## Respuestas de ejemplo

Inicio: `{"session":{"id":"<uuid>","conceptId":"20000000-0000-0000-0000-000000000001","pathStepId":"<step-uuid>","attempt":1,"state":"EN_PROGRESO"},"activities":[{"id":"42000000-0000-0000-0000-000000000001","conceptId":"20000000-0000-0000-0000-000000000001","code":"C01-EC1","statement":"¿Cuál es una primitiva de 4x?","difficulty":1,"type":"SELECCION_MULTIPLE","options":[{"id":"52000000-0000-0000-0000-000000000001","text":"2x²"},{"id":"52000000-0000-0000-0000-000000000002","text":"4x²"}]}]}`.

Respuesta: `{"id":"<answer-uuid>","activityId":"42000000-0000-0000-0000-000000000001","recorded":"Respuesta registrada"}`. Cierre: `{"sessionId":"<uuid>","score":100.0,"approved":true,"stepState":"DOMINADO","recalculated":false}`.

Errores: 404 para recursos inexistentes, 422 para datos o actividades no válidas, 409 para diagnóstico pendiente, ruta ya activa, paso bloqueado o sesión fuera de estado. Ninguna respuesta incluye la corrección.

## Cambio de schema para la base existente

`schema.sql` contiene la regla equivalente. Aplicar manualmente una vez:

```sql
ALTER TABLE sesion_evaluacion
ADD CONSTRAINT ck_sesion_evaluacion_paso_requerido
CHECK (tipo_evaluacion <> 'EVALUACION_CORTA' OR paso_ruta_id IS NOT NULL);
```

No se usan migraciones ni conexión de tests a Supabase.
