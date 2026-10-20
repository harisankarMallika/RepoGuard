# RepoGuard

RepoGuard is a local-first Java 17+ security gate for Git commits. It scans source and configuration files for common credential leaks before a commit can leave your machine. It includes a desktop dashboard, a fail-closed CLI, an installable Git pre-commit hook, Jenkins CI, and an Ansible deployment playbook.

## Quick start

```powershell
mvn clean verify
java -jar target/repoguard-1.0.0.jar
java -jar target/repoguard-1.0.0.jar --scan C:\path\to\your\repository
```

The desktop app starts when no arguments are passed. The CLI returns exit code 0 for a clean scan, 1 when findings exist, and 2 on usage or scan errors. Current rules flag AWS access key IDs, private-key headers, GitHub and Slack tokens, likely hard-coded credentials, and selected credential-style environment assignments. `.git`, build output, dependencies, and common binary files are excluded.

## Protect a repository's commits

Run `scripts/install-hook.ps1 -RepositoryPath C:\path\to\repo` on Windows or `bash scripts/install-hook.sh /path/to/repo` on macOS/Linux. The hook builds RepoGuard if necessary and rejects a commit if the scanner finds a match. Keep `repoguard-1.0.0.jar` and Java on the developer's PATH.

## Jenkins

Create a Pipeline job pointing at this repository's `Jenkinsfile`. Configure a GitHub webhook to `https://<jenkins-host>/github-webhook/` for push events and enable the GitHub hook trigger in the job. The Jenkinsfile also polls the SCM every five minutes (`H/5 * * * *`) as a fallback. Provide a Java 17+ and Maven 3 installation named `Maven` or adjust the pipeline tools section.

## Ansible deployment

Build with `mvn clean package`, then run from `ansible/`:

```bash
ansible-playbook -i inventory.ini deploy.yml -e "repo_guard_jar=../target/repoguard-1.0.0.jar"
```

The playbook copies the CLI-capable jar to Linux hosts and installs a systemd unit that scans the configured repository path on service start. Set `repo_guard_repository` and `repo_guard_user` in inventory/group vars. Review the host access and become settings before deployment.

## Security notes

This is a heuristic secret scanner, not a substitute for secret management or a full SAST tool. Patterns can miss novel credential formats and can produce false positives. Findings contain the file, line and rule but never print the matched secret itself. Never place real credentials in the repository, even if a scan passes. The desktop application is local-only and does not transmit repository contents.

## Project map

- `src/main/java`: Java desktop application, scanner and CLI.
- `src/test/java`: scanner unit tests.
- `pom.xml`: Maven build and executable JAR manifest.
- `Jenkinsfile`: webhook and five-minute polling CI pipeline.
- `ansible/`: deployment playbook, defaults and systemd unit.
- `scripts/`: hook installers for Windows and POSIX shells.
- `docs/`: operating and configuration notes.
- `output/pdf/RepoGuard-Project-Guide.pdf`: project UI screenshots and setup/command reference.
