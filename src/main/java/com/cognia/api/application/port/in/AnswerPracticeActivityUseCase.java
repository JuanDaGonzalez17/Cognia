package com.cognia.api.application.port.in;
import com.cognia.api.domain.model.PracticeResult;
import java.util.UUID;
public interface AnswerPracticeActivityUseCase { PracticeResult answer(UUID studentId, UUID activityId, UUID optionId, String answer, Integer seconds); }
