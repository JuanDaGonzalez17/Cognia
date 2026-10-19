package com.cognia.api.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "concepto")
public class ConceptJpaEntity {
	@Id
	private UUID id;
	@Column(name = "asignatura_id", nullable = false)
	private UUID subjectId;
	@Column(name = "codigo", nullable = false)
	private String code;
	@Column(name = "nombre", nullable = false)
	private String name;
	@Column(name = "descripcion")
	private String description;
	@Column(name = "nivel", nullable = false)
	private int level;
	@Column(name = "orden_sugerido", nullable = false)
	private int suggestedOrder;
	@Column(name = "activo", nullable = false)
	private boolean active;

	protected ConceptJpaEntity() {
	}

	public ConceptJpaEntity(UUID id, UUID subjectId, String code, String name, String description,
			int level, int suggestedOrder, boolean active) {
		this.id = id;
		this.subjectId = subjectId;
		this.code = code;
		this.name = name;
		this.description = description;
		this.level = level;
		this.suggestedOrder = suggestedOrder;
		this.active = active;
	}

	public UUID getId() { return id; }
	public UUID getSubjectId() { return subjectId; }
	public String getCode() { return code; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public int getLevel() { return level; }
	public int getSuggestedOrder() { return suggestedOrder; }
	public boolean isActive() { return active; }
}
