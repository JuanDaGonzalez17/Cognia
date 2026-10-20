package com.cognia.api.adapter.out.persistence;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
public interface ActivityOptionJpaRepository extends JpaRepository<ActivityOptionJpaEntity,UUID> { List<ActivityOptionJpaEntity> findAllByActivityId(UUID activityId); }
