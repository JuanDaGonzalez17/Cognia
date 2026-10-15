# Adaptive Learning Platform --- Project Context

## 1. Purpose and product vision

Build an adaptive learning application that helps a student master an
academic subject through a personalized learning path. The system should
assess the student's initial knowledge, guide them through concepts,
provide practice activities with feedback, evaluate mastery at the
concept level, schedule active-recall reviews, and adapt the path when
the student struggles or forgets previously learned material.

The first pilot subject is **Integral Calculus**. The architecture
should support adding other subjects later, such as Programming
Techniques and Models 2, without redesigning the core domain.

The core product principle is:

> Completing a lesson or answering practice exercises correctly does not
> automatically mean a student has mastered a concept. Formal
> assessments and subsequent recall evidence inform the system's
> estimate of mastery.

## 2. Learning workflow

1.  The student opens the application and selects a subject.
2.  The application runs an initial diagnostic covering subject concepts
    and relevant prerequisites.
3.  Diagnostic results inform an initial knowledge profile.
4.  The system generates a personalized learning path using the
    subject's concept graph, prerequisites, and the student's estimated
    mastery.
5.  The student works on the next concept and may receive recommended
    practice activities.
6.  Practice activities provide feedback but do not directly constitute
    a formal mastery grade.
7.  The student takes a concept-level assessment before advancing.
8.  Assessment results update the student's estimated mastery and path
    progress. If the student does not meet the configured passing
    threshold, the system recommends reinforcement and another attempt.
9.  Previously studied concepts are reviewed periodically using active
    recall from a question bank (inspired by tools such as Anki).
10. If a review is failed, the system schedules another attempt. If the
    student fails again, the path should be recalculated to return to
    the relevant concept for reinforcement.
11. When the required path is complete, the student takes a global
    subject assessment.
12. The system should support long-term retention: progress through new
    concepts must not mean forgetting old ones.

## 3. Domain vocabulary and agreed definitions

-   **Subject / Course (`Asignatura`)**: An academic subject, such as
    Integral Calculus.
-   **Concept (`Concepto`)**: A topic or skill treated as a unit of
    knowledge for this project. Topics and concepts are considered
    equivalent in the initial version. Examples include Integration
    Rules, Integration Techniques, Definite Integrals, Improper
    Integrals, Area Between Curves, Solids of Revolution, Centers of
    Mass, Arc Length, and Sequences and Series.
-   **Prerequisite (`Prerrequisito`)**: A directed relationship
    indicating that one concept should be mastered before another. A
    concept may have multiple prerequisites and may be a prerequisite
    for multiple concepts.
-   **Practice Activity (`Actividad`)**: An exercise used for practice.
    It has feedback but no formal mastery grade by itself.
-   **Activity Attempt (`Intento de actividad`)**: A student's response
    to a practice activity.
-   **Diagnostic (`Diagnóstico`)**: An initial assessment process that
    collects evidence about a student's existing knowledge, including
    prerequisite knowledge.
-   **Knowledge Profile (`Perfil de conocimiento`)**: The student's
    current knowledge state for a subject.
-   **Concept Mastery (`Dominio conceptual`)**: The system's current
    estimate of the student's mastery of one concept, represented
    initially as a normalized value from 0.0 to 1.0.
-   **Learning Path (`Ruta de aprendizaje`)**: A personalized plan
    generated for a student from the subject's concept graph and the
    student's knowledge state.
-   **Path Step (`Paso de ruta`)**: A concept's status within a
    particular learning path. A concept is static subject content; a
    path step is student/path-specific state.
-   **Concept Assessment (`Evaluación por concepto`)**: A formal
    assessment used to decide whether the student demonstrates
    sufficient aptitude to progress.
-   **Review (`Repaso`)**: A scheduled active-recall session for a
    previously studied concept.
-   **Review Question (`Pregunta de repaso`)**: A reusable question from
    the active-recall question bank.
-   **Review Answer (`Respuesta de repaso`)**: A student's answer to a
    question during a review.
-   **Global Assessment (`Evaluación global`)**: An assessment of the
    subject as a whole, usually after completing the required path.

## 4. Important domain distinctions

### Practice vs. assessment

Practice activities are formative. They return feedback and may record
correctness, but they do not directly decide whether the student can
advance. Concept assessments are formal evidence used for progression
and mastery updates.

### Evidence vs. estimated mastery

An assessment score is evidence (for example, 82/100). Concept mastery
is the system's current estimate (for example, 0.82). These are related
but not identical. Keep the mastery calculation in domain/application
logic, not in SQL constraints or database triggers, so the algorithm can
evolve.

### Knowledge graph vs. personalized path

