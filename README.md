# FINT Flyt SiDE Gateway

## Introduction
This repository contains the SiDE instance gateway for the FINT Flyt platform.
The application processes SiDE student instances with collections and document upload support.

## About SiDE
SiDE (Sikker deling av elevinformasjon) is a digital tool for secure journaling and sharing of student information with teachers while preserving privacy. It was developed in Nordland fylkeskommune through cross-sector collaboration and launched at Bodin vgs, with strong support from school leaders and public officials. The goal is better, more systematic student follow-up, improved overview, and more efficient resource use, replacing informal notes and corridor conversations. It has been rolled out to upper-secondary schools in Nordland with positive feedback and interest from other regions.

## Technology
- Spring Boot 3.5
- Java 25
- Kotlin
- ktlint
- Blocking architecture (non-reactive)

## Dependencies
This project now only needs:
- `fint-flyt-gateway-starter`

The `fint-flyt-gateway-starter` dependency already includes the other required dependencies.

## ktlint
`ktlint` is used to ensure consistent code formatting and style in the project.

### Using it in IntelliJ
To enable `ktlint` in IntelliJ:
1. Install the `ktlint` plugin via *Settings → Plugins*.
2. Go to *Settings → Tools → ktlint* to configure it.
3. Enable *Distract Free Mode* for a cleaner workspace.
   In this mode, `ktlint` formatting is applied automatically on save.

## Running Locally

Start Kafka on `localhost:9092` with `docker compose up -d`, then run the app with `./gradlew bootRun --args='--spring.profiles.active=local-staging'`. Add `--profile tools` to also start Kafdrop on http://localhost:19000. Kafka topics are empty on every start.

## Dependency Locking

The build uses [Gradle dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html), so every build resolves exactly the same direct and transitive versions. The locked versions live in the committed `gradle.lockfile` files. A pull request therefore shows every transitive version change it causes. Gradle plugins are not locked, because Dependabot only updates `gradle.lockfile`; their versions are pinned in the `plugins` block instead.

**Automatic (Dependabot):** Dependabot updates the lockfiles in its own pull requests, so no manual step is needed. If two Dependabot pull requests conflict in a lockfile, Dependabot rebases the remaining one after the first is merged. If it does not, comment `@dependabot recreate` on the pull request.

**Manual:** Any change you make by hand that affects dependency resolution requires regenerating the lockfiles. That covers versions, `extra[...]` properties, constraints, and adding or removing a dependency:

```bash
./gradlew dependencies --write-locks
```

Commit the updated lockfiles together with the change. If you forget, the build fails both locally and in CI with an error saying the resolved version "is not part of the dependency lock state".
