# Fase 2: actividades y práctica

Guía para probar manualmente la API de práctica de Cognia. El banco de actividades y las respuestas son **provisionales** y requieren validación docente.

La práctica es formativa: registrar un intento no modifica dominio, rutas ni evaluaciones.

## 1. Preparar los datos

La aplicación no ejecuta el schema ni los seeds al arrancar. En el SQL Editor de tu base de desarrollo, aplica los scripts en este orden:

1. src/main/resources/db/seed-calculo-integral.sql
2. src/main/resources/db/seed-actividades.sql

El primero crea la asignatura y cuatro conceptos; el segundo agrega actividades, opciones, criterios y un estudiante temporal. No es necesario modificar schema.sql. Los scripts usan UUID fijos y ON CONFLICT (id) DO NOTHING; si ya existen filas con esos UUID, revisa que sean los datos de desarrollo esperados.

El estudiante temporal tiene el UUID 30000000-0000-0000-0000-000000000001. Su password_hash es texto ficticio, no es una contraseña ni permite autenticarse.

| Concepto | Actividades activas de práctica | Casos de filtro |
| --- | --- | --- |
| 20000000-0000-0000-0000-000000000001 · C0.1 | 40000000-0000-0000-0000-000000000001 (opción múltiple), ...0002 (respuesta corta) | ...0009 es EVALUACION_CORTA y no debe aparecer |
| 20000000-0000-0000-0000-000000000002 · C1.1 | 40000000-0000-0000-0000-000000000003 (opción múltiple), ...0004 (respuesta corta) | ...0010 es práctica inactiva y no debe aparecer |
| 20000000-0000-0000-0000-000000000003 · C2.1 | 40000000-0000-0000-0000-000000000005 (opción múltiple), ...0006 (respuesta corta) | — |
| 20000000-0000-0000-0000-000000000004 · C3.1 | 40000000-0000-0000-0000-000000000007 (opción múltiple), ...0008 (respuesta corta) | — |

Los UUID abreviados con ... en la tabla mantienen el prefijo 40000000-0000-0000-0000- y completan el sufijo indicado con doce dígitos; por ejemplo, ...0002 es 40000000-0000-0000-0000-000000000002.

## 2. Arrancar la API

Configura las variables de conexión de desarrollo como indica el README y arranca desde Cognia:

```powershell
.\gradlew.bat bootRun
```

La base debe ser la de desarrollo que contiene los seeds. No uses Neon para pruebas automatizadas; los tests de integración usan PostgreSQL con Testcontainers.

## 3. Consultar actividades de práctica

GET http://localhost:8080/api/v1/concepts/20000000-0000-0000-0000-000000000001/practice-activities

Filtro opcional de dificultad (solo 1, 2 o 3):

GET http://localhost:8080/api/v1/concepts/20000000-0000-0000-0000-000000000001/practice-activities?difficulty=1

Ejemplo con curl en PowerShell:

```powershell
curl.exe "http://localhost:8080/api/v1/concepts/20000000-0000-0000-0000-000000000001/practice-activities?difficulty=1"
```

La respuesta debe incluir la actividad ...0001 y no debe incluir es_correcta, correct, retroalimentacion_especifica, specificFeedback ni criterios de respuesta corta:

```json
[
  {
    "id": "40000000-0000-0000-0000-000000000001",
    "conceptId": "20000000-0000-0000-0000-000000000001",
    "code": "C01-P1",
    "statement": "¿Cuál es una primitiva de 2x?",
    "difficulty": 1,
    "type": "SELECCION_MULTIPLE",
    "options": [
      { "id": "50000000-0000-0000-0000-000000000001", "text": "x²" },
      { "id": "50000000-0000-0000-0000-000000000002", "text": "2x²" }
    ]
  }
]
```

Comprueba también el filtro de seguridad:

- El GET del concepto C0.1 no devuelve la actividad 40000000-0000-0000-0000-000000000009 (uso EVALUACION_CORTA).
- El GET del concepto C1.1 no devuelve la actividad 40000000-0000-0000-0000-000000000010 (práctica inactiva).
- ?difficulty=2 en C0.1 solo devuelve la actividad ...0002; ?difficulty=0 devuelve 422.

## 4. Responder una selección múltiple

Solicitud correcta:

```http
POST http://localhost:8080/api/v1/activities/40000000-0000-0000-0000-000000000001/practice-attempts
Content-Type: application/json
X-Student-Id: 30000000-0000-0000-0000-000000000001
```

```json
{ "optionId": "50000000-0000-0000-0000-000000000001", "seconds": 8 }
```

curl:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/activities/40000000-0000-0000-0000-000000000001/practice-attempts" -H "Content-Type: application/json" -H "X-Student-Id: 30000000-0000-0000-0000-000000000001" -d '{"optionId":"50000000-0000-0000-0000-000000000001","seconds":8}'
```

Esperado: 200 OK; la respuesta incluye el resultado y, después de responder, el UUID de la opción correcta:

```json
{
  "correct": true,
  "feedback": "Derivar la respuesta permite verificarla.",
  "specificFeedback": "La derivada de x² es 2x.",
  "correctOptionId": "50000000-0000-0000-0000-000000000001"
}
```

Para probar respuesta incorrecta, repite usando la opción 50000000-0000-0000-0000-000000000002. Se espera 200 OK y correct: false.

## 5. Responder una respuesta corta

Actividad C0.1 de respuesta corta:

```http
POST http://localhost:8080/api/v1/activities/40000000-0000-0000-0000-000000000002/practice-attempts
Content-Type: application/json
X-Student-Id: 30000000-0000-0000-0000-000000000001
```

```json
{ "answer": " X^3 ", "seconds": 12 }
```

Esperado: 200 OK y correct: true.

La petición incluye exactamente una de estas propiedades: optionId o answer. La respuesta corta se recorta, se convierte a minúsculas, se normaliza sin tildes y se limita a 255 caracteres. Los criterios exactos y regex también deben estar normalizados. El regex se compara contra la respuesta normalizada completa, no busca una coincidencia parcial. El seed actual contiene criterios exactos; la cobertura de regex está en los tests.

## 6. Comprobar errores

Los errores deben responder application/problem+json con un cuerpo ProblemDetail.

| Prueba | Cómo provocarla | Esperado |
| --- | --- | --- |
| Header ausente | POST sin X-Student-Id | 400 |
| Header UUID inválido | POST con X-Student-Id: no-es-uuid | 400 |
| Concepto inexistente | GET /api/v1/concepts/20000000-0000-0000-0000-000000000099/practice-activities | 404 |
| Actividad inexistente | POST a /api/v1/activities/40000000-0000-0000-0000-000000000099/practice-attempts | 404 |
| Estudiante inexistente | POST con X-Student-Id: 30000000-0000-0000-0000-000000000099 | 404 |
| Opción de otra actividad | Responder actividad ...0001 con opción ...0003 | 422 |
| Respuesta corta vacía | Responder actividad ...0002 con { "answer": "  " } | 422 |
| Actividad de evaluación | POST a actividad 40000000-0000-0000-0000-000000000009 | 422 |
| Práctica inactiva | POST a actividad 40000000-0000-0000-0000-000000000010 | 422 |

Para opción ajena, ...0003 representa 50000000-0000-0000-0000-000000000003, que pertenece a otra actividad. Para los intentos directos sobre ...0009 y ...0010, se pueden usar sus opciones 50000000-0000-0000-0000-000000000009 y 50000000-0000-0000-0000-000000000011; el servicio rechaza el uso/estado antes de corregir la opción.

En códigos 404 y 422, revisa también status, title y detail en el cuerpo ProblemDetail.
