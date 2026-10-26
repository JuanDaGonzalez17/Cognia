package com.cognia.api.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="detalle_respuesta_repaso")
public class ReviewAnswerJpaEntity {
	@Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
	@Column(name="sesion_repaso_id",nullable=false) private UUID sessionId;
	@Column(name="progreso_repaso_id",nullable=false) private UUID progressId;
	@Column(name="calificacion_sm2",nullable=false) private int grade;
	@Column(name="intervalo_anterior_dias",nullable=false) private int oldInterval;
	@Column(name="intervalo_nuevo_dias",nullable=false) private int newInterval;
	@Column(name="factor_facilidad_nuevo",nullable=false) private double easeFactor;
	@Column(name="segundos_empleados") private Integer seconds;
	@Column(name="fecha_respuesta",insertable=false,updatable=false) private Instant answeredAt;
	protected ReviewAnswerJpaEntity() { }
	public ReviewAnswerJpaEntity(UUID session,UUID progress,int grade,int oldInterval,int newInterval,double ef,Integer seconds){sessionId=session;progressId=progress;this.grade=grade;this.oldInterval=oldInterval;this.newInterval=newInterval;easeFactor=ef;this.seconds=seconds;}
	public UUID getId(){return id;} public UUID getSessionId(){return sessionId;} public UUID getProgressId(){return progressId;} public int getGrade(){return grade;} public int getOldInterval(){return oldInterval;} public int getNewInterval(){return newInterval;} public double getEaseFactor(){return easeFactor;}
}
