# Java SDK release process

The Java SDK uses Release Please and GitHub Actions. A release requires no local Maven publication commands.

## Prerequisites

The repository must have a GitHub Environment named `maven-central`.

The Environment must:

- Permit only tags matching `v*`.
- Store `NEXUS_USERNAME` and `NEXUS_PASSWORD` as a Central Portal user token.
- Store `SIGNING_KEY_ID` and `SIGNING_PRIVATE_KEY_BASE64` for the organization OpenPGP key.

The Central Portal token must have permission to publish the `com.rudderstack.sdk.java.analytics` namespace.

## Create a release

1. Review the open Release Please pull request.
2. Confirm that its version, changelog, and `pom.xml` revision are correct.
3. Confirm that all required pull request checks pass.
4. Merge the Release Please pull request.

Release Please creates the version tag and GitHub release. The published GitHub release triggers [Publish to Maven Central](../.github/workflows/publish-maven-central.yml).

The Maven Central workflow then:

1. Checks out the exact release tag.
2. Verifies that the tag matches the POM revision and belongs to `master`.
3. Stops duplicate publication when the version already exists.
4. Imports the OpenPGP key from the protected Environment.
5. Runs all tests.
6. Creates concrete consumer POMs, sources, Javadocs, and signatures.
7. Validates every release artifact and signature.
8. Uploads through Sonatype's Central Publishing Maven Plugin.
9. Waits until Central reports `PUBLISHED`.
10. Resolves the public SDK and its dependencies from a clean Maven cache.
11. Sends the final Maven Central result to the release Slack channel.

## Verify a release

The workflow must finish successfully before the release is complete.

Confirm the public artifact at:

```text
https://central.sonatype.com/artifact/com.rudderstack.sdk.java.analytics/analytics/<version>
```

The public POM project and parent version elements must contain concrete versions. They must not contain a literal `${revision}` value.

## Recover from a failure

- If publication did not start, fix the workflow or build and rerun the failed job.
- If Central rejected the deployment, inspect the validation errors before retrying.
- If Central published the version but public verification failed, rerun the workflow. The workflow skips duplicate publication and repeats public verification.
- Never move or recreate a published version tag.
- Never attempt to replace a published Maven Central version. Publish a new patch version instead.

## Rotate credentials

Update credentials only in the `maven-central` GitHub Environment. Do not place credential values in source files, workflow logs, pull requests, or Slack.

After rotation, verify that:

- The Central token still has access to the Java namespace.
- The private key matches `SIGNING_KEY_ID`.
- The private key can sign without interactive input.
- The public key remains available to Maven Central signature validation.
