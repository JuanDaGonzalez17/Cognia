package com.cognia.api.adapter.out.persistence;
import com.cognia.api.domain.model.*; import java.util.List;
public class LearningPathJpaMapper {
	LearningPath toDomain(LearningPathJpaEntity path,List<PathStepJpaEntity> steps){return new LearningPath(path.getId(),path.getStudentId(),path.getSubjectId(),path.getVersion(),LearningPath.State.valueOf(path.getState()),steps.stream().map(s->new PathStep(s.getId(),s.getConceptId(),s.getOrder(),PathStep.State.valueOf(s.getState()))).toList());}
}
