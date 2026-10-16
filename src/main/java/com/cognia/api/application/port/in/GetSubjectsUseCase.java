package com.cognia.api.application.port.in;

import com.cognia.api.domain.model.Subject;
import java.util.List;
import java.util.Optional;

public interface GetSubjectsUseCase {

	List<Subject> getAll();

	Optional<Subject> getById(String id);
}
