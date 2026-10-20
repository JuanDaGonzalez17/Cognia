package com.cognia.api.application.port.out;
import com.cognia.api.domain.model.Activity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface ActivityRepository { List<Activity> findPracticeByConcept(UUID conceptId, Integer difficulty); List<Activity> findDiagnosticBySubject(UUID subjectId); Optional<Activity> findById(UUID id); }
