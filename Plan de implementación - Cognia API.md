# Plan de implementación - Cognia API

Oct 19, 2026 · @Alejandro

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

## Hoja de ruta

&#91;embedded content: hoja de ruta · 7 fases con su criterio de cierre\]

Cada rombo es el criterio que cierra la fase. La Fase 3 concentra el mayor riesgo y la Fase 5 es alcance adicional al anteproyecto, por eso va al final.

## Fase 0 · Preparación

Deja el repo listo para que el agente trabaje con contexto y sin sorpresas. Termina cuando la app arranca contra Neon y `./gradlew test` pasa con Testcontainers.

- Copiar al repo `CLAUDE.md` y `.claude/skills/`. Guardar `schema.sql` (el schema completo, ya cargado en Neon) en `src/main/resources/db/` como fuente de verdad. Sin Flyway: el schema se mantiene como un solo archivo.
- Agregar dependencias de PostgreSQL, Spring Data JPA, validación y Testcontainers. Verificar el nombre exacto de cada artefacto en la documentación oficial, porque Spring Boot 4 modularizó los starters.
- Perfiles: desarrollo contra Neon y pruebas con Testcontainers, que carga el mismo `schema.sql` como script de inicio. Hibernate con `ddl-auto` en `none` y `spring.sql.init.mode` en `never`, para que la app nunca modifique la base.
- Conexión a Neon por variable de entorno. Usar la conexión directa mientras se desarrolla y revisar la del pooler al desplegar. Ajustar los timeouts del pool al arranque en frío de Neon. Credenciales nunca en el repo: `.env.example` y `.env` en `.gitignore`.
- Registrar en `CLAUDE.md` la convención de nombres: tablas y columnas en español, dominio en inglés, y la traducción dentro del adaptador de persistencia.
- Fijar la fuente de verdad del alcance. Los repasos espaciados y el examen global no están en el anteproyecto: decidir si se documentan como alcance adicional.

## Fase 1 · Currículo

Es la primera slice y la que fija el patrón para todas las demás: reemplaza los datos en memoria de `Subject` y `Concept` por persistencia real y agrega el grafo de prerrequisitos. Termina cuando se puede crear y consultar un curso con su grafo válido por API.

| Capa | Qué se construye |
| --- | --- |
| Dominio | Alinear `Concept` con el schema (id UUID, `codigo`, `nivel`; sale `difficulty`). Servicio puro de validación del grafo: sin ciclos ni autoenlaces. |
| Puertos | Casos de uso de consultar asignaturas y conceptos, y de definir prerrequisitos. Repositorios de asignatura, concepto y prerrequisito. |
| Driven | Adaptador JPA con mapper sobre las tablas `asignatura`, `concepto` y `prerrequisito`. Seed de Cálculo Integral como script SQL guardado en el repo y aplicado a mano en Neon. |
| Driving | Endpoints `GET` de asignaturas y conceptos, y el de prerrequisitos. Errores con `ProblemDetail`. |

Pruebas clave: un ciclo de tres conceptos se rechaza, un prerrequisito de otra asignatura se rechaza (lo garantiza también la base), y el seed carga un grafo válido. El grafo del seed debe revisarlo un docente antes de darlo por oficial.

## Fase 2 · Actividades y práctica

Permite que un estudiante practique un concepto y reciba retroalimentación, sin que eso decida su avance. Termina cuando se puede pedir una actividad de un concepto, responderla y ver el resultado y la retroalimentación específica.

| Capa | Qué se construye |
| --- | --- |
| Dominio | `Activity` con `dificultad` (1 a 3), `uso` y `tipo`. Reglas de corrección: una opción correcta en selección múltiple; respuesta corta normalizada con patrón o expresión regular. |
| Puertos | Casos de uso de obtener actividades por concepto y uso, y de responder en práctica. Repositorios de actividad e intento de práctica. |
| Driven | Adaptador JPA sobre actividad, sus opciones y criterios, y la tabla de intentos de práctica. Seed de un banco mínimo por concepto. |
| Driving | `GET` de actividades y `POST` de respuesta de práctica. Nunca se devuelve cuál opción es la correcta antes de responder. |

