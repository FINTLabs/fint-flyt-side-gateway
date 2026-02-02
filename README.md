# FINT Flyt Web Gateway Template

## Introduction
This repository is a template for setting up a new *instance gateway* in the FINT Flyt platform.
The application includes examples of both simple and advanced usage:
- **Simple**: processes a single instance without collections and without file upload.
- **Advanced**: demonstrates the use of collections and support for file upload.

## Technology
- Spring Boot 3.5
- Java 21
- Kotlin
- ktlint
- Blocking architecture (non-reactive)

## Dependencies
This project now only needs:
- `fint-flyt-web-instance-gateway`

The `fint-flyt-web-instance-gateway` dependency already includes the other required dependencies.

## ktlint
`ktlint` is used to ensure consistent code formatting and style in the project.

### Using it in IntelliJ
To enable `ktlint` in IntelliJ:
1. Install the `ktlint` plugin via *Settings → Plugins*.
2. Go to *Settings → Tools → ktlint* to configure it.
3. Enable *Distract Free Mode* for a cleaner workspace.
   In this mode, `ktlint` formatting is applied automatically on save.
