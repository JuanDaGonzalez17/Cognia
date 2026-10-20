
CREATE TABLE asignatura (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(30) NOT NULL UNIQUE,         -- ej. 'MAT-2508700' o 'calculo-integral'
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE concepto (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asignatura_id UUID NOT NULL,
    codigo VARCHAR(20) NOT NULL,                -- ej. 'C0.1', 'C1.2', 'C2.1'
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    nivel INTEGER NOT NULL DEFAULT 1,           -- 0: fundamentos externos, 1..N: temas
    orden_sugerido INTEGER NOT NULL,            -- orden topológico sugerido
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_concepto_asignatura
        FOREIGN KEY (asignatura_id) REFERENCES asignatura(id),
    CONSTRAINT uq_concepto_asignatura_codigo
        UNIQUE (asignatura_id, codigo),
    -- Habilita FKs compuestas que garantizan "misma asignatura" (ver prerrequisito)
    CONSTRAINT uq_concepto_id_asignatura
        UNIQUE (id, asignatura_id),
    CONSTRAINT ck_concepto_nivel
        CHECK (nivel BETWEEN 0 AND 10),
    CONSTRAINT ck_concepto_orden
        CHECK (orden_sugerido >= 0)
);

CREATE INDEX idx_concepto_asignatura_nivel ON concepto(asignatura_id, nivel);

-- Grafo de dependencias. asignatura_id garantiza que concepto y prerrequisito
-- sean de la misma asignatura. Los ciclos NO se validan aquí: van en la capa
-- de aplicación (SQL no los cubre).
CREATE TABLE prerrequisito (
    asignatura_id UUID NOT NULL,
    concepto_id UUID NOT NULL,
    prerrequisito_id UUID NOT NULL,

    PRIMARY KEY (concepto_id, prerrequisito_id),

    CONSTRAINT fk_prerreq_concepto
        FOREIGN KEY (concepto_id, asignatura_id)
        REFERENCES concepto(id, asignatura_id) ON DELETE CASCADE,
    CONSTRAINT fk_prerreq_requerido
        FOREIGN KEY (prerrequisito_id, asignatura_id)
        REFERENCES concepto(id, asignatura_id) ON DELETE CASCADE,
    CONSTRAINT ck_prerreq_no_autoenlace
        CHECK (concepto_id <> prerrequisito_id)
);

-- idx por (concepto_id) no hace falta: lo cubre el prefijo de la PK
CREATE INDEX idx_prerreq_requerido ON prerrequisito(prerrequisito_id);


CREATE TABLE actividad (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concepto_id UUID NOT NULL,
    codigo VARCHAR(30) NOT NULL UNIQUE,         -- ej. 'C2.1-01', 'C3.1-04'
    enunciado TEXT NOT NULL,
    retroalimentacion TEXT,
    dificultad INTEGER NOT NULL,                -- 1: fácil, 2: media, 3: difícil
    uso VARCHAR(25) NOT NULL,
    tipo VARCHAR(25) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_actividad_concepto
        FOREIGN KEY (concepto_id) REFERENCES concepto(id),
    CONSTRAINT ck_actividad_dificultad
        CHECK (dificultad BETWEEN 1 AND 3),
    CONSTRAINT ck_actividad_uso
        CHECK (uso IN ('DIAGNOSTICO', 'PRACTICA', 'EVALUACION_CORTA', 'EXAMEN_GLOBAL')),
    CONSTRAINT ck_actividad_tipo
        CHECK (tipo IN ('SELECCION_MULTIPLE', 'RESPUESTA_CORTA'))
);

CREATE INDEX idx_actividad_concepto_uso ON actividad(concepto_id, uso);

-- Opciones de selección múltiple
CREATE TABLE actividad_opcion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actividad_id UUID NOT NULL,
    texto TEXT NOT NULL,
    es_correcta BOOLEAN NOT NULL DEFAULT FALSE,
    retroalimentacion_especifica TEXT,

    CONSTRAINT fk_opcion_actividad
        FOREIGN KEY (actividad_id) REFERENCES actividad(id) ON DELETE CASCADE,
    -- Habilita FK compuesta: la opción elegida debe pertenecer a la actividad
    CONSTRAINT uq_opcion_id_actividad
        UNIQUE (id, actividad_id)
);

