# Repository Guidelines

## Project Structure & Module Organization
Application code lives under `src/main/java/org/novastack/iposca`. Core business logic is grouped by feature: `sales/`, `cust/`, `stock/`, `utils/`, and `exceptions/`. JavaFX controllers are kept alongside their feature packages, while FXML views and images live in `src/main/resources/ui/**`. Jasper report templates are in `src/main/resources/jasper/`. jOOQ-generated database classes are checked in under `src/main/java/org/novastack/iposca/utils/db/generated`; treat that directory as generated output and avoid hand-editing it.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repository root:

- `./gradlew run` launches the JavaFX desktop app via `org.novastack.iposca.UIMain`.
- `./gradlew build` compiles the app, runs tests, and assembles deliverables.
- `./gradlew test` runs the JUnit 5 test task.
- `./gradlew jooqCodegen` regenerates jOOQ classes from `iposca.db`.

Run `gradlew.bat` instead of `./gradlew` on Windows.

## Coding Style & Naming Conventions
Follow the existing Java style: 4-space indentation, one public class per file, `PascalCase` for classes/records, `camelCase` for methods and fields, and package names under `org.novastack.iposca`. Keep controller names aligned with their FXML screens, for example `RegistrationController` for `ui/cust/custReg.fxml`. Preserve existing resource naming patterns such as `salesMenu.fxml` and `statement.png`. No formatter or lint task is configured, so keep imports tidy and match surrounding code.

## Testing Guidelines
JUnit 5 is configured in Gradle, but there is currently no `src/test/java` tree. Add new automated tests under `src/test/java` and name them `*Test.java`. Prefer focused unit tests for service and utility classes before relying on manual UI checks. Files under `src/main/java/org/novastack/iposca/utils/common/Test*.java` are developer utilities, not replacement test coverage.

## Commit & Pull Request Guidelines
Recent commits use short imperative summaries such as `Implemented card payment functionality for account holder`. Keep that style, but avoid vague messages like `foo`. Pull requests should include a brief description, affected screens or modules, database or codegen impact, test evidence, and screenshots for FXML/UI changes.

## Database & Generated Code
`iposca.db` is the local SQLite source used for jOOQ generation. When schema changes are required, update the database intentionally, rerun `./gradlew jooqCodegen`, and review the generated diff separately from handwritten logic.

# Sessions
- Unless specifically told to, do not try to be an agent and make changes to my code. You will only give responses, for the most part.
