package com.cognia.api.adapter.out.persistence;

import com.cognia.api.application.port.out.DiagnosticRepository;
import com.cognia.api.application.service.ResourceNotFoundException;
import com.cognia.api.domain.model.*;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository @Transactional
public class DiagnosticJpaRepositoryAdapter implements DiagnosticRepository {
	private final EntityManager em;
	public DiagnosticJpaRepositoryAdapter(EntityManager em){this.em=em;}
	public Optional<DiagnosticSession> findInProgress(UUID student,UUID subject){return em.createQuery("select s from DiagnosticSessionJpaEntity s where s.studentId=:student and s.subjectId=:subject and s.type='DIAGNOSTICO' and s.state='EN_PROGRESO' order by s.id",DiagnosticSessionJpaEntity.class).setParameter("student",student).setParameter("subject",subject).setMaxResults(1).getResultList().stream().map(this::toDomain).findFirst();}
	public boolean hasCompleted(UUID student,UUID subject){return !em.createQuery("select s.id from DiagnosticSessionJpaEntity s where s.studentId=:student and s.subjectId=:subject and s.type='DIAGNOSTICO' and s.state='COMPLETADA'",UUID.class).setParameter("student",student).setParameter("subject",subject).setMaxResults(1).getResultList().isEmpty();}
	public DiagnosticSession saveSession(DiagnosticSession session){if(session.id()==null){var entity=new DiagnosticSessionJpaEntity(session.studentId(),session.subjectId());em.persist(entity);em.flush();return toDomain(entity);}var entity=em.find(DiagnosticSessionJpaEntity.class,session.id());if(entity==null)throw new ResourceNotFoundException("Diagnostic session was not found");if("COMPLETADA".equals(session.state()))entity.complete(session.score(),session.closedAt());return toDomain(entity);}
	public Optional<DiagnosticSession> findSession(UUID id){return em.createQuery("select s from DiagnosticSessionJpaEntity s where s.id=:id and s.type='DIAGNOSTICO'",DiagnosticSessionJpaEntity.class).setParameter("id",id).setMaxResults(1).getResultList().stream().map(this::toDomain).findFirst();}
	public List<DiagnosticAnswer> answers(UUID id){return em.createQuery("select a from DiagnosticAnswerJpaEntity a where a.sessionId=:id order by a.id",DiagnosticAnswerJpaEntity.class).setParameter("id",id).getResultList().stream().map(DiagnosticAnswerJpaEntity::toDomain).toList();}
	public DiagnosticAnswer saveAnswer(DiagnosticAnswer a){var entity=new DiagnosticAnswerJpaEntity(a.sessionId(),a.activityId(),a.optionId(),a.answer(),a.correct(),a.seconds());em.persist(entity);em.flush();return entity.toDomain();}
	public UUID ensureProfile(UUID student,UUID subject){var found=em.createQuery("select p from KnowledgeProfileJpaEntity p where p.studentId=:student and p.subjectId=:subject",KnowledgeProfileJpaEntity.class).setParameter("student",student).setParameter("subject",subject).setMaxResults(1).getResultList();if(!found.isEmpty())return found.get(0).getId();var profile=new KnowledgeProfileJpaEntity(student,subject);em.persist(profile);em.flush();return profile.getId();}
	public void completeProfile(UUID id){var p=em.find(KnowledgeProfileJpaEntity.class,id);if(p!=null){p.complete();em.createNativeQuery("update perfil_conocimiento set fecha_actualizacion=current_timestamp where id=:id").setParameter("id",id).executeUpdate();}}
	public KnowledgeProfile profile(UUID student,UUID subject){var profiles=em.createQuery("select p from KnowledgeProfileJpaEntity p where p.studentId=:student and p.subjectId=:subject",KnowledgeProfileJpaEntity.class).setParameter("student",student).setParameter("subject",subject).setMaxResults(1).getResultList();if(profiles.isEmpty())throw new ResourceNotFoundException("Knowledge profile was not found");var p=profiles.get(0);var mastery=em.createQuery("select m from ConceptMasteryJpaEntity m where m.profileId=:id order by m.conceptId",ConceptMasteryJpaEntity.class).setParameter("id",p.getId()).getResultList().stream().map(ConceptMasteryJpaEntity::toDomain).toList();return new KnowledgeProfile(p.getId(),student,subject,p.isDiagnosticCompleted(),mastery);}
	public Optional<ConceptMastery> mastery(UUID profile,UUID concept){return em.createQuery("select m from ConceptMasteryJpaEntity m where m.profileId=:profile and m.conceptId=:concept",ConceptMasteryJpaEntity.class).setParameter("profile",profile).setParameter("concept",concept).setMaxResults(1).getResultList().stream().map(ConceptMasteryJpaEntity::toDomain).findFirst();}
	public ConceptMastery saveMastery(UUID profile,ConceptMastery m){var found=em.createQuery("select x from ConceptMasteryJpaEntity x where x.profileId=:profile and x.conceptId=:concept",ConceptMasteryJpaEntity.class).setParameter("profile",profile).setParameter("concept",m.conceptId()).setMaxResults(1).getResultList();if(found.isEmpty())em.persist(new ConceptMasteryJpaEntity(profile,m.conceptId(),m.level(),m.totalEvaluations(),m.totalCorrect()));else found.get(0).update(m.level(),m.totalEvaluations(),m.totalCorrect());em.flush();return m;}
	public void saveHistory(UUID profile,UUID concept,double previous,double next,MasteryChangeSource source,UUID session){var mastery=em.createQuery("select m from ConceptMasteryJpaEntity m where m.profileId=:profile and m.conceptId=:concept",ConceptMasteryJpaEntity.class).setParameter("profile",profile).setParameter("concept",concept).getSingleResult();em.persist(new MasteryHistoryJpaEntity(mastery.getId(),previous,next,source.name(),session));}
	private DiagnosticSession toDomain(DiagnosticSessionJpaEntity e){return new DiagnosticSession(e.getId(),e.getStudentId(),e.getSubjectId(),e.getState(),e.getScore(),e.getClosedAt());}
}
