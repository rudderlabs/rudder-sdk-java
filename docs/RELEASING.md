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
11. Runs the **full published-package E2E suite** for `com.rudderstack.sdk.java.analytics:analytics` at the verified exact version and peeled release-tag commit. The central shared action must wait for matching-version delivery **and cleanup** acceptance, not merely a successful dispatch.
12. Announces release completion only after public verification and full E2E both succeed; otherwise reports incomplete publication or published-but-not-accepted status. No automatic unpublish occurs.

The published-mode central workflow and shared action ([SDK-5961](https://linear.app/rudderstack/issue/SDK-5961), [SDK-5962](https://linear.app/rudderstack/issue/SDK-5962)) must be on main, with a reviewed, organization-allowlisted full action SHA, before this draft change is merged. Until then the E2E caller deliberately fails closed; do not run it for a release. Java candidate-mode E2E is not supported: the existing source build, tests, security, and quality checks remain pre-publication checks, not full candidate E2E.

## Verify a release

Publication, public registry verification, and full published-package E2E acceptance are distinct. The release is complete only when the verified public artifact and the exact-version, exact-commit central run have passed full delivery and cleanup. A dispatch ID or a visible POM alone is insufficient. The release Slack announcement reflects acceptance, not merely upload.

Confirm the public artifact at:

```text
https://central.sonatype.com/artifact/com.rudderstack.sdk.java.analytics/analytics/<version>
```

The public POM project and parent version elements must contain concrete versions. They must not contain a literal `${revision}` value.

## Recover from a failure

- If publication did not start, fix the build/workflow before any new publication attempt. If Central rejected deployment, inspect its validation errors first. Never assume a failed HEAD request means an immutable version is not already public.
- If the version is public but verification or E2E acceptance failed or timed out, **do not rerun the publishing workflow**. Inspect any still-running central E2E run before retrying; live locks and cleanup may still be active.
- On the trusted `master` workflow ref, run [Retry published Java SDK acceptance (no publication)](../.github/workflows/validate-published-maven-central.yml) with the exact existing stable release tag and the full 40-character peeled tag commit SHA. The retry confirms the published GitHub release, non-prerelease metadata, master ancestry, POM revision, and expected commit, then repeats clean-cache public verification and the same full E2E check. It cannot sign, deploy, tag, or choose the latest package.
- A successful retry sends a new release-complete notification; a failed or skipped retry reports incomplete acceptance. The earlier failure notification remains historical, not the final status after a successful retry.
- Never move or recreate a published version tag or replace a published Maven Central version. Publish a new patch version instead.

Hosted live pre/post-publication acceptance for this SDK is deferred to the next normal release. Offline workflow tests are implementation checks only; they do not establish live delivery/cleanup acceptance.

## Rotate credentials

Update credentials only in the `maven-central` GitHub Environment. Do not place credential values in source files, workflow logs, pull requests, or Slack.

After rotation, verify that:

- The Central token still has access to the Java namespace.
- The private key matches `SIGNING_KEY_ID`.
- The private key can sign without interactive input.
- The public key remains available to Maven Central signature validation.
