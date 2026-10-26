package com.cognia.api.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="progreso_repaso", uniqueConstraints=@UniqueConstraint(name="uq_estudiante_tarjeta", columnNames={"estudiante_id","tarjeta_id"}))
public class ReviewProgressJpaEntity {
	@Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
	@Column(name="estudiante_id",nullable=false) private UUID studentId;
	@Column(name="tarjeta_id",nullable=false) private UUID cardId;
	@Column(name="concepto_id",nullable=false) private UUID conceptId;
	@Column(name="factor_facilidad",nullable=false) private double easeFactor=2.5;
	@Column(name="intervalo_dias",nullable=false) private int intervalDays;
	@Column(name="repeticiones_consecutivas",nullable=false) private int repetitions;
	@Column(name="fallos_acumulados",nullable=false) private int totalLapses;
	@Column(name="fallos_consecutivos",nullable=false) private int consecutiveLapses;
	@Column(name="fecha_ultimo_repaso") private Instant lastReviewedAt;
	@Column(name="fecha_proximo_repaso",nullable=false) private Instant nextReviewAt;
	@Column(name="estado",nullable=false) private String state;
	protected ReviewProgressJpaEntity() { }
	public ReviewProgressJpaEntity(UUID student,UUID card,UUID concept,Instant firstDue){studentId=student;cardId=card;conceptId=concept;nextReviewAt=firstDue;state="APRENDIENDO";}
	public UUID getId(){return id;} public UUID getStudentId(){return studentId;} public UUID getCardId(){return cardId;} public UUID getConceptId(){return conceptId;} public double getEaseFactor(){return easeFactor;} public int getIntervalDays(){return intervalDays;} public int getRepetitions(){return repetitions;} public int getTotalLapses(){return totalLapses;} public int getConsecutiveLapses(){return consecutiveLapses;} public Instant getLastReviewedAt(){return lastReviewedAt;} public Instant getNextReviewAt(){return nextReviewAt;} public String getState(){return state;}
	public void update(com.cognia.api.domain.model.ReviewProgress p){easeFactor=p.easeFactor();intervalDays=p.intervalDays();repetitions=p.repetitions();totalLapses=p.totalLapses();consecutiveLapses=p.consecutiveLapses();lastReviewedAt=p.lastReviewedAt();nextReviewAt=p.nextReviewAt();state=p.state().name();}
	public void resetConsecutive(){consecutiveLapses=0;}
}
