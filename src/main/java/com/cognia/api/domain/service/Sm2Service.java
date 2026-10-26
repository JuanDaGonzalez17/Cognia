package com.cognia.api.domain.service;

import com.cognia.api.domain.model.ReviewProgress;

public class Sm2Service {
	public record Schedule(double easeFactor, int intervalDays, int repetitions, int totalLapses, int consecutiveLapses, ReviewProgress.State state) { }
	public Schedule grade(ReviewProgress progress, int grade) {
		if (grade < 0 || grade > 5) throw new IllegalArgumentException("Grade must be between 0 and 5");
		var difference = 5 - grade;
		var ease = Math.max(1.30, Math.round((progress.easeFactor() + (0.1 - difference * (0.08 + difference * 0.02))) * 100.0) / 100.0);
		if (grade < 3) return new Schedule(ease, 1, 0, progress.totalLapses() + 1, progress.consecutiveLapses() + 1, ReviewProgress.State.REAPRENDIENDO);
		var interval = progress.repetitions() == 0 ? 1 : progress.repetitions() == 1 ? 6 : (int) Math.round(progress.intervalDays() * ease);
		var state = progress.state() == ReviewProgress.State.APRENDIENDO ? ReviewProgress.State.REPASANDO : ReviewProgress.State.REPASANDO;
		return new Schedule(ease, interval, progress.repetitions() + 1, progress.totalLapses(), 0, state);
	}
}
