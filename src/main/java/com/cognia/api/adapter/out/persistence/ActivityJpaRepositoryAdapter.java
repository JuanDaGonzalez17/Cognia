package com.cognia.api.adapter.out.persistence;
import com.cognia.api.application.port.out.*; import com.cognia.api.domain.model.Activity; import java.util.*; import org.springframework.stereotype.Repository; import org.springframework.transaction.annotation.Transactional;
@Repository @Transactional(readOnly=true) public class ActivityJpaRepositoryAdapter implements ActivityRepository {
	private final ActivityJpaRepository activities; private final ActivityOptionJpaRepository options; private final ShortAnswerCriterionJpaRepository criteria; private final ConceptJpaRepository concepts; private final ActivityJpaMapper mapper=new ActivityJpaMapper();
	public ActivityJpaRepositoryAdapter(ActivityJpaRepository a,ActivityOptionJpaRepository o,ShortAnswerCriterionJpaRepository c,ConceptJpaRepository concepts){activities=a;options=o;criteria=c;this.concepts=concepts;}
	public List<Activity> findPracticeByConcept(UUID id,Integer difficulty){return activities.findPractice(id,difficulty).stream().map(this::map).toList();} public Optional<Activity> findById(UUID id){return activities.findById(id).map(this::map);} public boolean existsConcept(UUID id){return concepts.existsById(id);}
	private Activity map(ActivityJpaEntity e){return mapper.toDomain(e,options.findAllByActivityId(e.getId()),criteria.findAllByActivityId(e.getId()));}
}
