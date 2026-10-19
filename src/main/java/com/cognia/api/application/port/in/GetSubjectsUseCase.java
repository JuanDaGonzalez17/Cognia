package com.cognia.api.application.port.in;

import com.cognia.api.domain.model.Subject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetSubjectsUseCase {

	List<Subject> getAll();

	Optional<Subject> getById(UUID id);
}
