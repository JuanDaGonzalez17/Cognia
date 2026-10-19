package com.cognia.api.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "prerrequisito")
@IdClass(PrerequisiteJpaEntity.PrerequisiteId.class)
public class PrerequisiteJpaEntity {
	@Id
	@Column(name = "concepto_id")
	private UUID conceptId;
	@Id
	@Column(name = "prerrequisito_id")
	private UUID prerequisiteId;
	@Column(name = "asignatura_id", nullable = false)
	private UUID subjectId;

	protected PrerequisiteJpaEntity() {
	}

	public PrerequisiteJpaEntity(UUID conceptId, UUID prerequisiteId, UUID subjectId) {
		this.conceptId = conceptId;
		this.prerequisiteId = prerequisiteId;
		this.subjectId = subjectId;
	}

	public UUID getConceptId() { return conceptId; }
	public UUID getPrerequisiteId() { return prerequisiteId; }
	public UUID getSubjectId() { return subjectId; }

	public static class PrerequisiteId implements Serializable {
		private UUID conceptId;
		private UUID prerequisiteId;

		public PrerequisiteId() {
		}

		public PrerequisiteId(UUID conceptId, UUID prerequisiteId) {
			this.conceptId = conceptId;
			this.prerequisiteId = prerequisiteId;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) return true;
			if (!(other instanceof PrerequisiteId that)) return false;
			return Objects.equals(conceptId, that.conceptId) && Objects.equals(prerequisiteId, that.prerequisiteId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(conceptId, prerequisiteId);
		}
	}
}
