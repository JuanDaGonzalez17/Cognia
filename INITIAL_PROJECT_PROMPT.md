# Prompt: Initialize the Adaptive Learning Platform

You are a senior software architect and pragmatic coding assistant. Read
`PROJECT_CONTEXT.md` in full before making changes. Treat it as the
product and architecture context for this repository.

## Goal for this first iteration

Create the **project foundation and one minimal, runnable vertical
slice** for the adaptive learning platform. Do not attempt to implement
the entire product. The pilot subject is Integral Calculus, but the code
must remain subject-agnostic.

## First, inspect before coding

1.  Inspect the current repository, existing files, build tools, and
    conventions.
2.  Check whether the repository already establishes a language,
    framework, database, frontend, or test framework.
3.  If the stack is already established, follow it. If it is not
    established and the choice materially affects the implementation,
    ask me one concise question with a recommended default rather than
    silently selecting a stack.
4.  Summarize the proposed architecture and folder/package structure
    before making large changes. If the repository is empty and the
    stack is clear, proceed with the smallest reasonable implementation.

## Architecture requirements

Use Hexagonal Architecture (Ports and Adapters), preferably as a modular
monolith.

Keep clear boundaries between: - **Domain**: framework-independent
concepts, entities/value objects, invariants, and domain policies. -
**Application**: use cases and port interfaces. - **Inbound adapters**:
HTTP controllers or another entry point. - **Outbound adapters /
infrastructure**: persistence and implementations of ports. -
**Configuration/composition**: dependency wiring and
environment-specific configuration.

Dependency direction must point inward. Domain code must not import web,
ORM, or database-framework classes. Controllers must not contain
business rules. Do not expose ORM entities as domain entities by
default; use explicit mapping where appropriate.

## Minimum first milestone

Implement only enough to prove that the architecture works end to end:

1.  A clean folder/package structure reflecting the boundaries above.
2.  A runnable application with configuration suitable for local
    development.
3.  A simple health/status endpoint (or framework-equivalent).
4.  Minimal domain models for `Subject` and `Concept`. Add a
    prerequisite representation only if it fits cleanly in the first
    slice.
5.  One use case to list subjects or retrieve a subject and its
    concepts.
6.  An application port for retrieving data and one outbound adapter. An
    in-memory adapter is acceptable if database choice/configuration is
    not yet settled; if the repository already has a database stack, use
    its established approach.
7.  A minimal seed/example for Integral Calculus only if appropriate to
    the chosen persistence approach. Keep sample content separate from
    business logic.
8.  Unit tests for domain invariants and the application use case, plus
    a lightweight endpoint/integration test if practical.
9.  A README with prerequisites, setup, run, test, folder structure,
    architecture boundaries, and current limitations.
10. Basic formatting/lint/build configuration appropriate to the
    existing stack.

A single vertical slice should demonstrate the flow:

`HTTP/entry adapter → application use case → output port → adapter → response`

If a web endpoint is not appropriate for the established project,
demonstrate the same dependency flow through the project's existing
entry mechanism.

## Explicitly out of scope for this milestone

Do not implement these yet: - Full student authentication or
authorization. - The complete 17-table relational model. - The
diagnostic questionnaire and scoring engine. - The adaptive
learning-path generation/recalculation algorithm. - Formal
concept-assessment workflows. - Active-recall/spaced-repetition
scheduling. - Global assessments. - A complete frontend. -
Microservices, event sourcing, CQRS infrastructure, or other complexity
without a demonstrated need.

Do not create empty classes/interfaces for every future entity just to
mirror the context document. Only introduce abstractions that are used
by the first vertical slice or are necessary to keep boundaries clean.

## Quality requirements

-   Prefer simple, idiomatic, maintainable code over elaborate
    abstractions.
-   Add tests as behavior is introduced.
-   Use meaningful names and keep methods focused.
-   Validate essential inputs at the appropriate boundary/domain level.
-   Keep configuration outside source code where appropriate.
-   Never commit secrets, credentials, or real personal data.
-   Avoid hard-coding Integral Calculus into core domain rules.
-   Do not claim tests or commands passed unless you actually ran them.
-   If you cannot run a command in the IDE environment, state that
    clearly.
-   Keep changes small and easy for a student team to review.

## Work process

1.  Read `PROJECT_CONTEXT.md`.
2.  Inspect the repository and report the current state briefly.
3.  Present the proposed folder structure and any blocking stack
    question.
4.  Implement the smallest coherent foundation and vertical slice.
5.  Run the relevant formatter, build, and tests that are available.
6.  Fix failures caused by your changes.
7.  Update the README.
8.  Finish with a concise report:
    -   files/folders created or changed;
    -   architecture flow implemented;
    -   commands to run the app and tests;
    -   actual test/build results;
    -   assumptions made;
    -   important work intentionally deferred.

Start by inspecting the repository. Do not begin by generating all
domain entities or all database tables.
