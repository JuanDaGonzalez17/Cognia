package com.cognia.api.application.port.in;

import com.cognia.api.domain.model.Prerequisite;
import java.util.List;
import java.util.UUID;

public interface GetConceptPrerequisitesUseCase {
	List<Prerequisite> getPrerequisites(UUID conceptId);
}
