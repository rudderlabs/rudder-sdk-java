#!/usr/bin/env bash

set -euo pipefail

version="${1:?usage: verify-published-poms.sh <version>}"

if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Invalid release version: $version" >&2
  exit 1
fi

for artifact in analytics-parent analytics-core analytics analytics-sample; do
  case "$artifact" in
    analytics-parent)
      pom=".flattened-pom.xml"
      ;;
    *)
      pom="${artifact}/.flattened-pom.xml"
      ;;
  esac

  test -s "$pom"

  if grep -Eq '<version>[[:space:]]*\$\{revision\}[[:space:]]*</version>' "$pom"; then
    echo "Unresolved project or parent version in $pom" >&2
    exit 1
  fi

  grep -Fq "<version>${version}</version>" "$pom"
done

for element in name description url licenses developers scm; do
  grep -Fq "<$element>" .flattened-pom.xml
done

echo "Published POMs contain concrete versions and required metadata for ${version}."