Decisión de orden: la práctica depende del estudiante, así que `estudiante` entra en esta fase solo como identificador. La autenticación completa queda para la Fase 6, con el estudiante recibido por parámetro o por un usuario de prueba.

Las tablas ya existen en Neon desde el inicio: esta fase solo agrega código, sin cambios de schema salvo que aparezca un ajuste.

## Fase 3 · Estudiante, diagnóstico y dominio

Aquí entra lo adaptativo: el sistema estima el dominio de cada concepto a partir de evidencia. Termina cuando un estudiante hace el diagnóstico y recibe un perfil con su dominio estimado por concepto.

| Capa | Qué se construye |
| --- | --- |
| Dominio | `KnowledgeProfile` y dominio por concepto (0.0 a 1.0). Interfaz `MasteryPolicy` con una primera implementación por reglas y umbrales configurables. Se mantiene separada la evidencia (respuestas) de la estimación (dominio). |
| Puertos | Iniciar, responder y finalizar diagnóstico; consultar perfil. Repositorios de perfil, dominio, historial y sesión de evaluación. |
| Driven | Adaptador JPA sobre perfil, dominio, historial y las tablas de sesión de evaluación. Cada cambio de dominio escribe una fila en el historial con su origen. |
| Driving | Iniciar sesión de diagnóstico, enviar respuestas, finalizar y `GET` del perfil. |

Es la fase de mayor riesgo porque aquí vive el algoritmo. La política va detrás de su interfaz para poder cambiar reglas por BKT después, y se prueba con una tabla de casos. Los umbrales no se fijan sin validarlos con el docente.

Pruebas clave: la misma entrada produce siempre el mismo dominio, el historial registra cada cambio con origen `DIAGNOSTICO`, y una sesión no se completa sin puntaje.

## Fase 4 · Ruta y evaluación corta

Cierra el ciclo principal: el diagnóstico genera una ruta, y la evaluación corta de cada concepto decide si el estudiante avanza o refuerza. Termina cuando un estudiante recorre una ruta completa de ejemplo y esta se recalcula tras un fallo.

| Capa | Qué se construye |
| --- | --- |
| Dominio | `LearningPath` y `PathStep` con estados (bloqueado, disponible, en progreso, refuerzo, dominado). Generador de ruta que respeta prerrequisitos y orden topológico. Regla de avance o refuerzo tras una evaluación corta. |
| Puertos | Generar ruta, consultar siguiente paso, iniciar y finalizar evaluación corta. Repositorios de ruta y de paso. |
| Driven | Adaptador JPA sobre `V4`. Recalcular crea una versión nueva y marca la anterior como recalculada; nunca borra. |
| Driving | `GET` de la ruta y del siguiente paso, y los endpoints de evaluación corta. |

Reglas a cuidar: ninguna recomendación viola prerrequisitos, hay una sola ruta activa por estudiante y asignatura (la base lo impone), y cada actividad recomendada registra por qué se eligió.

Pruebas clave: la ruta generada es un orden topológico válido, un fallo deja el paso en refuerzo, y recalcular conserva el historial de versiones.

## Fase 5 · Repasos y examen global

Agrega retención a largo plazo y el cierre de la asignatura. Estas dos piezas no están en el anteproyecto, así que van al final: el producto ya funciona sin ellas. Termina cuando un estudiante completa una sesión de repaso diaria y, al terminar la ruta, presenta el examen global.

