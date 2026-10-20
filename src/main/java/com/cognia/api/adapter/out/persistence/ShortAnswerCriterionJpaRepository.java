package com.cognia.api.adapter.out.persistence;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ShortAnswerCriterionJpaRepository extends JpaRepository<ShortAnswerCriterionJpaEntity,UUID> { List<ShortAnswerCriterionJpaEntity> findAllByActivityId(UUID activityId); }
