# CI/CD Examples

[中文](CI_CD_EXAMPLES.zh-CN.md)

This repository intentionally ships **example** CI/CD material only. Teams can translate the same
stages to GitHub Actions, GitLab CI, Jenkins, or an internal platform.

## Recommended baseline

1. Run `./gradlew assemble test --no-daemon` on every pull request.
2. Run `./gradlew build --no-daemon` on the protected branch once quality gates are green for the
   repository.
3. Build the app-specific container image only for the deployable app you are releasing
   (`apps:admin-api`, `apps:mobile-api`, or `apps:migrator`).
4. Apply Flyway via `apps:migrator` before promoting the API deployment.

## Sample workflow

See [docs/examples/ci/github-actions-ci.yml](examples/ci/github-actions-ci.yml) for a concrete
GitHub Actions example. Treat it as a translation sample, not as a required platform choice.

## What the sample demonstrates

- Gradle dependency caching
- PR pipeline with `assemble test`
- Main-branch pipeline with `build`
- App-specific image build steps
- A separate migrator step before API rollout
