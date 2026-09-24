# Changelog

All notable changes to JobRadar are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-09-24

### Added

- Spring Boot REST API for persisted job search and manual ingestion.
- Arbeitnow integration with source-neutral job normalization.
- PostgreSQL persistence with versioned Flyway migrations.
- Atomic job deduplication based on provider and source identifier.
- Configurable scheduled ingestion with safe disabled-by-default behavior.
- Search by keyword, location, and work model with deterministic pagination.
- Newest-first and relevance-first result ordering.
- Deterministic explainable relevance scoring for data engineering and AI roles.
- Responsive web dashboard with shareable filter state.
- Direct links from result cards to original provider listings.
- PostgreSQL integration tests with Testcontainers.
- GitHub Actions verification for Maven and the complete container stack.
- Multi-stage Java 17 application image running as a non-root user.
- Health-aware Docker Compose stack for JobRadar and PostgreSQL.
- Example environment configuration through `.env.example`.

### Changed

- Provider descriptions are converted from HTML into normalized plain text.
- Provider tags and job types are preserved for search and scoring.
- Local source execution uses explicit PostgreSQL datasource defaults.
- The README documents architecture, API usage, scoring, scheduling, dashboard usage, testing, and container workflows.

### Fixed

- Nested HTML entities no longer appear as literal markup in job descriptions.
- Complete job cards now open the original provider listing.
- The container smoke test verifies the non-root runtime before stack cleanup.

[Unreleased]: https://github.com/saveriobutright/JobRadar/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/saveriobutright/JobRadar/releases/tag/v0.1.0
