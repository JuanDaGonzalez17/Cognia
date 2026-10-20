package com.cognia.api.adapter.out.persistence;
import com.cognia.api.application.port.out.ConceptExistencePort; import java.util.UUID; import org.springframework.stereotype.Repository;
@Repository public class ConceptExistenceAdapter implements ConceptExistencePort { private final ConceptJpaRepository repository; public ConceptExistenceAdapter(ConceptJpaRepository repository){this.repository=repository;} public boolean exists(UUID id){return repository.existsById(id);} }
