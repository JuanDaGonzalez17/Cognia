package com.cognia.api.domain.service;
import com.cognia.api.domain.model.Activity;
import com.cognia.api.domain.model.ActivityOption;
import com.cognia.api.domain.model.ShortAnswerCriterion;
import java.util.UUID;
public class ActivityCorrectionService {
	public static final int MAX_ANSWER_LENGTH = 255;
	public boolean correctChoice(Activity activity, UUID optionId) { requireType(activity, Activity.Type.SELECCION_MULTIPLE); return activity.options().stream().filter(o -> o.id().equals(optionId)).findFirst().orElseThrow(() -> new IllegalArgumentException("Option does not belong to activity")).correct(); }
	public boolean correctShortAnswer(Activity activity, String answer) { requireType(activity, Activity.Type.RESPUESTA_CORTA); validateAnswer(answer); return activity.criteria().stream().anyMatch(c -> c.matches(answer)); }
	public static void validateAnswer(String answer) { if (answer == null || answer.isBlank()) throw new IllegalArgumentException("Answer must not be blank"); if (answer.length() > MAX_ANSWER_LENGTH) throw new IllegalArgumentException("Answer exceeds 255 characters"); }
	private static void requireType(Activity a, Activity.Type t) { if (a.type() != t) throw new IllegalArgumentException("Unexpected activity type"); }
}
