package com.cognia.api.application.port.in;
import com.cognia.api.domain.model.*; import java.util.*;
public interface DiagnosticUseCases {
	DiagnosticStart start(UUID studentId, UUID subjectId);
	DiagnosticAnswer answer(UUID studentId, UUID sessionId, UUID activityId, UUID optionId, String answer, Integer seconds);
	DiagnosticSession finish(UUID studentId, UUID sessionId);
	KnowledgeProfile profile(UUID studentId, UUID subjectId);
	record DiagnosticStart(DiagnosticSession session, List<Activity> activities) {}
}
