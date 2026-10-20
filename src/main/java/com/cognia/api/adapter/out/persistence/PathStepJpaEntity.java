package com.cognia.api.adapter.out.persistence;
import jakarta.persistence.Column; import jakarta.persistence.Entity; import jakarta.persistence.GeneratedValue; import jakarta.persistence.GenerationType; import jakarta.persistence.Id; import jakarta.persistence.Table; import jakarta.persistence.UniqueConstraint; import java.util.UUID;
@Entity @Table(name="paso_ruta",uniqueConstraints=@UniqueConstraint(name="uq_paso_ruta_concepto",columnNames={"ruta_id","concepto_id"})) public class PathStepJpaEntity {
	@Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(name="ruta_id",nullable=false) private UUID pathId; @Column(name="concepto_id",nullable=false) private UUID conceptId; @Column(name="orden_sugerido",nullable=false) private int order; @Column(name="estado",nullable=false) private String state;
	protected PathStepJpaEntity(){} PathStepJpaEntity(UUID path,UUID concept,int order,String state){pathId=path;conceptId=concept;this.order=order;this.state=state;}
	public UUID getId(){return id;} public UUID getPathId(){return pathId;} public UUID getConceptId(){return conceptId;} public int getOrder(){return order;} public String getState(){return state;} public void setState(String state){this.state=state;}
}
