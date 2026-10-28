# Published Java SDK acceptance

The published-package workflow runs the full central E2E suite for the exact stable release version. It uses the existing release App and waits for delivery validation and cleanup. The central workflow checks the release tag against the immutable event commit and `master` ancestry. It installs the exact public registry package and verifies native package metadata and integrity evidence.

Acceptance starts only after the existing publisher verifies the public package. Existing native build, security, and publication checks remain in place. Release Please creates the GitHub release without sending an early Slack announcement. The release workflow posts one final status root with SDK, version, date, and the release mention; its non-broadcast reply carries release, artifact, and run links plus public-verification and E2E results. A completed-release status requires full delivery and cleanup acceptance. If upload completed but acceptance did not, the status says "published; validation failed or incomplete". Slack links do not unfurl.

Full source-candidate E2E before publication is a separate rollout. Do not treat native tests or this post-publication check as that gate.

## Validation-only retry

1. Open the failed central run from the shared action summary.
2. Resolve the validation failure.
3. Run this workflow at the existing release tag:

```sh
gh workflow run published-sdk-e2e.yml --repo rudderlabs/rudder-sdk-java --ref vX.Y.Z
```

The retry has no upload path or publishing credential. It derives the exact version and commit from the selected tag. It does not use `latest`, publish a package, or recreate a tag. Tags predating this workflow need an exact-version dispatch through the central workflow. Stable `X.Y.Z` versions are supported; prerelease acceptance is not implemented.

A published package remains published if acceptance fails. Do not rerun a successful upload job to retry E2E. A validation-only retry does not upload or send another release announcement. Record a successful retry in the release ticket and existing Slack release thread; the original workflow status remains part of the release history.

## Rollout

Tracked in [SDK-5970](https://linear.app/rudderstack/issue/SDK-5970). Keep this PR draft until [central PR #32](https://github.com/rudderlabs/rudder-sdk-e2e-tests/pull/32) resolves [SDK-5978](https://linear.app/rudderstack/issue/SDK-5978) and its cleanup-budget finding, and [SDK-5977](https://linear.app/rudderstack/issue/SDK-5977)'s shared workflow is reviewed and available. Adopt that workflow only after its exact immutable ref and contract are known. The current action pin is an existing commit from [shared-action PR #5](https://github.com/rudderlabs/github-action-sdk-e2e/pull/5); verify or refresh the reviewed, allowlisted SHA before merge. Hosted acceptance remains pending the next normal release. Do not create a release solely to test this wiring.
