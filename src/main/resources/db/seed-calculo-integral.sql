-- PROVISIONAL: conceptos candidatos de PROJECT_CONTEXT.md.
-- Los niveles y prerrequisitos de ejemplo deben ser revisados y validados por un docente.
INSERT INTO asignatura (id, codigo, nombre, descripcion, activa) VALUES
    ('10000000-0000-0000-0000-000000000001', 'calculo-integral', 'Cálculo Integral',
     'Currículo provisional para pruebas y desarrollo local.', TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO concepto (id, asignatura_id, codigo, nombre, descripcion, nivel, orden_sugerido, activo) VALUES
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'C0.1',
     'Funciones primitivas y antiderivadas', 'Reconocimiento de antiderivadas y reglas iniciales.', 0, 0, TRUE),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'C1.1',
     'Reglas de integración', 'Aplicación de reglas básicas de integración.', 1, 1, TRUE),
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', 'C2.1',
     'Integrales definidas', 'Evaluación e interpretación de integrales definidas.', 2, 2, TRUE),
    ('20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', 'C3.1',
     'Integrales impropias', 'Introducción a integrales impropias.', 3, 3, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Aristas exclusivamente ilustrativas: A requiere B; validar el sentido y contenido con un docente.
INSERT INTO prerrequisito (asignatura_id, concepto_id, prerrequisito_id) VALUES
    ('10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002'),
    ('10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000003')
ON CONFLICT (concepto_id, prerrequisito_id) DO NOTHING;