| Capa | Qué se construye |
| --- | --- |
| Dominio | Algoritmo SM-2 como servicio puro (intervalo, factor de facilidad, calificación 0 a 5). Regla de fallos consecutivos que dispara el recálculo. Selección del examen global entre los conceptos de la ruta. |
| Puertos | Obtener cola de repaso del día, calificar tarjeta, cerrar sesión de repaso; iniciar y finalizar examen global. |
| Driven | Adaptador JPA sobre tarjetas, progreso y sesiones de repaso. La cola diaria consulta `fecha_proximo_repaso <= now()` apoyándose en el índice por estudiante y fecha. |
| Driving | `GET` de la cola del día, `POST` de calificación y los endpoints del examen. |

Antes de empezar hay que cerrar dos decisiones de producto: con qué peso mueve el repaso el dominio estimado, y si el umbral de fallos se cuenta por tarjeta o por concepto. El umbral va en configuración.

Pruebas clave: SM-2 contra una tabla de casos conocida, el factor de facilidad nunca baja de 1.30, y dos fallos consecutivos recalculan la ruta.

## Fase 6 · Transversales

Lo que atraviesa todas las slices y se endurece al final. Termina cuando un estudiante nuevo puede registrarse y recorrer el flujo completo desde el navegador.

- **Autenticación y autorización:** registro e inicio de sesión con Spring Security. Cada endpoint toma al estudiante de la credencial y no de un parámetro. Decidir antes entre sesión y JWT.
- **Errores y validación:** respuestas uniformes con `ProblemDetail` y validación de entrada en los DTOs.
- **Documentación de la API:** OpenAPI generado desde los controllers.
- **Frontend (React y TypeScript, como en el anteproyecto):** un solo flujo primero, diagnóstico, ruta y práctica. Puede avanzar en paralelo desde la Fase 2 si hay una persona dedicada, contra la API ya probada.
- **Despliegue:** contenedor, variables de entorno y CI que corra los tests en cada cambio. El schema ya vive en Neon; los cambios futuros se aplican a mano (ver riesgos).

## Decisiones pendientes y riesgos

Seis decisiones frenan fases concretas; ninguna bloquea el arranque. Cada una se cierra antes de la fase indicada.

| Decisión | Opciones | Se necesita antes de |
| --- | --- | --- |
| Fuente de verdad del alcance | Anteproyecto, `PROJECT_CONTEXT.md`, o ambos con los repasos marcados como alcance adicional | Fase 0 |
| Identificador del dominio | UUID como id y `codigo` como slug, o slug como id | Fase 1 |
| Umbrales de aprobación y de dominio | Valores iniciales validados con el docente, siempre configurables | Fase 3 |
| Peso del repaso sobre el dominio estimado | Sin efecto, efecto parcial o igual que una evaluación | Fase 5 |
| Umbral de fallos que recalcula la ruta | Por tarjeta o por concepto | Fase 5 |
| Autenticación | Sesión o JWT | Fase 6, o antes si hay una demo |

Riesgos y respuesta:

- **El algoritmo de dominio** puede cambiar tras probarlo con estudiantes. Se aísla detrás de `MasteryPolicy` y se prueba con tablas de casos.
- **Schema sin historial de migraciones.** Los cambios a la base se aplican a mano en Neon y no quedan versionados. Mitigación: `schema.sql` es la única fuente de verdad, se edita primero ahí y las pruebas con Testcontainers lo cargan, así que detectan desajustes. Si más personas modifican la base, conviene adoptar migraciones.
- **Desajuste entre el schema y el código Java actual** (`Concept`). Se resuelve en la Fase 1, con pocas clases afectadas.
- **Los skills no se han probado contra el repo real.** La primera slice sirve de prueba: si el agente repite un error, se corrige el skill en ese momento.
- **Alcance adicional** (repasos y examen global) compite con el tiempo. Por eso van al final y el producto ya sirve sin ellos.

Una slice cuenta como terminada cuando sus tests pasan, sus endpoints responden y el `README.md` está al día. Falta la fecha de entrega para poner fechas a las fases.
