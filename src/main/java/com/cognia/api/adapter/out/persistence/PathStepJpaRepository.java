package com.cognia.api.adapter.out.persistence;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface PathStepJpaRepository extends JpaRepository<PathStepJpaEntity,UUID> { List<PathStepJpaEntity> findAllByPathIdOrderByOrderAsc(UUID pathId); Optional<PathStepJpaEntity> findByIdAndPathId(UUID id,UUID pathId); }
