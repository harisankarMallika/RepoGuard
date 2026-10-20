# CI, deployment and Git integration

## Maven conversion
The Maven project uses standard `src/main/java` and `src/test/java` source roots. `pom.xml` sets Java release 17 and builds `target/repoguard-1.0.0.jar` with `com.repoguard.Main` as its entry point. Use `mvn clean verify` to compile, run unit tests and package the app.

## GitHub push
The configured upstream is `https://github.com/harisankarMallika/RepoGuard.git`. Typical commands:

```bash
git init -b main
git add .
git status --short
git commit -m "Build RepoGuard secure commit scanner"
git remote add origin https://github.com/harisankarMallika/RepoGuard.git
git push -u origin main
```

If `origin` already exists, use `git remote set-url origin https://github.com/harisankarMallika/RepoGuard.git`. Authenticate with Git Credential Manager or a scoped GitHub token stored in the credential manager. Do not put tokens in remotes, scripts, or this guide. Inspect `git status --short --branch`, `git log -1 --oneline`, and `git ls-remote --heads origin` to check local and remote state.

## Jenkins configuration
1. Install Jenkins, Git, JDK 17+, Pipeline, GitHub Integration and JUnit plugins.
2. Add Jenkins tools called `Java17` (JDK 17+) and `Maven` (Maven 3.9+), matching `Jenkinsfile`.
3. Create a Pipeline job with SCM source `https://github.com/harisankarMallika/RepoGuard.git`, branch `*/main`, and script path `Jenkinsfile`.
4. Enable the GitHub hook trigger. Set the GitHub repository webhook to `https://<jenkins-host>/github-webhook/`, content type `application/json`, push events.
5. `githubPush()` runs the job on pushed commits. `pollSCM('H/5 * * * *')` checks for missed changes about every five minutes.
6. The job runs Maven verify, archives the JAR and publishes JUnit results. The final smoke scan marks credential findings unstable.

## Ansible deployment
Copy `ansible/inventory.ini.example` to a private `inventory.ini`, add reachable Linux hosts and set `repo_guard_user` and `repo_guard_repository`. Build the JAR, then run:

```bash
mvn clean package
ansible-playbook -i ansible/inventory.ini ansible/deploy.yml -e repo_guard_jar="$(pwd)/target/repoguard-1.0.0.jar"
ansible -i ansible/inventory.ini repoguard -b -m command -a 'systemctl status repoguard.service'
ansible -i ansible/inventory.ini repoguard -b -m command -a 'journalctl -u repoguard.service -n 50 --no-pager'
```

The service runs a scan at service start and exits with a failure status when findings exist. The playbook installs the jar and oneshot systemd unit. For recurring remote scans, schedule a systemd timer or a Jenkins build as an operational extension.

## Status commands

```bash
git status --short --branch
git remote -v
git log -1 --oneline
mvn -version
mvn clean verify
java -jar target/repoguard-1.0.0.jar --scan .
```
