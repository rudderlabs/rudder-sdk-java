# Maven dependency submission

The workflow resolves the parent reactor with the resolver used by GitHub's Maven submission action. It checks every active module and preserves separate `pom.xml` attribution. Test and provided dependencies are not filtered out. JDK 17 is the existing build JDK; the SDK's Java 8 target is unchanged.

Pull requests only validate and upload graphs with read permission. Dependency changes on `master`, weekly refreshes and manual runs also validate. Submission runs only on `master`, after validation. The upstream action combines resolution and submission, so only that trusted default-branch job has `contents: write`. No credentials are persisted by checkout.

The new pinned Maven action requires organization allowlist approval. After merge, inspect its submitted manifests at the source revision. Verify all three modules, direct and indirect dependencies, and test/provided scope handling. Check previously missing packages such as `com.google.guava:failureaccess` and `org.jetbrains.kotlin:kotlin-stdlib-jdk8`.

This route covers the reactor dependency graph. It does not establish complete transitive build-plugin inventory, inactive profiles, or dependencies of arbitrary downstream applications. Keep these limitations visible rather than treating a successful job as complete coverage.

Rollback: remove this workflow. Preserve native alerts and existing Dependabot configuration. Previously submitted snapshots can remain until superseded or removed.
