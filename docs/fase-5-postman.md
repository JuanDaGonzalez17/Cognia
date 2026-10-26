# Fase 5: repasos SM-2 y examen global

Todos los umbrales, la demora de primera revisión, el tamaño de cola y el máximo por concepto son provisionales; un docente debe validarlos. Los tests usan PostgreSQL de Testcontainers. No ejecutes seeds de prueba contra Supabase.

## Preparación manual

El DDL existente ya contiene las tablas necesarias; no se necesita ALTER ni migración. En una base local de desarrollo, aplica manualmente y en este orden los seeds `seed-calculo-integral.sql`, `seed-actividades.sql`, `seed-diagnostico.sql`, `seed-evaluacion-corta.sql` y `seed-repaso-examen.sql`. El último seed es provisional. La asignatura temporal es `10000000-0000-0000-0000-000000000001`, el estudiante es `30000000-0000-0000-0000-000000000001` y los conceptos de ejemplo terminan en `0001` a `0004`.

Usa `X-Student-Id: 30000000-0000-0000-0000-000000000001` en las peticiones. En el perfil `dev`, `review.initial-delay-days=0`; el valor general es 1 día. Si cambiaste la configuración o necesitas hacer vencida una tarjeta concreta, adelanta su fecha en la base local:

```sql
UPDATE progreso_repaso
SET fecha_proximo_repaso = CURRENT_TIMESTAMP - INTERVAL '1 minute'
WHERE estudiante_id = '30000000-0000-0000-0000-000000000001'
  AND tarjeta_id = '70000000-0000-0000-0000-000000000001';
```

## Aprobar un concepto y recibir tarjetas

Completa el diagnóstico y genera la ruta siguiendo [Fase 4](fase-4-postman.md). Inicia la evaluación del paso disponible, responde correctamente y finalízala:

```http
POST /api/v1/subjects/10000000-0000-0000-0000-000000000001/path-steps/{stepId}/short-evaluations
X-Student-Id: 30000000-0000-0000-0000-000000000001

POST /api/v1/short-evaluations/{sessionId}/answers/42000000-0000-0000-0000-000000000001
{"optionId":"52000000-0000-0000-0000-000000000001","seconds":20}

POST /api/v1/short-evaluations/{sessionId}/finish
X-Student-Id: 30000000-0000-0000-0000-000000000001
```

Al aprobar, las tarjetas activas del concepto se agregan una vez al mazo del estudiante. Las tarjetas de seed incluyen `70000000-0000-0000-0000-000000000001` y `...0002` para el concepto `...0001`, `...0003` y `...0004` para `...0002`, `...0005` y `...0006` para `...0003`, y `...0007` y `...0008` para `...0004`.

Consulta la cola, que devuelve tanto anverso como reverso, y abre una sesión:

```http
GET /api/v1/subjects/10000000-0000-0000-0000-000000000001/reviews/due
X-Student-Id: 30000000-0000-0000-0000-000000000001

POST /api/v1/subjects/10000000-0000-0000-0000-000000000001/review-sessions
X-Student-Id: 30000000-0000-0000-0000-000000000001
```

Califica cada `progress.id` una sola vez. Una calificación de 4 o 5 representa recuerdo; 0, 1 o 2, olvido. La respuesta devuelve el intervalo y la nueva fecha:

```http
POST /api/v1/review-sessions/{reviewSessionId}/grades
X-Student-Id: 30000000-0000-0000-0000-000000000001
Content-Type: application/json

{"progressId":"{progressId}","grade":5,"seconds":12}
```

Para registrar olvido, repite con `grade: 1` y otra tarjeta vencida. Cierra la sesión con `POST /api/v1/review-sessions/{reviewSessionId}/finish`; revisadas, recordadas y olvidadas se calculan de las respuestas almacenadas.

## Forzar la reapertura después de dos fallos

El umbral provisional es dos fallos consecutivos **por tarjeta**. Repite dos veces una calificación menor a 3 sobre una tarjeta de un concepto que esté DOMINADO. Entre los dos fallos, adelanta la próxima fecha de esa tarjeta con el `UPDATE` de preparación y abre otra sesión de repaso. En el segundo fallo el concepto pasa a REFUERZO en una nueva versión de ruta; la ruta previa, incluso si estaba COMPLETADA, pasa a RECALCULADA. No se recalcula otra vez mientras el paso siga en REFUERZO. En el disparo, los fallos consecutivos de todas las tarjetas del concepto vuelven a cero y sus fallos acumulados se conservan.

Los pasos dependientes DOMINADO y EN_PROGRESO se conservan. Los que estaban DISPONIBLE o BLOQUEADO se recalculan respecto a los prerrequisitos: si dejan de estar todos DOMINADO, pasan a BLOQUEADO. Vuelve a aprobar el concepto reabierto mediante la evaluación corta para completar la versión vigente.

## Completar la ruta y presentar examen global

La semilla contiene dos actividades EXAMEN_GLOBAL por concepto, con UUID `71000000-0000-0000-0000-000000000001` a `...0008`; códigos C01-X1/X2, C11-X1/X2, C21-X1/X2 y C31-X1/X2. El orden es por orden sugerido del concepto y después por código. El máximo provisional es 2 por concepto.

Completa cada paso de la ruta vigente mediante el flujo de evaluación de Fase 4. Los datos de prueba para las actividades cortas son: C01 usa opción `520...0001` y respuesta `5x^3/3`; C11 usa `520...0003` y `-cos(x)`; C21 usa `520...0005` y `4`; C31 usa `520...0007` y `1`. Si ya aprobaste algún paso, no lo repitas. La ruta debe quedar COMPLETADA en su versión de mayor número.

Inicia el examen; una sesión EN_PROGRESO existente para esa asignatura se devuelve sin crear intento nuevo. Los DTOs de actividades no incluyen claves ni criterios de corrección:

```http
POST /api/v1/subjects/10000000-0000-0000-0000-000000000001/global-exams
X-Student-Id: 30000000-0000-0000-0000-000000000001
```

Responde una vez por actividad. Para selección múltiple envía `optionId`; para respuesta corta envía `text`. El endpoint solo confirma que se registró:

```http
POST /api/v1/global-exams/{examId}/answers/71000000-0000-0000-0000-000000000001
X-Student-Id: 30000000-0000-0000-0000-000000000001
Content-Type: application/json

{"optionId":"72000000-0000-0000-0000-000000000001","seconds":20}
```

Finaliza con `POST /api/v1/global-exams/{examId}/finish`. Devuelve puntaje 0–100, aprobación según el umbral provisional 70 y resumen de aciertos/total por concepto. Actualiza dominio e historial con origen EXAMEN_GLOBAL y referencia al examen, pero no cambia la ruta. Si la versión vigente no está COMPLETADA (por ejemplo, luego de reabrir un concepto), iniciar, responder o finalizar devuelve 409.

## Configuración provisional

`review.initial-delay-days=1`, `review.queue-limit=20`, `review.consecutive-lapses-before-recalculation=2`, `global-exam.max-per-concept=2` y `global-exam.passing-threshold=70`. Desarrollo sobrescribe `review.initial-delay-days=0` para probar sin esperar. Los valores deben validarse con un docente.
