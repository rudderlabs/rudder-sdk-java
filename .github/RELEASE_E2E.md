# Published Java SDK acceptance

The published-package workflow runs the full central E2E suite for the exact stable release version. It uses the existing release App and waits for delivery validation and cleanup. The central workflow checks the release tag against the immutable event commit and `master` ancestry. It installs the exact public registry package and verifies native package metadata and integrity evidence.

Acceptance starts only after the existing publisher verifies the public package. Existing native build, security, and publication checks remain in place. Release Please creates the GitHub release without sending an early Slack announcement. Only the first release-run attempt sends a notification. The release workflow posts one final status root with SDK, version, date, and the release mention; its non-broadcast reply carries release, artifact, and run links plus public-verification and E2E results and the available central validation run. The workflow records its status before Slack and fails if the root or thread reply is not confirmed. A completed-release status requires full delivery and cleanup acceptance. If upload completed but acceptance did not, the status says "published; validation failed or incomplete". Slack links do not unfurl.

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

## Retry after a workflow fix

A tag retry uses workflow code and pins stored at that tag. The retry workflow
must exist on the default branch and selected tag. If a later workflow fix is
needed, use the current central workflow. Copy the full commit SHA from the
original release run. Replace the version, commit placeholder, and request ID:

```sh
gh workflow run server-sdk-validation.yml --repo rudderlabs/rudder-sdk-e2e-tests --ref main \
  -f selection=auto -f source_repository=rudderlabs/rudder-sdk-java \
  -f source_sha=RELEASE_EVENT_COMMIT_SHA -f package_version=X.Y.Z \
  -f suite=full -f request_id=java-published-X_Y_Z-retry-UNIQUE_ID
```

This validates the published version without uploading or announcing. Record the
new evidence in the release ticket and existing release thread. Do not move the
tag. Release-run reruns send no announcement after the first attempt. Repair a
failed notification in the existing release thread separately.

## Rollout

Tracked in [SDK-5970](https://linear.app/rudderstack/issue/SDK-5970). Central
#32/#33/#35 and shared action v1.0.0 are merged. The caller pins the released
workflow at `b6de40cc65abe8ee6c6593aac1787aea746b671f`. Native build, signing,
public artifact verification, and publication checks remain required. Hosted
acceptance remains pending the next normal release. Do not create a release
solely to test this wiring.
