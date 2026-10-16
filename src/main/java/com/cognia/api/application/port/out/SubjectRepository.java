package com.cognia.api.application.port.out;

import com.cognia.api.domain.model.Subject;
import java.util.List;
import java.util.Optional;

public interface SubjectRepository {

	List<Subject> findAll();

	Optional<Subject> findById(String id);
}
