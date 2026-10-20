package com.cognia.api.adapter.out.persistence;
import jakarta.persistence.Entity; import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="perfil_conocimiento",uniqueConstraints=@UniqueConstraint(name="uq_perfil_estudiante_asignatura",columnNames={"estudiante_id","asignatura_id"})) public class KnowledgeProfileJpaEntity {
	@Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(name="estudiante_id",nullable=false) private UUID studentId; @Column(name="asignatura_id",nullable=false) private UUID subjectId; @Column(name="diagnostico_completado",nullable=false) private boolean diagnosticCompleted; @Column(name="fecha_actualizacion",insertable=false,updatable=false) private Instant updatedAt;
	protected KnowledgeProfileJpaEntity(){} public KnowledgeProfileJpaEntity(UUID student,UUID subject){studentId=student;subjectId=subject;} public UUID getId(){return id;} public UUID getStudentId(){return studentId;} public UUID getSubjectId(){return subjectId;} public boolean isDiagnosticCompleted(){return diagnosticCompleted;} public void complete(){diagnosticCompleted=true;}
}
