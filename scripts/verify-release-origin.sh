#!/usr/bin/env bash
# Validate the immutable release origin; retry may verify a separate tag checkout.
set -euo pipefail

tag="${1:?usage: verify-release-origin.sh <tag> <event-ref> <draft> <prerelease> [expected-commit]}"
event_ref="${2:?missing event ref}"
draft="${3:?missing release draft status}"
prerelease="${4:?missing release prerelease status}"
expected_commit="${5:-}"
release_checkout="${6:-.}"

if [[ ! "$tag" =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ || "$event_ref" != "refs/tags/$tag" ||
      "$draft" != false || "$prerelease" != false ]]; then
  echo "Not a stable, published release for the requested tag and ref: $tag" >&2
  exit 1
fi

commit="$(git -C "$release_checkout" rev-parse "refs/tags/$tag^{commit}")"
if [[ "$(git -C "$release_checkout" rev-parse HEAD)" != "$commit" || ! "$commit" =~ ^[0-9a-f]{40}$ ]]; then
  echo "Checked-out commit does not match the release tag" >&2
  exit 1
fi
if [[ -n "$expected_commit" && "$expected_commit" != "$commit" ]]; then
  echo "Expected release commit does not match the tag tip" >&2
  exit 1
fi

git -C "$release_checkout" merge-base --is-ancestor "$commit" origin/master || {
  echo "Release commit is not in master history" >&2
  exit 1
}

version="${tag#v}"
pom_version="$(mvn --batch-mode --quiet --no-transfer-progress \
  -f "$release_checkout/pom.xml" help:evaluate -Dexpression=revision -DforceStdout)"
if [[ "$pom_version" != "$version" ]]; then
  echo "Tag $tag does not match POM revision $pom_version" >&2
  exit 1
fi

printf 'version=%s\ncommit=%s\n' "$version" "$commit" >> "$GITHUB_OUTPUT"
