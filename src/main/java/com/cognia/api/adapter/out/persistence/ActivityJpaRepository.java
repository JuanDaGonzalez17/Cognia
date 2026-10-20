package com.cognia.api.adapter.out.persistence;
import java.util.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
public interface ActivityJpaRepository extends JpaRepository<ActivityJpaEntity,UUID> { @Query("select a from ActivityJpaEntity a where a.conceptId=:concept and a.usage='PRACTICA' and a.active=true and (:difficulty is null or a.difficulty=:difficulty) order by a.difficulty,a.code") List<ActivityJpaEntity> findPractice(@Param("concept") UUID concept,@Param("difficulty") Integer difficulty); }
