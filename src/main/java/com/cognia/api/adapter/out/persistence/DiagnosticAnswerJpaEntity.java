package com.cognia.api.adapter.out.persistence;
import com.cognia.api.domain.model.DiagnosticAnswer;
import jakarta.persistence.Entity; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="detalle_respuesta_evaluacion",uniqueConstraints=@UniqueConstraint(name="uq_sesion_actividad",columnNames={"sesion_evaluacion_id","actividad_id"})) public class DiagnosticAnswerJpaEntity {
	@Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(name="sesion_evaluacion_id",nullable=false) private UUID sessionId; @Column(name="actividad_id",nullable=false) private UUID activityId; @Column(name="opcion_seleccionada_id") private UUID optionId; @Column(name="texto_ingresado") private String answer; @Column(name="es_correcta",nullable=false) private boolean correct; @Column(name="segundos_empleados") private Integer seconds;
	protected DiagnosticAnswerJpaEntity(){} public DiagnosticAnswerJpaEntity(UUID session,UUID activity,UUID option,String answer,boolean correct,Integer seconds){sessionId=session;activityId=activity;optionId=option;this.answer=answer;this.correct=correct;this.seconds=seconds;} public DiagnosticAnswer toDomain(){return new DiagnosticAnswer(id,sessionId,activityId,optionId,answer,correct,seconds);}
}
