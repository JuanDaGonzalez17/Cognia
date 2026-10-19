package com.cognia.api.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "asignatura")
public class SubjectJpaEntity {
	@Id
	private UUID id;
	@Column(name = "codigo", nullable = false)
	private String code;
	@Column(name = "nombre", nullable = false)
	private String name;
	@Column(name = "descripcion")
	private String description;
	@Column(name = "activa", nullable = false)
	private boolean active;

	protected SubjectJpaEntity() {
	}

	public SubjectJpaEntity(UUID id, String code, String name, String description, boolean active) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.active = active;
	}

	public UUID getId() { return id; }
	public String getCode() { return code; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public boolean isActive() { return active; }
}
