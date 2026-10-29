"""Offline checks for exact Java release acceptance and Slack status ordering."""

import os
import pathlib
import subprocess
import tempfile
import unittest

import yaml


WORKFLOWS = pathlib.Path(__file__).resolve().parent.parent / ".github" / "workflows"


def workflow(name):
    # PyYAML uses YAML 1.1 and parses the GitHub Actions `on` key as True.
    return yaml.safe_load((WORKFLOWS / name).read_text())


def steps(job):
    return {step["name"]: (i, step) for i, step in enumerate(job["steps"])}


class ReleaseWorkflowTests(unittest.TestCase):
    def test_release_please_has_no_early_announcement(self):
        jobs = workflow("release-please.yml")["jobs"]
        self.assertEqual(set(jobs), {"release-please"})
        self.assertNotIn("SLACK_BOT_TOKEN", str(jobs))

    def test_publisher_keeps_exact_origin_and_public_verification(self):
        release = workflow("publish-maven-central.yml")
        self.assertEqual(release[True]["release"]["types"], ["published"])
        self.assertFalse(release["concurrency"]["cancel-in-progress"])
        publish = release["jobs"]["publish"]
        s = steps(publish)
        self.assertEqual(s["Checkout the exact release tag"][1]["with"]["ref"],
                         "${{ github.event.release.tag_name }}")
        guard = s["Verify the release tag and version"][1]
        for field in ("github.ref", "github.sha", "github.event.release.draft",
                      "github.event.release.prerelease"):
            self.assertIn(field, str(guard["env"]))
        self.assertIn('"$(git rev-parse HEAD)" != "$RELEASE_SHA"', guard["run"])
        self.assertIn("pom_version", guard["run"])
        self.assertLess(s["Build, test, sign, and validate release artifacts"][0],
                        s["Publish and wait for Maven Central"][0])
        self.assertLess(s["Publish and wait for Maven Central"][0],
                        s["Verify the public Maven Central release"][0])
        self.assertIn("verify-maven-central-release.sh",
                      s["Verify the public Maven Central release"][1]["run"])
        self.assertEqual(release["jobs"]["published-e2e"]["needs"], "publish")
        self.assertEqual(release["jobs"]["published-e2e"]["with"]["package-version"],
                         "${{ needs.publish.outputs.version }}")

    def test_one_final_status_requires_registry_and_full_e2e(self):
        release = workflow("publish-maven-central.yml")
        jobs = release["jobs"]
        self.assertEqual(set(jobs) & {"notify-release", "notify-success", "notify-failure"},
                         {"notify-release"})
        notify = jobs["notify-release"]
        self.assertEqual(notify["needs"], ["publish", "published-e2e"])
        self.assertIn("always()", notify["if"])
        s = steps(notify)
        status = s["Prepare one final status and its details"][1]["run"]
        self.assertIn('"$PUBLISH_RESULT" == success && "$PUBLIC_VERIFIED" == true && "$E2E_RESULT" == success', status)
        self.assertIn("published; validation failed or incomplete", status)
        self.assertIn("S03SW7DM8P3", status)
        self.assertIn("RELEASE_PUBLISHED_AT", status)
        self.assertIn("Public registry verification", status)
        self.assertIn("Full E2E delivery and cleanup", status)
        root = s["Post release status root"][1]
        reply = s["Post non-broadcast detail reply"][1]
        for step in (root, reply):
            self.assertEqual(step["with"]["token"], "${{ secrets.SLACK_BOT_TOKEN }}")
            self.assertIn("secrets.SLACK_RELEASE_CHANNEL_ID", step["with"]["payload"])
            self.assertIn('"unfurl_links": false', step["with"]["payload"])
            self.assertIn('"unfurl_media": false', step["with"]["payload"])
        self.assertEqual(root["uses"], reply["uses"])
        self.assertIn("fromJSON(steps.root.outputs.response).ts", reply["with"]["payload"])
        self.assertIn('"reply_broadcast": false', reply["with"]["payload"])

    def test_final_status_words_follow_acceptance_and_upload_state(self):
        run = steps(workflow("publish-maven-central.yml")["jobs"]["notify-release"])[
            "Prepare one final status and its details"][1]["run"]
        cases = (
            ("success", "true", "true", "success", "release complete"),
            ("success", "true", "true", "failure", "published; validation failed or incomplete"),
            ("failure", "true", "false", "skipped", "published; validation failed or incomplete"),
            ("failure", "false", "false", "skipped", "publication failed or incomplete; upload not confirmed"),
        )
        for publish, upload, public, e2e, expected in cases:
            with self.subTest(publish=publish, upload=upload, public=public, e2e=e2e):
                with tempfile.TemporaryDirectory() as directory:
                    output = pathlib.Path(directory) / "output"
                    env = {
                        **os.environ,
                        "GITHUB_OUTPUT": str(output),
                        "TAG_NAME": "v1.2.3",
                        "RELEASE_PUBLISHED_AT": "2026-10-28T12:00:00Z",
                        "RELEASE_URL": "https://example.test/release",
                        "PACKAGE_VERSION": "1.2.3",
                        "CENTRAL_URL": "https://example.test/artifact",
                        "RUN_URL": "https://example.test/run",
                        "PUBLISH_RESULT": publish,
                        "UPLOAD_CONFIRMED": upload,
                        "PUBLIC_VERIFIED": public,
                        "E2E_RESULT": e2e,
                    }
                    subprocess.run(["bash", "-e", "-c", run], env=env, check=True)
                    message = output.read_text()
                    self.assertIn(f"Java SDK v1.2.3 (2026-10-28): {expected}", message)
                    self.assertIn("Public registry verification:", message)
                    self.assertIn("Full E2E delivery and cleanup:", message)

    def test_retry_dispatch_is_exact_and_cannot_publish_or_announce(self):
        retry = workflow("published-sdk-e2e.yml")
        self.assertIn("workflow_dispatch", retry[True])
        self.assertFalse(retry["concurrency"]["cancel-in-progress"])
        job = retry["jobs"]["validate"]
        self.assertNotIn("steps", job)
        self.assertRegex(job["uses"], r"^rudderlabs/github-action-sdk-e2e/\.github/workflows/published-sdk-e2e\.yml@[0-9a-f]{40}$")
        self.assertEqual(job["with"]["package-version"], "${{ inputs.package-version || github.ref_name }}")
        self.assertEqual(job["secrets"], {"RELEASE_PRIVATE_KEY": "${{ secrets.RELEASE_PRIVATE_KEY }}"})
        self.assertNotIn("pull_request", str(retry[True]))
        self.assertNotRegex(str(retry), r"NEXUS_|SIGNING_|central\.autoPublish|\bdeploy\b|SLACK_BOT_TOKEN")


if __name__ == "__main__":
    unittest.main()
