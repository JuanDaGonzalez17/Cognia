package com.cognia.api.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import com.cognia.api.domain.model.ReviewProgress;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class Sm2ServiceTest {
	private final Sm2Service sm2=new Sm2Service();
	private ReviewProgress progress(double ef,int interval,int repetitions,int total,int consecutive,ReviewProgress.State state){return new ReviewProgress(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),ef,interval,repetitions,total,consecutive,Instant.parse("2026-01-01T00:00:00Z"),Instant.parse("2026-01-01T00:00:00Z"),state);}

	@Test void knownSuccessfulSequenceUsesOneThenSixThenRoundedEaseIntervals(){var p=progress(2.5,0,0,0,0,ReviewProgress.State.APRENDIENDO);var first=sm2.grade(p,5);assertEquals(1,first.intervalDays());assertEquals(1,first.repetitions());assertEquals(2.6,first.easeFactor());var second=sm2.grade(progress(first.easeFactor(),first.intervalDays(),first.repetitions(),0,0,ReviewProgress.State.REPASANDO),4);assertEquals(6,second.intervalDays());assertEquals(2.6,second.easeFactor());var third=sm2.grade(progress(second.easeFactor(),second.intervalDays(),second.repetitions(),0,0,ReviewProgress.State.REPASANDO),5);assertEquals(16,third.intervalDays());}
	@Test void failureResetsRepetitionsAndSchedulesRelearningAfterOneDay(){var result=sm2.grade(progress(2.5,24,4,2,1,ReviewProgress.State.REPASANDO),2);assertEquals(1,result.intervalDays());assertEquals(0,result.repetitions());assertEquals(3,result.totalLapses());assertEquals(2,result.consecutiveLapses());assertEquals(2.18,result.easeFactor());assertEquals(ReviewProgress.State.REAPRENDIENDO,result.state());}
	@Test void easeFactorNeverFallsBelowDatabaseFloor(){var p=progress(1.3,1,1,7,6,ReviewProgress.State.REAPRENDIENDO);assertEquals(1.3,sm2.grade(p,0).easeFactor());}
	@Test void successfulReviewAfterFailureResetsConsecutiveLapsesAndRestartsAtOneDay(){var result=sm2.grade(progress(2.18,1,0,3,2,ReviewProgress.State.REAPRENDIENDO),3);assertEquals(1,result.intervalDays());assertEquals(1,result.repetitions());assertEquals(3,result.totalLapses());assertEquals(0,result.consecutiveLapses());assertEquals(ReviewProgress.State.REPASANDO,result.state());}
	@Test void rejectsGradesOutsideTheSm2Scale(){assertThrows(IllegalArgumentException.class,()->sm2.grade(progress(2.5,0,0,0,0,ReviewProgress.State.APRENDIENDO),6));}
}