The subject's concept graph and prerequisite relationships are
relatively static curriculum data. A student's learning path is dynamic
and personal. Do not treat a single integer `position` as a complete
representation of the concept graph. A path may branch, be recalculated,
or revisit a concept.

### Mastery vs. path-step state

A concept's mastery estimate and its state in a particular path are
different. The student may have an estimated mastery score while the
corresponding path step is pending, in progress, in reinforcement, or
completed.

### Learning vs. retention

Mastering a concept once does not guarantee permanent retention. Reviews
should be scheduled and recorded independently from the initial learning
path. Review outcomes can trigger additional reviews and, after repeated
failure, path recalculation.

## 5. Pilot subject: Integral Calculus

Candidate concepts include: - Primitive functions / antiderivatives -
Integration rules - Integration techniques - Fundamental Theorem of
Calculus - Definite integrals - Improper integrals - Area between
curves - Solids of revolution - Centers of mass and centroids - Arc
length - Pappus's centroid theorems - Sequences and series

Likely prerequisites include Differential Calculus and AYT (the meaning
of AYT should be confirmed with the project team). The prerequisite
graph must be validated by a subject-matter expert before it is treated
as authoritative.

Available learning resources include a calculus textbook (James Stewart)
and YouTube. Available assessment formats include quizzes, workshops,
and exams. Comparing student answers may help validate early assessment
behavior.

Do not hard-code the pilot subject into the domain model. Seed data may
include Integral Calculus, but the architecture must be
subject-agnostic.

## 6. Initial conceptual data model

The previously discussed relational model includes the following
entities:

1.  `student`: user identity and account information.
2.  `subject`: academic subject.
3.  `concept`: subject-specific learning unit and difficulty.
4.  `prerequisite`: directed many-to-many relationship between concepts.
5.  `knowledge_profile`: one current profile per student and subject.
6.  `concept_mastery`: current mastery estimate per profile and concept.
7.  `diagnostic`: a diagnostic session and its lifecycle.
8.  `activity`: a practice exercise with feedback.
9.  `activity_concept`: many-to-many mapping between activities and
    concepts.
10. `activity_attempt`: an individual student attempt at an activity.
11. `learning_path`: a personalized path for a student and subject.
12. `path_step`: a concept's state within a path.
13. `concept_assessment`: a formal assessment attempt, score, and
    pass/fail result.
14. `review`: a scheduled active-recall review for a concept.
15. `review_question`: a reusable question linked to a concept.
16. `review_answer`: a student's answer to a question during a review.
17. `global_assessment`: a subject-wide assessment attempt and result.

This is a conceptual starting point, not a frozen schema. Before
implementing every table, review the model against actual use cases. In
particular, the full application will likely need: - Diagnostic
questions and diagnostic answers/results. - Assessment definitions and
assessment questions, rather than only assessment attempt summaries. - A
reliable association between assessment attempts and the path step they
evaluate. - Review scheduling data (for example, next due date,
interval, or scheduling state). - Path versioning/history so
recalculation does not erase previous progress. - Constraints ensuring a
concept and its prerequisites belong to the same subject. - Validation
that prerequisite relationships do not create cycles. - A clear
distinction between a review session and the individual questions
answered in it. - Answer evaluation that can support numeric,
multiple-choice, and open-ended answers as appropriate. - Audit/history
of mastery changes if explaining adaptation decisions is a requirement.

Avoid implementing speculative tables before they support a concrete use
case. Prefer an incremental schema and explicit migrations.

## 7. Suggested initial relational relationships

-   One `subject` has many `concept` records.
-   Concepts can depend on other concepts through `prerequisite`.
-   One `student` has one current `knowledge_profile` per subject.
-   One knowledge profile has many `concept_mastery` records.
-   A student can complete multiple diagnostics for a subject over time.
-   Activities and concepts are many-to-many through `activity_concept`.
-   A student can make many `activity_attempt` records.
-   A student can have multiple learning paths over time, including
    recalculated paths.
-   A learning path has multiple `path_step` records.
-   A student can make multiple concept assessment attempts.
-   A student can have many scheduled reviews.
-   A concept can have many review questions.
-   A review session has multiple review answers.
-   A student can take multiple global assessments over time.

## 8. Adaptation rules (initial product behavior)

The following behavior is required at the product level, but exact
thresholds and mastery mathematics are not yet finalized:

-   Diagnostic results inform the initial mastery profile and path.
-   A concept assessment determines whether the student can progress,
    using a configurable passing threshold.
-   A failed concept assessment leads to reinforcement and a new
    attempt.
