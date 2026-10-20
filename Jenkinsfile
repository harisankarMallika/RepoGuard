pipeline {
  agent any
  triggers {
    githubPush()
    pollSCM('H/5 * * * *')
  }
  tools {
    jdk 'Java17'
    maven 'Maven'
  }
  options { timestamps(); disableConcurrentBuilds() }
  stages {
    stage('Checkout') { steps { checkout scm } }
    stage('Verify') {
      steps {
        script {
          if (isUnix()) sh 'mvn -B clean verify'
          else bat 'mvn -B clean verify'
        }
      }
    }
    stage('Package') { steps { archiveArtifacts artifacts: 'target/repoguard-*.jar', fingerprint: true } }
    stage('Security smoke scan') {
      steps {
        script {
          int result
          if (isUnix()) result = sh(returnStatus: true, script: 'java -jar target/repoguard-1.0.0.jar --scan .')
          else result = bat(returnStatus: true, script: 'java -jar target/repoguard-1.0.0.jar --scan .')
          if (result > 1) error("RepoGuard scanner failed with exit code ${result}")
          if (result == 1) unstable('RepoGuard detected credential patterns in the workspace')
        }
      }
    }
  }
  post { always { junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true } }
}
