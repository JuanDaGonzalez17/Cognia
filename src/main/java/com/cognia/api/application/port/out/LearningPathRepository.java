package com.cognia.api.application.port.out;
import com.cognia.api.domain.model.*; import java.util.*;
public interface LearningPathRepository {
	Optional<LearningPath> active(UUID student,UUID subject); Optional<LearningPath> byId(UUID id); Optional<LearningPath> latest(UUID student,UUID subject);
	LearningPath save(LearningPath path); LearningPath update(LearningPath path); List<PathStep> saveSteps(UUID pathId,List<PathStep> steps);
}