-   Passing a concept assessment updates mastery and path progress.
-   Reviews use active recall and are scheduled periodically.
-   A failed review creates a new attempt.
-   If the student fails a review again, recalculate the learning path
    and return to the relevant concept for reinforcement.
-   A global assessment is available after the required learning path is
    complete.
-   Difficulty may influence mastery aggregation, but do not assume
    linear regression is the correct model before validating the desired
    behavior and data.
-   Keep the mastery algorithm replaceable. A simple rule-based
    implementation is acceptable for the MVP if its limitations are
    documented.

## 9. Architecture and engineering principles

The application is intended to use **Hexagonal Architecture (Ports and
Adapters)**. The specific stack has not been confirmed in this brief; do
not silently assume one. If the repository already has a stack, follow
it. Otherwise, ask or propose a minimal, conventional stack before
implementation.

Keep the following boundaries clear:

-   **Domain**: core entities/value objects, invariants, domain
    policies, and domain services. It must not depend on database
    frameworks, web frameworks, or UI code.
-   **Application**: use cases/orchestration, commands/queries, and port
    interfaces required by use cases.
-   **Adapters / Infrastructure**: database persistence, ORM mappings,
    external services, and other implementations of ports.
-   **Inbound adapters**: REST controllers, CLI, or UI entry points that
    invoke application use cases.
-   **Composition/configuration**: dependency wiring and application
    configuration.

Dependencies should point inward. The domain must not import persistence
or web-framework classes. Controllers should not contain business rules.
Repositories should be accessed through application/domain-facing ports,
with implementations in infrastructure.

Use clear names, small modules, explicit interfaces, and tests. Avoid
overengineering, premature microservices, and unnecessary abstractions.
A modular monolith is appropriate for the first iteration unless project
constraints indicate otherwise.

## 10. MVP scope for the first implementation milestone

The first milestone is **project foundation and minimal vertical
slice**, not the complete adaptive learning engine.

Expected deliverables: - A clean, understandable folder/package
structure reflecting Hexagonal Architecture. - A runnable application. -
A health/status endpoint or equivalent minimal entry point. - Initial
domain types for `Subject` and `Concept` (and prerequisite
representation if appropriate). - A minimal use case to list or retrieve
subjects/concepts, using ports and an adapter. - Initial database
connection and one small migration/seed only if the stack is agreed and
database setup is available. - Unit tests for core domain behavior and
at least one application use case. - A README with setup/run/test
instructions and a short architecture explanation. - Configuration
through environment variables; do not commit secrets. - Basic
formatting/linting/build configuration appropriate to the chosen stack.

Do not implement the full diagnostic, adaptive path algorithm,
assessment engine, spaced repetition algorithm, authentication, or all
17 tables in the first milestone unless explicitly requested. Establish
a sound structure and one end-to-end slice first.

## 11. Definition of done for the first milestone

-   The project builds and starts locally using documented commands.
-   The folder structure makes the domain/application/adapter boundaries
    obvious.
-   At least one request/use case traverses the entry adapter,
    application layer, port, and persistence/in-memory adapter.
-   Core logic can be tested without starting the web server or database
    where practical.
-   Automated tests pass.
-   No business logic is embedded in controllers or ORM entities.
-   No secrets or real credentials are committed.
-   The README documents assumptions and deferred decisions.
-   The implementation is small enough for a team to understand and
    extend.

## 12. Open decisions to resolve later

-   Backend language/framework and frontend stack.
-   Authentication provider and account lifecycle.
-   Whether the first MVP needs a frontend or only an API.
-   Assessment/question formats and answer grading strategy.
-   Passing thresholds and mastery update algorithm.
-   Review scheduling algorithm and failure policy details.
-   How to version and recalculate paths without losing history.
-   How to represent branching paths and prerequisite graph validation.
-   Whether and how to persist every mastery change for explainability.
-   What "AYT" means in the prerequisite list.
-   Content authoring and who is allowed to edit concepts, questions,
    and activities.

## 13. Instructions for an AI coding assistant

When working in the repository: 1. Read this document and the existing
repository before making changes. 2. Inspect existing files and preserve
established conventions. 3. Do not generate the entire application at
once. 4. Begin with the project structure and a minimal, runnable
vertical slice. 5. State assumptions and ask only questions that block a
sound implementation. 6. Do not invent requirements or silently settle
major stack decisions. 7. Keep the domain independent of frameworks and
infrastructure. 8. Add tests with each meaningful piece of behavior. 9.
Prefer small, reviewable changes and explain the purpose of new files.
10. Update the README when commands, architecture, or setup change. 11.
Keep deferred functionality explicitly out of scope rather than adding
empty classes for every future entity. 12. Before finishing, report what
was created, how to run it, how to test it, and what remains deferred.
