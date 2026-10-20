package com.cognia.api.application.port.out;
import com.cognia.api.domain.model.*; import java.util.*;
public interface DiagnosticRepository {
	Optional<DiagnosticSession> findInProgress(UUID studentId, UUID subjectId); boolean hasCompleted(UUID studentId, UUID subjectId);
	DiagnosticSession saveSession(DiagnosticSession session); Optional<DiagnosticSession> findSession(UUID id);
	List<DiagnosticAnswer> answers(UUID sessionId); DiagnosticAnswer saveAnswer(DiagnosticAnswer answer);
	UUID ensureProfile(UUID studentId, UUID subjectId); void completeProfile(UUID profileId); KnowledgeProfile profile(UUID studentId, UUID subjectId);
	Optional<ConceptMastery> mastery(UUID profileId, UUID conceptId); ConceptMastery saveMastery(UUID profileId, ConceptMastery mastery);
	void saveHistory(UUID profileId, UUID conceptId, double previous, double next, MasteryChangeSource source, UUID sessionId);
}