CREATE INDEX idx_actividad_opcion_actividad ON actividad_opcion(actividad_id);

-- Una sola opción correcta por actividad (selección múltiple de respuesta única)
CREATE UNIQUE INDEX uq_opcion_una_correcta
    ON actividad_opcion(actividad_id) WHERE es_correcta;

-- Criterios de respuesta corta
CREATE TABLE actividad_criterio_rc (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actividad_id UUID NOT NULL,
    respuesta_patron VARCHAR(255) NOT NULL,     -- valor esperado normalizado
    es_expresion_regular BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_criterio_actividad
        FOREIGN KEY (actividad_id) REFERENCES actividad(id) ON DELETE CASCADE,
    CONSTRAINT ck_criterio_patron_no_vacio
        CHECK (length(btrim(respuesta_patron)) > 0)
);

CREATE INDEX idx_criterio_rc_actividad ON actividad_criterio_rc(actividad_id);


CREATE TABLE estudiante (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_completo VARCHAR(120) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMPTZ
);

-- Correo único sin distinguir mayúsculas
CREATE UNIQUE INDEX uq_estudiante_correo_lower ON estudiante (lower(correo));

CREATE TABLE perfil_conocimiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL,
    asignatura_id UUID NOT NULL,
    diagnostico_completado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_perfil_estudiante
        FOREIGN KEY (estudiante_id) REFERENCES estudiante(id) ON DELETE CASCADE,
    CONSTRAINT fk_perfil_asignatura
        FOREIGN KEY (asignatura_id) REFERENCES asignatura(id),
    CONSTRAINT uq_perfil_estudiante_asignatura
        UNIQUE (estudiante_id, asignatura_id)
);

-- Dominio estimado (0.0-1.0) por concepto. No es evidencia: la evidencia son
-- las respuestas/sesiones; esto es la estimación del sistema.
CREATE TABLE dominio_conceptual (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    perfil_id UUID NOT NULL,
    concepto_id UUID NOT NULL,
    nivel_dominio DECIMAL(5, 4) NOT NULL DEFAULT 0.0000,
    total_evaluaciones INTEGER NOT NULL DEFAULT 0,
    total_aciertos INTEGER NOT NULL DEFAULT 0,
    fecha_actualizacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dominio_perfil
        FOREIGN KEY (perfil_id) REFERENCES perfil_conocimiento(id) ON DELETE CASCADE,
    CONSTRAINT fk_dominio_concepto
        FOREIGN KEY (concepto_id) REFERENCES concepto(id),
    CONSTRAINT uq_dominio_perfil_concepto
        UNIQUE (perfil_id, concepto_id),
    CONSTRAINT ck_dominio_rango
        CHECK (nivel_dominio BETWEEN 0.0000 AND 1.0000),
    CONSTRAINT ck_dominio_contadores
        CHECK (total_evaluaciones >= 0
           AND total_aciertos >= 0
           AND total_aciertos <= total_evaluaciones)
);

-- Historial para trazabilidad de la evolución del aprendizaje
CREATE TABLE historial_dominio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dominio_conceptual_id UUID NOT NULL,
    nivel_dominio_anterior DECIMAL(5, 4) NOT NULL,
    nivel_dominio_nuevo DECIMAL(5, 4) NOT NULL,
    origen_cambio VARCHAR(30) NOT NULL,
    referencia_id UUID,                         -- id de la sesión que disparó el cambio
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historial_dominio
        FOREIGN KEY (dominio_conceptual_id) REFERENCES dominio_conceptual(id) ON DELETE CASCADE,
    CONSTRAINT ck_historial_rango
        CHECK (nivel_dominio_anterior BETWEEN 0.0000 AND 1.0000
           AND nivel_dominio_nuevo BETWEEN 0.0000 AND 1.0000),
    CONSTRAINT ck_historial_origen
        CHECK (origen_cambio IN ('DIAGNOSTICO', 'EVALUACION_CORTA', 'REPASO', 'EXAMEN_GLOBAL'))
);

CREATE INDEX idx_historial_dominio_fecha
    ON historial_dominio(dominio_conceptual_id, fecha_registro);


