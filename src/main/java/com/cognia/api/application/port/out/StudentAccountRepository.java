package com.cognia.api.application.port.out;

import com.cognia.api.domain.model.StudentAccount;
import java.util.Optional;

public interface StudentAccountRepository {
	Optional<StudentAccount> findByEmail(String email);
	Optional<StudentAccount> findById(java.util.UUID id);
	StudentAccount save(StudentAccount account);
	boolean existsByEmail(String email);
}
