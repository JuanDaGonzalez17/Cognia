package com.cognia.api.adapter.out.persistence;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface LearningPathJpaRepository extends JpaRepository<LearningPathJpaEntity,UUID> {
	Optional<LearningPathJpaEntity> findFirstByStudentIdAndSubjectIdAndEstadoOrderByVersionDesc(UUID student,UUID subject,String state);
	Optional<LearningPathJpaEntity> findFirstByStudentIdAndSubjectIdOrderByVersionDesc(UUID student,UUID subject);
}
