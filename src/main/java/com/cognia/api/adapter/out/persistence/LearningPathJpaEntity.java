package com.cognia.api.adapter.out.persistence;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="ruta_aprendizaje") public class LearningPathJpaEntity {
	@Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(name="estudiante_id",nullable=false) private UUID studentId; @Column(name="asignatura_id",nullable=false) private UUID subjectId; @Column(nullable=false) private int version; @Column(nullable=false) private String estado; @Column(name="fecha_creacion",insertable=false,updatable=false) private Instant createdAt;
	protected LearningPathJpaEntity(){} LearningPathJpaEntity(UUID student,UUID subject,int version,String state){this.studentId=student;this.subjectId=subject;this.version=version;this.estado=state;}
	public UUID getId(){return id;} public UUID getStudentId(){return studentId;} public UUID getSubjectId(){return subjectId;} public int getVersion(){return version;} public String getState(){return estado;} public void setState(String state){estado=state;}
}
