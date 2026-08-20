#!/usr/bin/env bash

set -euo pipefail

version="${1:?usage: verify-maven-central-release.sh <version>}"

if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Invalid release version: $version" >&2
  exit 1
fi

group_path="com/rudderstack/sdk/java/analytics"
central_url="https://repo1.maven.org/maven2"
verification_directory="$(mktemp -d)"
local_repository="$(mktemp -d)"
trap 'rm -rf "$verification_directory" "$local_repository"' EXIT

artifacts=(analytics-parent analytics-core analytics analytics-sample)

for attempt in $(seq 1 45); do
  available=true

  for artifact in "${artifacts[@]}"; do
    pom_url="${central_url}/${group_path}/${artifact}/${version}/${artifact}-${version}.pom"

    if ! curl --fail --silent --show-error --location \
      --output "${verification_directory}/${artifact}.pom" "$pom_url"; then
      available=false
      break
    fi
  done

  if [[ "$available" == true ]]; then
    break
  fi

  if [[ "$attempt" == 45 ]]; then
    echo "Release ${version} did not become available on Maven Central." >&2
    exit 1
  fi

  echo "Waiting for Maven Central propagation (${attempt}/45)..."
  sleep 20
done

for artifact in "${artifacts[@]}"; do
  pom="${verification_directory}/${artifact}.pom"

  if grep -Eq '<version>[[:space:]]*\$\{revision\}[[:space:]]*</version>' "$pom"; then
    echo "Published POM contains an unresolved project or parent version: $artifact" >&2
    exit 1
  fi

  grep -Fq "<version>${version}</version>" "$pom"
done

analytics_jar="${verification_directory}/analytics-${version}.jar"
curl --fail --silent --show-error --location \
  --output "$analytics_jar" \
  "${central_url}/${group_path}/analytics/${version}/analytics-${version}.jar"
jar tf "$analytics_jar" | grep -Fq 'com/rudderstack/sdk/java/analytics/RudderAnalytics.class'

mvn --batch-mode --quiet --no-transfer-progress \
  -Dmaven.repo.local="$local_repository" \
  -Dartifact="com.rudderstack.sdk.java.analytics:analytics:${version}" \
  -Dtransitive=true \
  -DremoteRepositories="central::default::${central_url}" \
  org.apache.maven.plugins:maven-dependency-plugin:3.11.0:get

echo "Maven Central release ${version} is public and consumable from a clean cache."