CREATE TABLE ruta_aprendizaje (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL,
    asignatura_id UUID NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ruta_estudiante
        FOREIGN KEY (estudiante_id) REFERENCES estudiante(id) ON DELETE CASCADE,
    CONSTRAINT fk_ruta_asignatura
        FOREIGN KEY (asignatura_id) REFERENCES asignatura(id),
    CONSTRAINT uq_ruta_version
        UNIQUE (estudiante_id, asignatura_id, version),
    CONSTRAINT ck_ruta_version
        CHECK (version >= 1),
    CONSTRAINT ck_ruta_estado
        CHECK (estado IN ('ACTIVA', 'COMPLETADA', 'RECALCULADA'))
);

-- A lo sumo una ruta ACTIVA por estudiante y asignatura
CREATE UNIQUE INDEX uq_ruta_una_activa
    ON ruta_aprendizaje(estudiante_id, asignatura_id) WHERE estado = 'ACTIVA';

CREATE TABLE paso_ruta (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ruta_id UUID NOT NULL,
    concepto_id UUID NOT NULL,
    orden_sugerido INTEGER NOT NULL,
    estado VARCHAR(25) NOT NULL DEFAULT 'BLOQUEADO',

    CONSTRAINT fk_paso_ruta
        FOREIGN KEY (ruta_id) REFERENCES ruta_aprendizaje(id) ON DELETE CASCADE,
    CONSTRAINT fk_paso_concepto
        FOREIGN KEY (concepto_id) REFERENCES concepto(id),
    CONSTRAINT uq_paso_ruta_concepto
        UNIQUE (ruta_id, concepto_id),
    CONSTRAINT ck_paso_orden
        CHECK (orden_sugerido >= 0),
    CONSTRAINT ck_paso_estado
        CHECK (estado IN ('BLOQUEADO', 'DISPONIBLE', 'EN_PROGRESO', 'REFUERZO', 'DOMINADO'))
);

CREATE INDEX idx_paso_ruta_estado ON paso_ruta(ruta_id, estado);


CREATE TABLE sesion_evaluacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL,
    tipo_evaluacion VARCHAR(25) NOT NULL,
    asignatura_id UUID NOT NULL,
    concepto_id UUID,                           -- obligatorio si es EVALUACION_CORTA
    paso_ruta_id UUID,
    intento_numero INTEGER NOT NULL DEFAULT 1,
    fecha_inicio TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion TIMESTAMPTZ,
    puntaje_obtenido DECIMAL(5, 2),             -- 0.00 a 100.00
    aprobada BOOLEAN,
    estado VARCHAR(20) NOT NULL DEFAULT 'EN_PROGRESO',

    CONSTRAINT fk_sesion_estudiante
        FOREIGN KEY (estudiante_id) REFERENCES estudiante(id) ON DELETE CASCADE,
    CONSTRAINT fk_sesion_asignatura
        FOREIGN KEY (asignatura_id) REFERENCES asignatura(id),
    CONSTRAINT fk_sesion_concepto
        FOREIGN KEY (concepto_id) REFERENCES concepto(id),
    CONSTRAINT fk_sesion_paso_ruta
        FOREIGN KEY (paso_ruta_id) REFERENCES paso_ruta(id),
    CONSTRAINT ck_sesion_tipo
        CHECK (tipo_evaluacion IN ('DIAGNOSTICO', 'EVALUACION_CORTA', 'EXAMEN_GLOBAL')),
    CONSTRAINT ck_sesion_estado
        CHECK (estado IN ('EN_PROGRESO', 'COMPLETADA', 'ABANDONADA')),
    CONSTRAINT ck_sesion_concepto_requerido
        CHECK (tipo_evaluacion <> 'EVALUACION_CORTA' OR concepto_id IS NOT NULL),
    CONSTRAINT ck_sesion_evaluacion_paso_requerido
        CHECK (tipo_evaluacion <> 'EVALUACION_CORTA' OR paso_ruta_id IS NOT NULL),
    CONSTRAINT ck_sesion_intento
        CHECK (intento_numero >= 1),
    CONSTRAINT ck_sesion_puntaje
        CHECK (puntaje_obtenido IS NULL OR puntaje_obtenido BETWEEN 0 AND 100),
    -- Una sesión completada debe traer resultado y fecha de cierre
    CONSTRAINT ck_sesion_completada_coherente
        CHECK (estado <> 'COMPLETADA'
            OR (puntaje_obtenido IS NOT NULL
                AND fecha_finalizacion IS NOT NULL
                AND ((tipo_evaluacion = 'DIAGNOSTICO' AND aprobada IS NULL)
                    OR (tipo_evaluacion <> 'DIAGNOSTICO' AND aprobada IS NOT NULL))))
);

