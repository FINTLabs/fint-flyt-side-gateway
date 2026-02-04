# FINT Flyt SiDE Gateway

## Introduction
This repository contains the SiDE instance gateway for the FINT Flyt platform.
The application processes SiDE student instances with collections and document upload support.

## About SiDE
SiDE (Sikker deling av elevinformasjon) is a digital tool for secure journaling and sharing of student information with teachers while preserving privacy. It was developed in Nordland fylkeskommune through cross-sector collaboration and launched at Bodin vgs, with strong support from school leaders and public officials. The goal is better, more systematic student follow-up, improved overview, and more efficient resource use, replacing informal notes and corridor conversations. It has been rolled out to upper-secondary schools in Nordland with positive feedback and interest from other regions.

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
