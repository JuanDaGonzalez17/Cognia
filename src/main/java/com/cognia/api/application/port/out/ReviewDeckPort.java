package com.cognia.api.application.port.out;

import com.cognia.api.domain.model.ReviewCard;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewDeckPort {
	List<ReviewCard> activeCards(UUID conceptId);
	Optional<ReviewCard> card(UUID cardId);
	void addMissingProgress(UUID studentId, UUID conceptId, List<ReviewCard> cards, Instant firstDueAt);
	void resetConsecutiveLapses(UUID studentId, UUID conceptId);
}
