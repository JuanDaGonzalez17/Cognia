package com.cognia.api.application.port.out;
import com.cognia.api.domain.model.Activity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface ActivityRepository { List<Activity> findPracticeByConcept(UUID conceptId, Integer difficulty); default List<Activity> findPracticeByConceptAtMost(UUID conceptId,Integer difficulty){return findPracticeByConcept(conceptId,difficulty);} List<Activity> findDiagnosticBySubject(UUID subjectId); default List<Activity> findShortEvaluationByConcept(UUID conceptId){return List.of();} Optional<Activity> findById(UUID id); }
