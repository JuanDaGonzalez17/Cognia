-- PROVISIONAL: tarjetas y reactivos sintéticos para desarrollo. Validar redacción, claves y cobertura con un docente.
INSERT INTO tarjeta_repaso(id,concepto_id,codigo,anverso_pregunta,reverso_respuesta,pista,activa) VALUES
('70000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','C01-R1','¿Cuál es una antiderivada de 2x?','x² + C','Usa la regla de potencia.',TRUE),
('70000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','C01-R2','¿Qué operación comprueba una antiderivada?','Derivarla y comprobar que se obtiene el integrando.','Invierte la integración.',TRUE),
('70000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000002','C11-R1','Integra 3x².','x³ + C','Aumenta el exponente y divide.',TRUE),
('70000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000002','C11-R2','¿Qué representa C en una integral indefinida?','La constante arbitraria de integración.','Considera la familia de primitivas.',TRUE),
('70000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000003','C21-R1','Calcula la integral de 3x² entre 0 y 1.','1','Evalúa una primitiva en los extremos.',TRUE),
('70000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000003','C21-R2','¿Qué teorema conecta antiderivadas e integral definida?','Teorema fundamental del cálculo.','Relaciona derivación e integración.',TRUE),
('70000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000004','C31-R1','¿Converge la integral de 1/x² desde 1 hasta infinito?','Sí; su valor es 1.','Compara con una p-integral.',TRUE),
('70000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000004','C31-R2','¿Qué se estudia en una integral impropia?','Un intervalo infinito o una discontinuidad del integrando.','Revisa los límites de integración.',TRUE)
ON CONFLICT(id) DO NOTHING;

INSERT INTO actividad(id,concepto_id,codigo,enunciado,retroalimentacion,dificultad,uso,tipo,activa) VALUES
('71000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','C01-EG1','¿Cuál es una antiderivada de 2x?','',1,'EXAMEN_GLOBAL','SELECCION_MULTIPLE',TRUE),
('71000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','C01-EG2','Escribe una antiderivada de 3x².','',2,'EXAMEN_GLOBAL','RESPUESTA_CORTA',TRUE),
('71000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000002','C11-EG1','¿Qué expresa la constante de integración?','',1,'EXAMEN_GLOBAL','SELECCION_MULTIPLE',TRUE),
('71000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000002','C11-EG2','Escribe la integral de 2x.','',2,'EXAMEN_GLOBAL','RESPUESTA_CORTA',TRUE),
('71000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000003','C21-EG1','Calcula la integral de 2x entre 0 y 2.','',2,'EXAMEN_GLOBAL','SELECCION_MULTIPLE',TRUE),
('71000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000003','C21-EG2','¿Qué relación describe el teorema fundamental?','',3,'EXAMEN_GLOBAL','RESPUESTA_CORTA',TRUE),
('71000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000004','C31-EG1','¿Converge la integral de 1/x² desde 1 hasta infinito?','',1,'EXAMEN_GLOBAL','SELECCION_MULTIPLE',TRUE),
('71000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000004','C31-EG2','Nombra una condición que hace impropia una integral.','',3,'EXAMEN_GLOBAL','RESPUESTA_CORTA',TRUE)
ON CONFLICT(id) DO NOTHING;

INSERT INTO actividad_opcion(id,actividad_id,texto,es_correcta) VALUES
('72000000-0000-0000-0000-000000000001','71000000-0000-0000-0000-000000000001','x² + C',TRUE),('72000000-0000-0000-0000-000000000002','71000000-0000-0000-0000-000000000001','2x² + C',FALSE),
('72000000-0000-0000-0000-000000000003','71000000-0000-0000-0000-000000000003','Una constante arbitraria',TRUE),('72000000-0000-0000-0000-000000000004','71000000-0000-0000-0000-000000000003','El valor de la integral definida',FALSE),
('72000000-0000-0000-0000-000000000005','71000000-0000-0000-0000-000000000005','4',TRUE),('72000000-0000-0000-0000-000000000006','71000000-0000-0000-0000-000000000005','2',FALSE),
('72000000-0000-0000-0000-000000000007','71000000-0000-0000-0000-000000000007','Sí, converge',TRUE),('72000000-0000-0000-0000-000000000008','71000000-0000-0000-0000-000000000007','No, diverge',FALSE)
ON CONFLICT(id) DO NOTHING;

INSERT INTO actividad_criterio_rc(id,actividad_id,respuesta_patron,es_expresion_regular) VALUES
('73000000-0000-0000-0000-000000000001','71000000-0000-0000-0000-000000000002','x^3',FALSE),
('73000000-0000-0000-0000-000000000002','71000000-0000-0000-0000-000000000004','x^2 + C',FALSE),
('73000000-0000-0000-0000-000000000003','71000000-0000-0000-0000-000000000006','la integral de una derivada recupera la función salvo constante',FALSE),
('73000000-0000-0000-0000-000000000004','71000000-0000-0000-0000-000000000008','intervalo infinito',FALSE)
ON CONFLICT(id) DO NOTHING;
