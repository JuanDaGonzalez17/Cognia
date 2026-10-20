package com.cognia.api.adapter.out.persistence;
import jakarta.persistence.Entity; import jakarta.persistence.*; import java.util.UUID;
@Entity @Table(name="historial_dominio") public class MasteryHistoryJpaEntity {
	@Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(name="dominio_conceptual_id",nullable=false) private UUID masteryId; @Column(name="nivel_dominio_anterior",nullable=false) private double previous; @Column(name="nivel_dominio_nuevo",nullable=false) private double next; @Column(name="origen_cambio",nullable=false) private String source; @Column(name="referencia_id") private UUID reference;
	protected MasteryHistoryJpaEntity(){} public MasteryHistoryJpaEntity(UUID mastery,double previous,double next,String source,UUID session){masteryId=mastery;this.previous=previous;this.next=next;this.source=source;reference=session;}
}
