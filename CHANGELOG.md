# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.7.1] - 2026-04-20

### Added
- HTML report generation via `report.type=html` or `report.type=HTML`
- Unit tests covering entity, util, and business logic classes
- GNU Lesser General Public License v3 (`LICENSE.md`)
- `pull_request` trigger to verify.yml CI workflow

### Changed
- Updated README with supported SonarQube versions, `SONAR_USER_TOKEN`, `report.type`, and all config properties
- Updated compatibility table: tested on SonarQube 26.4 Community Edition

### Fixed
- Warn and skip report generation when `SONAR_USER_TOKEN` is not set
- Remove deprecated `module()` attribute from `@Property` annotations

## [1.6.7] - 2026-04-20

### Fixed
- Use snapshot copy of `measuresKeys` to avoid `ConcurrentModificationException`

## [1.6.6] - 2026-04-20

### Fixed
- Handle unsupported metric keys gracefully with retry on 404
- Add warning log and improve javadoc for metric fetch error handling

## [1.6.5] - 2026-04-20

### Changed
- Remove unused files and release binaries from repository
- Read bearer token from `SONAR_USER_TOKEN` environment variable; documented in README

## [1.6.4] - 2026-04-20

### Added
- Read bearer token from `SONAR_USER_TOKEN` environment variable; documented in README

## [1.6.3] - 2026-04-20

### Fixed
- Fall back to `api/projects/search` on 403 from `api/components/show`
- Initialize `projectDescription` as empty string to fix NPE

## [1.6.2] - 2026-04-20

### Added
- Split GitHub Actions workflow into separate `verify.yml` and `release.yml` files

### Fixed
- Fix 401 Unauthorized when `SONAR_TOKEN` environment variable is used for authentication

## [1.6.1] - 2026-04-20

### Added
- Dependabot configuration for Maven and GitHub Actions dependency updates

### Fixed
- Fix `NullPointerException` in `Project.getMeasure` when `measures` is null

## [1.6.0] - 2026-04-20

### Added
- GitHub Actions workflow to build and release JAR on version tags
- Initial fork from upstream with SonarQube Community Build v26.4.0.121862 compatibility
- Update for SonarQube API compatibility (period index constant, pom comment)

[Unreleased]: https://github.com/tomaskovacik/sonar-pdf-report/compare/v1.6.7...HEAD
[1.6.7]: https://github.com/tomaskovacik/sonar-pdf-report/compare/v1.6.6...v1.6.7
[1.6.6]: https://github.com/tomaskovacik/sonar-pdf-report/compare/v1.6.5...v1.6.6
[1.6.5]: https://github.com/tomaskovacik/sonar-pdf-report/compare/v1.6.4...v1.6.5
[1.6.4]: https://github.com/tomaskovacik/sonar-pdf-report/compare/v1.6.3...v1.6.4
[1.6.3]: https://github.com/tomaskovacik/sonar-pdf-report/compare/v1.6.2...v1.6.3
[1.6.2]: https://github.com/tomaskovacik/sonar-pdf-report/compare/v1.6.1...v1.6.2
[1.6.1]: https://github.com/tomaskovacik/sonar-pdf-report/compare/v1.6.0...v1.6.1
[1.6.0]: https://github.com/tomaskovacik/sonar-pdf-report/releases/tag/v1.6.0
