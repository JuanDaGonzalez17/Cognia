package com.cognia.api.adapter.out.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="tarjeta_repaso")
public class ReviewCardJpaEntity {
	@Id private UUID id;
	@Column(name="concepto_id", nullable=false) private UUID conceptId;
	@Column(name="anverso_pregunta", nullable=false) private String front;
	@Column(name="reverso_respuesta", nullable=false) private String back;
	@Column(name="pista") private String hint;
	@Column(name="activa", nullable=false) private boolean active;
	protected ReviewCardJpaEntity() { }
	public UUID getId(){return id;} public UUID getConceptId(){return conceptId;} public String getFront(){return front;} public String getBack(){return back;} public String getHint(){return hint;} public boolean isActive(){return active;}
}
