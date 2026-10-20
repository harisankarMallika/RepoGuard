param([Parameter(Mandatory=$true)][string]$RepositoryPath)
$repo = (Resolve-Path $RepositoryPath).Path
$hookDir = Join-Path $repo '.git\hooks'
if (-not (Test-Path $hookDir)) { throw "Git hooks directory not found: $hookDir" }
$jar = Join-Path (Split-Path $PSScriptRoot -Parent) 'target\repoguard-1.0.0.jar'
if (-not (Test-Path $jar)) { mvn -f (Join-Path (Split-Path $PSScriptRoot -Parent) 'pom.xml') -DskipTests package; if ($LASTEXITCODE -ne 0) { throw 'Maven build failed' } }
$hook = @"
#!/bin/sh
java -jar "$($jar -replace '\\','/')" --scan "`$(git rev-parse --show-toplevel)"
exit `$?
"@
Set-Content -Encoding ascii -Path (Join-Path $hookDir 'pre-commit') -Value $hook
Write-Host "RepoGuard pre-commit hook installed in $repo"
