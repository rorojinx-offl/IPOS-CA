# AGENTS.md

## Project Overview

- Project: `IPOS-CA`
- Type: Java desktop application built with Gradle
- Purpose: desktop portal for pharmaceutical merchants to manage stock and customers
- Current entry point: `org.novastack.iposca.stock.UIMain`
- UI stack: JavaFX with FXML resources under `src/main/resources/ui`
- Data stack: SQLite via `sqlite-jdbc` and jOOQ

## Repository Layout

- `src/main/java/org/novastack/iposca/stock`
  - `UIMain.java`: JavaFX application entry point
  - `Stock.java`: stock domain model
  - `TestJooq.java`: ad hoc jOOQ usage example, not a JUnit test
- `src/main/java/org/novastack/iposca/utils/db`
  - SQLite connection helpers
  - DDL helpers and schema modeling utilities
- `src/main/java/org/novastack/iposca/utils/ui`
  - shared JavaFX dialog and navigation helpers
- `src/main/java/org/novastack/iposca/utils/db/generated`
  - jOOQ-generated sources from the local SQLite database schema
- `src/main/resources/ui`
  - FXML views and shared dialog assets
- `build.gradle.kts`
  - Gradle build, JavaFX plugin, jOOQ codegen, application main class

## Build And Validation

- Run tests: `./gradlew test`
- Build app: `./gradlew build`
- Run app: `./gradlew run`

Current state:

- `./gradlew test` succeeds
- There are no JUnit test sources yet, so Gradle reports `test NO-SOURCE`

## Database Notes

- The application uses a local SQLite file at `iposca.db`
- jOOQ code generation reads from `jdbc:sqlite:${projectDir}/iposca.db`
- Generated sources are written into `src/main/java/org/novastack/iposca/utils/db/generated`

Agent guidance:

- Treat `src/main/java/org/novastack/iposca/utils/db/generated` as generated code
- Prefer changing schema/codegen inputs instead of editing generated classes by hand
- Be careful with changes that depend on `iposca.db`; local schema drift will affect generated output

## UI Notes

- JavaFX screens are loaded from classpath FXML files
- `UIMain` currently loads `/ui/stock/StockMenu.fxml`
- Shared dialogs live under `/ui/common`
- `CommonCalls` centralizes scene traversal and modal dialog helpers

## Editing Guidance

- Preserve existing package structure under `org.novastack.iposca`
- Prefer small, targeted fixes over broad refactors
- Do not overwrite unrelated user changes already present in the worktree
- Keep JavaFX resource paths aligned with `src/main/resources`
- If touching persistence code, verify assumptions against both SQLite usage and jOOQ usage

## Known Context

- The worktree already contains uncommitted changes in:
  - `src/main/java/org/novastack/iposca/stock/Stock.java`
  - `src/main/java/org/novastack/iposca/stock/TestJooq.java`
- `StockMenu.fxml` is currently a minimal empty `AnchorPane`
- The README is minimal and does not document developer workflow

## Preferred Next Improvements

- Add real JUnit tests
- Add a documented schema/bootstrap flow for `iposca.db`
- Separate example/demo database operations from production application code
- Expand README with setup, run, and architecture notes

## Sessions
- Unless specifically told to, do not try to be an agent and make changes to my code. You will only give responses, for the most part. 