CREATE INDEX idx_sesion_eval_estudiante
    ON sesion_evaluacion(estudiante_id, tipo_evaluacion, estado);
CREATE INDEX idx_sesion_eval_concepto
    ON sesion_evaluacion(estudiante_id, concepto_id, tipo_evaluacion);

CREATE TABLE detalle_respuesta_evaluacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sesion_evaluacion_id UUID NOT NULL,
    actividad_id UUID NOT NULL,
    opcion_seleccionada_id UUID,                -- selección múltiple
    texto_ingresado TEXT,                       -- respuesta corta
    es_correcta BOOLEAN NOT NULL,
    segundos_empleados INTEGER,

    CONSTRAINT fk_detalle_sesion
        FOREIGN KEY (sesion_evaluacion_id) REFERENCES sesion_evaluacion(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_actividad
        FOREIGN KEY (actividad_id) REFERENCES actividad(id),
    -- La opción elegida debe pertenecer a la actividad respondida
    CONSTRAINT fk_detalle_opcion
        FOREIGN KEY (opcion_seleccionada_id, actividad_id)
        REFERENCES actividad_opcion(id, actividad_id),
    CONSTRAINT uq_sesion_actividad
        UNIQUE (sesion_evaluacion_id, actividad_id),
    CONSTRAINT ck_detalle_eval_segundos
        CHECK (segundos_empleados IS NULL OR segundos_empleados >= 0)
);

-- Práctica formativa libre
CREATE TABLE intento_actividad_practica (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL,
    actividad_id UUID NOT NULL,
    opcion_seleccionada_id UUID,                -- para reconstruir la retroalimentación específica
    respuesta_dada TEXT NOT NULL,
    es_correcta BOOLEAN NOT NULL,
    segundos_empleados INTEGER,
    fecha_intento TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_intento_estudiante
        FOREIGN KEY (estudiante_id) REFERENCES estudiante(id) ON DELETE CASCADE,
    CONSTRAINT fk_intento_actividad
        FOREIGN KEY (actividad_id) REFERENCES actividad(id),
    CONSTRAINT fk_intento_opcion
        FOREIGN KEY (opcion_seleccionada_id, actividad_id)
        REFERENCES actividad_opcion(id, actividad_id),
    CONSTRAINT ck_intento_segundos
        CHECK (segundos_empleados IS NULL OR segundos_empleados >= 0)
);

CREATE INDEX idx_intento_estudiante_actividad
    ON intento_actividad_practica(estudiante_id, actividad_id);


CREATE TABLE tarjeta_repaso (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    concepto_id UUID NOT NULL,
    codigo VARCHAR(30) UNIQUE,
    anverso_pregunta TEXT NOT NULL,             -- pregunta de recuerdo activo
    reverso_respuesta TEXT NOT NULL,            -- respuesta esperada
    pista TEXT,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tarjeta_concepto
        FOREIGN KEY (concepto_id) REFERENCES concepto(id),
    -- Habilita FK compuesta desde progreso_repaso (consistencia tarjeta/concepto)
    CONSTRAINT uq_tarjeta_id_concepto
        UNIQUE (id, concepto_id)
);

CREATE INDEX idx_tarjeta_concepto ON tarjeta_repaso(concepto_id);

