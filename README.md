# Cognia API

Cognia is an adaptive learning platform. This first iteration is a small
Spring Boot vertical slice for browsing subjects and their concepts.

## Prerequisites

- Java 25
- The included Gradle wrapper

## Run

```bash
./gradlew bootRun
```

The API is available at `http://localhost:8080`.

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/subjects
curl http://localhost:8080/api/v1/subjects/integral-calculus
```

## Test and build

```bash
./gradlew test
./gradlew build
```

## Architecture

The application follows Hexagonal Architecture in a modular-monolith
structure:

```text
com.cognia.api
├── domain/model                 # Framework-independent Subject and Concept
├── application/port/in          # Use-case interfaces
├── application/port/out         # Outbound repository ports
├── application/service           # Use-case orchestration
└── adapter
    ├── in/web                    # REST entry adapter and response DTOs
    └── out/persistence            # In-memory repository adapter and seed
```

The request flow is:
`SubjectController -> GetSubjectsUseCase -> SubjectRepository ->
InMemorySubjectRepository -> response DTO`.

Domain records validate their own essential invariants. The controller maps
domain objects to API response DTOs, so HTTP and persistence concerns do not
leak into the domain.

## Current limitations and assumptions

- No database configuration or migration has been established. The first slice
  therefore uses an in-memory adapter and keeps persistence replaceable.
- Integral Calculus is sample data only; the domain and use case are
  subject-agnostic.
- Authentication, diagnostics, mastery calculation, learning-path adaptation,
  assessments, reviews, and the remaining product model are intentionally
  deferred until their concrete use cases and rules are defined.
