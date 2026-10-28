"""Offline guards for the published Java SDK release and validation-only retry."""
import os
import pathlib
import subprocess
import tempfile
import unittest

import yaml

ROOT = pathlib.Path(__file__).resolve().parent.parent
WORKFLOWS = ROOT / ".github" / "workflows"


def load(name):
    # PyYAML treats `on` as a YAML 1.1 boolean; job/step fields are unaffected.
    return yaml.safe_load((WORKFLOWS / name).read_text())


def steps(job):
    return {step["name"]: (index, step) for index, step in enumerate(job["steps"])}


def origin(tag="v1.2.3", event_ref="refs/tags/v1.2.3", draft="false",
           prerelease="false", expected=None, revision="1.2.3", separate_checkout=False):
    with tempfile.TemporaryDirectory() as directory:
        root = pathlib.Path(directory)
        subprocess.run(["git", "init", "-q", str(root)], check=True)
        (root / "pom.xml").write_text("<revision>" + revision + "</revision>")
        subprocess.run(["git", "-C", str(root), "add", "pom.xml"], check=True)
        subprocess.run(["git", "-C", str(root), "-c", "user.name=test", "-c",
                        "user.email=test@example.com", "commit", "-qm", "test"], check=True)
        subprocess.run(["git", "-C", str(root), "-c", "tag.gpgSign=false", "tag", tag], check=True)
        commit = subprocess.check_output(["git", "-C", str(root), "rev-parse", "HEAD"], text=True).strip()
        subprocess.run(["git", "-C", str(root), "update-ref", "refs/remotes/origin/master", commit], check=True)
        fake_mvn = root / "mvn"
        fake_mvn.write_text("#!/bin/sh\nprintf '%s' \"$TEST_REVISION\"\n")
        fake_mvn.chmod(0o755)
        output = root / "output"
        checkout = root / "release" if separate_checkout else root
        if separate_checkout:
            checkout.symlink_to(root, target_is_directory=True)
        result = subprocess.run(["bash", str(ROOT / "scripts/verify-release-origin.sh"),
                                 tag, event_ref, draft, prerelease, expected or commit, str(checkout)],
                                cwd=root, text=True, capture_output=True,
                                env={**os.environ, "PATH": f"{root}:{os.environ['PATH']}",
                                     "TEST_REVISION": revision, "GITHUB_OUTPUT": str(output)})
        return result.returncode, output.read_text() if output.exists() else "", commit