CREATE TABLE progreso_repaso (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL,
    tarjeta_id UUID NOT NULL,
    concepto_id UUID NOT NULL,                  -- desnormalizado; la FK compuesta evita incoherencias
    factor_facilidad DECIMAL(4, 2) NOT NULL DEFAULT 2.50,
    intervalo_dias INTEGER NOT NULL DEFAULT 0,
    repeticiones_consecutivas INTEGER NOT NULL DEFAULT 0,
    fallos_acumulados INTEGER NOT NULL DEFAULT 0,
    fallos_consecutivos INTEGER NOT NULL DEFAULT 0,   -- el umbral de recálculo va en configuración
    fecha_ultimo_repaso TIMESTAMPTZ,
    fecha_proximo_repaso TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'APRENDIENDO',

    CONSTRAINT fk_progreso_estudiante
        FOREIGN KEY (estudiante_id) REFERENCES estudiante(id) ON DELETE CASCADE,
    CONSTRAINT fk_progreso_tarjeta_concepto
        FOREIGN KEY (tarjeta_id, concepto_id)
        REFERENCES tarjeta_repaso(id, concepto_id),
    CONSTRAINT uq_estudiante_tarjeta
        UNIQUE (estudiante_id, tarjeta_id),
    CONSTRAINT ck_factor_facilidad_min
        CHECK (factor_facilidad >= 1.30),
    CONSTRAINT ck_progreso_contadores
        CHECK (intervalo_dias >= 0
           AND repeticiones_consecutivas >= 0
           AND fallos_acumulados >= 0
           AND fallos_consecutivos >= 0
           AND fallos_consecutivos <= fallos_acumulados),
    CONSTRAINT ck_progreso_estado
        CHECK (estado IN ('APRENDIENDO', 'REPASANDO', 'REAPRENDIENDO'))
);

-- Cola diaria: el filtro "fecha_proximo_repaso <= now()" va en la consulta.
-- (Un índice parcial con CURRENT_TIMESTAMP no es válido: el predicado debe ser inmutable.)
CREATE INDEX idx_progreso_repaso_due
    ON progreso_repaso(estudiante_id, fecha_proximo_repaso);
CREATE INDEX idx_progreso_repaso_concepto
    ON progreso_repaso(estudiante_id, concepto_id);

CREATE TABLE sesion_repaso (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL,
    asignatura_id UUID NOT NULL,
    fecha_inicio TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMPTZ,
    total_tarjetas_revisadas INTEGER NOT NULL DEFAULT 0,
    total_recordadas INTEGER NOT NULL DEFAULT 0,
    total_olvidadas INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_repaso_sesion_estudiante
        FOREIGN KEY (estudiante_id) REFERENCES estudiante(id) ON DELETE CASCADE,
    CONSTRAINT fk_repaso_sesion_asignatura
        FOREIGN KEY (asignatura_id) REFERENCES asignatura(id),
    CONSTRAINT ck_sesion_repaso_contadores
        CHECK (total_tarjetas_revisadas >= 0
           AND total_recordadas >= 0
           AND total_olvidadas >= 0
           AND total_recordadas + total_olvidadas <= total_tarjetas_revisadas)
);

CREATE INDEX idx_sesion_repaso_estudiante
    ON sesion_repaso(estudiante_id, fecha_inicio);

-- Calificación SM-2 (0-5) de cada tarjeta dentro de la sesión
CREATE TABLE detalle_respuesta_repaso (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sesion_repaso_id UUID NOT NULL,
    progreso_repaso_id UUID NOT NULL,
    calificacion_sm2 INTEGER NOT NULL,          -- 0: blackout total, 3: aceptable, 5: perfecto
    intervalo_anterior_dias INTEGER NOT NULL,
    intervalo_nuevo_dias INTEGER NOT NULL,
    factor_facilidad_nuevo DECIMAL(4, 2) NOT NULL,
    segundos_empleados INTEGER,
    fecha_respuesta TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_detalle_repaso_sesion
        FOREIGN KEY (sesion_repaso_id) REFERENCES sesion_repaso(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_repaso_progreso
        FOREIGN KEY (progreso_repaso_id) REFERENCES progreso_repaso(id) ON DELETE CASCADE,
    CONSTRAINT ck_calificacion_sm2_rango
        CHECK (calificacion_sm2 BETWEEN 0 AND 5),
    CONSTRAINT ck_detalle_repaso_valores
        CHECK (intervalo_anterior_dias >= 0
           AND intervalo_nuevo_dias >= 0
           AND factor_facilidad_nuevo >= 1.30
           AND (segundos_empleados IS NULL OR segundos_empleados >= 0))
);

CREATE INDEX idx_detalle_repaso_sesion ON detalle_respuesta_repaso(sesion_repaso_id);
CREATE INDEX idx_detalle_repaso_progreso ON detalle_respuesta_repaso(progreso_repaso_id);
