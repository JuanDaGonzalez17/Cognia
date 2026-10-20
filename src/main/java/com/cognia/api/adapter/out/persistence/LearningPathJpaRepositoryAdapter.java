package com.cognia.api.adapter.out.persistence;
import com.cognia.api.application.port.out.LearningPathRepository; import com.cognia.api.domain.model.*; import java.util.*; import org.springframework.stereotype.Repository; import org.springframework.transaction.annotation.Transactional;
@Repository @Transactional public class LearningPathJpaRepositoryAdapter implements LearningPathRepository {
	private final LearningPathJpaRepository paths; private final PathStepJpaRepository steps; private final LearningPathJpaMapper mapper=new LearningPathJpaMapper();
	public LearningPathJpaRepositoryAdapter(LearningPathJpaRepository paths,PathStepJpaRepository steps){this.paths=paths;this.steps=steps;}
	public Optional<LearningPath> active(UUID student,UUID subject){return paths.findFirstByStudentIdAndSubjectIdAndEstadoOrderByVersionDesc(student,subject,"ACTIVA").map(this::map);}
	public Optional<LearningPath> byId(UUID id){return paths.findById(id).map(this::map);}
	public Optional<LearningPath> latest(UUID student,UUID subject){return paths.findFirstByStudentIdAndSubjectIdOrderByVersionDesc(student,subject).map(this::map);}
	public LearningPath save(LearningPath path){var entity=paths.saveAndFlush(new LearningPathJpaEntity(path.studentId(),path.subjectId(),path.version(),path.state().name()));return map(entity);}
	public LearningPath update(LearningPath path){var entity=paths.findById(path.id()).orElseThrow();entity.setState(path.state().name());for(var step:path.steps())if(step.id()!=null)steps.findByIdAndPathId(step.id(),path.id()).ifPresent(row->row.setState(step.state().name()));paths.flush();return map(entity);}
	public List<PathStep> saveSteps(UUID pathId,List<PathStep> values){var rows=values.stream().map(s->new PathStepJpaEntity(pathId,s.conceptId(),s.order(),s.state().name())).toList();return steps.saveAllAndFlush(rows).stream().map(s->new PathStep(s.getId(),s.getConceptId(),s.getOrder(),PathStep.State.valueOf(s.getState()))).toList();}
	private LearningPath map(LearningPathJpaEntity path){return mapper.toDomain(path,steps.findAllByPathIdOrderByOrderAsc(path.getId()));}
}
