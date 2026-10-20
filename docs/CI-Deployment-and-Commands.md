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
1. Jenkins is installed as a Windows service and runs locally at `http://localhost:8080/` (controller version 2.578). Git, GitHub, Pipeline, JUnit and Maven support are installed; available plugin security updates were applied and Jenkins restarted.
2. Jenkins Tools defines `Java25` at `C:\Program Files\Java\jdk-25.0.4.1` and `Maven` using the Jenkins Apache Maven installer (3.10.0). Java 25 runs the controller and compiles the Java 17-targeted project.
3. The `RepoGuard` job is a Pipeline from SCM: repository `https://github.com/harisankarMallika/RepoGuard.git`, branch `*/main`, script path `Jenkinsfile`, lightweight checkout enabled, no credentials required for this public repository.
4. The job has both `GitHub hook trigger for GITScm polling` and `Poll SCM` enabled. Its poll schedule is `H/5 * * * *`; the Jenkinsfile also declares `pollSCM('H/5 * * * *')`.
5. No public GitHub webhook is configured because this controller is local-only. SCM polling detects pushes within about five minutes. For immediate webhook builds, host Jenkins at a secured, publicly reachable HTTPS address and configure the repository webhook at `/github-webhook/` for push events.
6. Jenkins build #4 succeeded on commit `2ca9e1d`: Maven clean verify passed all 3 tests, the scanner returned no findings, and `target/repoguard-1.0.0.jar` was archived. The initial setup build failed before the Java tool name was aligned; the corrected builds passed.
7. The job runs Maven verify, archives the JAR and publishes JUnit results. The final smoke scan marks credential findings unstable.

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
