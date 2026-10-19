package com.cognia.api.application.port.in;

import com.cognia.api.domain.model.Prerequisite;
import java.util.UUID;

public interface AddPrerequisiteUseCase {
	Prerequisite addPrerequisite(UUID conceptId, UUID prerequisiteId);
}
