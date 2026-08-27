#!/usr/bin/env bash

set -euo pipefail

version="${1:?usage: verify-release-artifacts.sh <version>}"

scripts/verify-published-poms.sh "$version"

for artifact in analytics-parent analytics-core analytics analytics-sample; do
  case "$artifact" in
    analytics-parent)
      pom=".flattened-pom.xml"
      signature="target/${artifact}-${version}.pom.asc"
      ;;
    *)
      pom="${artifact}/.flattened-pom.xml"
      signature="${artifact}/target/${artifact}-${version}.pom.asc"
      ;;
  esac

  test -s "$signature"
  gpg --batch --verify "$signature" "$pom"
done

for artifact in analytics-core analytics analytics-sample; do
  directory="$artifact/target"

  for classifier in "" -sources -javadoc; do
    archive="$directory/${artifact}-${version}${classifier}.jar"
    test -s "$archive"
    test -s "$archive.asc"
    gpg --batch --verify "$archive.asc" "$archive"
  done
done

sample_archive="analytics-sample/target/analytics-sample-${version}-jar-with-dependencies.jar"
test -s "$sample_archive"
test -s "$sample_archive.asc"
gpg --batch --verify "$sample_archive.asc" "$sample_archive"

echo "Release POMs, archives, and OpenPGP signatures are valid for ${version}."
