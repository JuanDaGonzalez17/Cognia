package com.cognia.api.adapter.out.persistence;

import com.cognia.api.domain.model.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ReviewJpaMapper {
	public ReviewCard toDomain(ReviewCardJpaEntity e){return new ReviewCard(e.getId(),e.getConceptId(),e.getFront(),e.getBack(),e.getHint());}
	public ReviewCard toCardDomain(Object[] row){return new ReviewCard((UUID)row[0],(UUID)row[1],(String)row[2],(String)row[3],(String)row[4]);}
	public ReviewProgress toProgressDomain(Object[] row){return new ReviewProgress((UUID)row[0],(UUID)row[1],(UUID)row[2],(UUID)row[3],((Number)row[4]).doubleValue(),((Number)row[5]).intValue(),((Number)row[6]).intValue(),((Number)row[7]).intValue(),((Number)row[8]).intValue(),toInstant(row[9]),toInstant(row[10]),ReviewProgress.State.valueOf((String)row[11]));}
	public ReviewSession toSessionDomain(Object[] row){return new ReviewSession((UUID)row[0],(UUID)row[1],(UUID)row[2],toInstant(row[3]),toInstant(row[4]),((Number)row[5]).intValue(),((Number)row[6]).intValue(),((Number)row[7]).intValue());}
	public ReviewAnswer toAnswerDomain(Object[] row){return new ReviewAnswer((UUID)row[0],(UUID)row[1],(UUID)row[2],((Number)row[3]).intValue(),((Number)row[4]).intValue(),((Number)row[5]).intValue(),((Number)row[6]).doubleValue());}
	private Instant toInstant(Object value){if(value==null)return null;if(value instanceof Instant instant)return instant;if(value instanceof Timestamp timestamp)return timestamp.toInstant();if(value instanceof OffsetDateTime offsetDateTime)return offsetDateTime.toInstant();throw new IllegalArgumentException("Unsupported database timestamp type: "+value.getClass().getName());}
	public ReviewProgress toDomain(ReviewProgressJpaEntity e){return new ReviewProgress(e.getId(),e.getStudentId(),e.getCardId(),e.getConceptId(),e.getEaseFactor(),e.getIntervalDays(),e.getRepetitions(),e.getTotalLapses(),e.getConsecutiveLapses(),e.getLastReviewedAt(),e.getNextReviewAt(),ReviewProgress.State.valueOf(e.getState()));}
	public ReviewSession toDomain(ReviewSessionJpaEntity e){return new ReviewSession(e.getId(),e.getStudentId(),e.getSubjectId(),e.getStartedAt(),e.getFinishedAt(),e.getReviewed(),e.getRemembered(),e.getForgotten());}
	public ReviewAnswer toDomain(ReviewAnswerJpaEntity e){return new ReviewAnswer(e.getId(),e.getSessionId(),e.getProgressId(),e.getGrade(),e.getOldInterval(),e.getNewInterval(),e.getEaseFactor());}
	public GlobalExam toDomain(DiagnosticSessionJpaEntity e){return new GlobalExam(e.getId(),e.getStudentId(),e.getSubjectId(),e.getAttempt(),e.getState(),e.getClosedAt(),e.getScore(),e.getPassed());}
}
