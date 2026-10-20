package com.cognia.api.adapter.out.persistence;
import com.cognia.api.domain.model.*;
public class ActivityJpaMapper {
	public Activity toDomain(ActivityJpaEntity e, java.util.List<ActivityOptionJpaEntity> options, java.util.List<ShortAnswerCriterionJpaEntity> criteria) { return new Activity(e.getId(),e.getConceptId(),e.getCode(),e.getStatement(),e.getFeedback(),e.getDifficulty(),Activity.Usage.valueOf(e.getUsage()),Activity.Type.valueOf(e.getType()),e.isActive(),options.stream().map(o->new ActivityOption(o.getId(),o.getText(),o.isCorrect(),o.getSpecificFeedback())).toList(),criteria.stream().map(c->new ShortAnswerCriterion(c.getPattern(),c.isRegularExpression())).toList()); }
	public PracticeAttemptJpaEntity toEntity(PracticeAttempt a){return new PracticeAttemptJpaEntity(a.studentId(),a.activityId(),a.selectedOptionId(),a.answer(),a.correct(),a.seconds());}
	public PracticeAttempt toDomain(PracticeAttemptJpaEntity e){return new PracticeAttempt(e.getId(),e.getStudentId(),e.getActivityId(),e.getSelectedOptionId(),e.getAnswer(),e.isCorrect(),e.getSeconds());}
}