class ReleaseWorkflowTests(unittest.TestCase):
    def test_origin_verifier_rejects_prerelease_wrong_event_version_and_commit(self):
        code, output, commit = origin()
        self.assertEqual(code, 0)
        self.assertEqual(output, f"version=1.2.3\ncommit={commit}\n")
        self.assertEqual(origin(separate_checkout=True)[0], 0)
        for kwargs in ({"prerelease": "true"}, {"draft": "true"},
                       {"event_ref": "refs/tags/v9.9.9"}, {"revision": "9.9.9"},
                       {"expected": "0" * 40}, {"tag": "v1.2.3-rc.1"}):
            with self.subTest(kwargs=kwargs):
                self.assertNotEqual(origin(**kwargs)[0], 0)

    def test_automatic_event_and_order(self):
        workflow = load("publish-maven-central.yml")
        self.assertEqual(workflow[True]["release"]["types"], ["published"])
        self.assertFalse(workflow["concurrency"]["cancel-in-progress"])
        publish = workflow["jobs"]["publish"]
        s = steps(publish)
        self.assertIn("github.event.release.tag_name", s["Checkout the exact release tag"][1]["with"]["ref"])
        guard = s["Verify the release tag and version"][1]
        for field in ("github.ref", "github.event.release.draft", "github.event.release.prerelease"):
            self.assertIn(field, str(guard["env"]))
        self.assertIn("verify-release-origin.sh", guard["run"])
        self.assertLess(s["Verify the release tag and version"][0], s["Publish and wait for Maven Central"][0])
        self.assertLess(s["Publish and wait for Maven Central"][0], s["Verify the public Maven Central release"][0])
        self.assertIn("verify-maven-central-release.sh", s["Verify the public Maven Central release"][1]["run"])
        self.assertEqual(publish["outputs"]["version"], "${{ steps.release.outputs.version }}")
        self.assertEqual(publish["outputs"]["commit"], "${{ steps.release.outputs.commit }}")
        self.assertNotIn("RELEASE_PRIVATE_KEY", str(publish))
        self.assertEqual(workflow["jobs"]["published-e2e"]["needs"], ["publish"])
        self.assertEqual(workflow["jobs"]["published-e2e"]["with"], {
            "version": "${{ needs.publish.outputs.version }}",
            "source-sha": "${{ needs.publish.outputs.commit }}"})
        self.assertIn("published-e2e.result == 'success'", workflow["jobs"]["notify-success"]["if"])
        self.assertIn("published-e2e.result != 'success'", workflow["jobs"]["notify-failure"]["if"])
        self.assertIn("published", workflow["jobs"]["notify-failure"]["env"]["PUBLICATION_CONFIRMED"])
        self.assertEqual(publish["outputs"]["public-verified"],
                         "${{ steps.public-verified.outcome == 'success' }}")
        self.assertIn("PUBLIC_VERIFIED", steps(workflow["jobs"]["notify-failure"])["Select failure status"][1]["run"])

    def test_retry_is_only_validation_on_master(self):
        workflow = load("validate-published-maven-central.yml")
        self.assertEqual(set(workflow[True]["workflow_dispatch"]["inputs"]), {"tag", "expected-commit"})
        self.assertFalse(workflow["concurrency"]["cancel-in-progress"])
        self.assertIn("inputs.tag", workflow["concurrency"]["group"])
        jobs = workflow["jobs"]
        self.assertIn("refs/heads/master", jobs["verify-public"]["if"])
        s = steps(jobs["verify-public"])
        self.assertIn("inputs.tag", s["Check out exact release tag"][1]["with"]["ref"])
        self.assertEqual(s["Check out exact release tag"][1]["with"]["path"], "release")
        self.assertLess(s["Check out trusted master workflow scripts"][0],
                        s["Check out exact release tag"][0])
        check = s["Confirm published GitHub release and tag tip"][1]
        self.assertIn("releases/tags/$TAG_NAME", check["run"])
        self.assertIn("published_at", check["run"])
        self.assertIn("verify-release-origin.sh", check["run"])
        self.assertIn('"$EXPECTED_COMMIT" release', check["run"])
        self.assertLess(s["Confirm published GitHub release and tag tip"][0],
                        s["Verify the exact public Maven Central release"][0])
        self.assertEqual(jobs["published-e2e"]["needs"], ["verify-public"])
        self.assertEqual(jobs["published-e2e"]["with"]["version"],
                         "${{ needs.verify-public.outputs.version }}")
        self.assertEqual(jobs["published-e2e"]["with"]["source-sha"],
                         "${{ needs.verify-public.outputs.commit }}")
        for name, condition in (("notify-success", "published-e2e.result == 'success'"),
                                ("notify-failure", "published-e2e.result != 'success'")):
            self.assertIn(condition, jobs[name]["if"])
            self.assertIn("verify-public.result", jobs[name]["if"])
        self.assertNotRegex(str(workflow), r"\bdeploy\b|SIGNING_|NEXUS_|central\.autoPublish")

    def test_shared_caller_is_full_and_fail_closed_until_dependency_lands(self):
        workflow = load("published-package-e2e.yml")
        self.assertEqual(set(workflow[True]["workflow_call"]["inputs"]), {"version", "source-sha"})
        job = workflow["jobs"]["accept"]
        self.assertIn("refs/heads/master", job["if"])
        self.assertNotIn("pull_request", job["if"])
        s = steps(job)
        self.assertEqual(s["Dispatch and wait for full published-package E2E"][1]["with"]["suite"], "full")
        self.assertEqual(s["Dispatch and wait for full published-package E2E"][1]["with"]["package-version"],
                         "${{ inputs.version }}")
        self.assertEqual(s["Dispatch and wait for full published-package E2E"][1]["with"]["source-sha"],
                         "${{ inputs.source-sha }}")
        self.assertLess(s["Require published-mode action dependency"][0],
                        s["Create central validation token"][0])
        self.assertIn("exit 1", s["Require published-mode action dependency"][1]["run"])
        token = s["Create central validation token"][1]["with"]
        self.assertEqual(token["repositories"], "rudder-sdk-e2e-tests")
        self.assertEqual(token["permission-actions"], "write")
        self.assertNotIn("permission-contents", token)
        self.assertNotIn("pull_request", str(load("validate-published-maven-central.yml")[True]))


if __name__ == "__main__":
    unittest.main()
