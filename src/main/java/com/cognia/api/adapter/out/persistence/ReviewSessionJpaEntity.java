package com.cognia.api.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="sesion_repaso")
public class ReviewSessionJpaEntity {
	@Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
	@Column(name="estudiante_id",nullable=false) private UUID studentId;
	@Column(name="asignatura_id",nullable=false) private UUID subjectId;
	@Column(name="fecha_inicio",nullable=false) private Instant startedAt;
	@Column(name="fecha_fin") private Instant finishedAt;
	@Column(name="total_tarjetas_revisadas",nullable=false) private int reviewed;
	@Column(name="total_recordadas",nullable=false) private int remembered;
	@Column(name="total_olvidadas",nullable=false) private int forgotten;
	protected ReviewSessionJpaEntity() { }
	public ReviewSessionJpaEntity(UUID student,UUID subject,Instant now){studentId=student;subjectId=subject;startedAt=now;}
	public UUID getId(){return id;} public UUID getStudentId(){return studentId;} public UUID getSubjectId(){return subjectId;} public Instant getStartedAt(){return startedAt;} public Instant getFinishedAt(){return finishedAt;} public int getReviewed(){return reviewed;} public int getRemembered(){return remembered;} public int getForgotten(){return forgotten;}
	public void finish(Instant at,int reviewed,int remembered,int forgotten){finishedAt=at;this.reviewed=reviewed;this.remembered=remembered;this.forgotten=forgotten;}
}
