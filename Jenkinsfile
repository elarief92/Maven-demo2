pipeline {
  agent any

  tools {
    maven 'Maven-3.9.2'
  }

  parameters {
    string(name: 'NEW_VERSION', defaultValue: '', description: 'Version like 1.1.1')
  }

  environment {
    BUILD_TIMESTAMP = "${new Date().format('yyyy-MM-dd_HH:mm')}"
    NOTIFY_TO       = "mohamed.elarief30@gmail.com"
    APP_PORT        = "8090"

    // Fixed values
    LOB             = "DXP"
    ENVNAME         = "dev"
    BRANCH          = "Branch2"

    APP_NAME        = "demo1"
    GROUP_PATH      = "com/example"
    NEXUS_BASE      = "http://localhost:8081/repository/netflix"
  }

  stages {

    stage('Validate Version') {
      steps {
        script {
          def v = (params.NEW_VERSION ?: '').trim()
          if (!v) {
            error("NEW_VERSION is required. Example: 1.1.1")
          }
          // Basic SemVer-ish check: digits.digits.digits
          if (!(v ==~ /^\d+\.\d+\.\d+$/)) {
            error("Invalid NEW_VERSION '${v}'. Use format like 1.1.1")
          }

          env.RELEASE_NAME = "R${env.BUILD_NUMBER}-${env.LOB}-v${v}-${env.ENVNAME}"
          currentBuild.displayName = env.RELEASE_NAME
          echo "Build Name: ${env.RELEASE_NAME}"
        }
      }
    }

    stage('Checkout') {
      steps {
        git branch: "${env.BRANCH}", url: 'https://github.com/elarief92/Maven-demo2.git'
      }
    }

    stage('Build + Sonar Scan') {
      steps {
        withSonarQubeEnv('SonarServer-old') {
          withCredentials([string(credentialsId: 'Access-to-SonarQube-Server', variable: 'SONAR_TOKEN')]) {
            sh """
              mvn clean install org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
              -Dsonar.login=\$SONAR_TOKEN \
              -Dsonar.branch.name=${env.BRANCH}
            """
          }
        }
      }
    }

    stage('Wait for Sonar Task') {
      steps {
        sleep(time: 10, unit: 'SECONDS')
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 2, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Package') {
      steps {
        sh 'mvn package'
        sh 'ls -la target'
      }
    }

    stage('Rename Artifact') {
      steps {
        sh '''
          set -e
          JAR=$(ls -1 target/*.jar | head -n 1)
          echo "Original JAR: $JAR"
          mv "$JAR" "target/demo1-${RELEASE_NAME}.jar"
          ls -la target
        '''
      }
    }

    stage('Upload Artifact to Nexus') {
      steps {
        nexusArtifactUploader artifacts: [[
          artifactId: "${env.APP_NAME}",
          classifier: '',
          file: "target/${env.APP_NAME}-${env.RELEASE_NAME}.jar",
          type: 'jar'
        ]],
        credentialsId: 'Access-to-Nexus-Server',
        groupId: 'com.example',
        nexusUrl: 'localhost:8081',
        nexusVersion: 'nexus3',
        protocol: 'http',
        repository: 'netflix',
        version: "${env.RELEASE_NAME}"
      }
    }

    stage('Deploy') {
      steps {
        script {
          def VERSION = "${env.RELEASE_NAME}"
          def ART_URL = "${env.NEXUS_BASE}/${env.GROUP_PATH}/${env.APP_NAME}/${VERSION}/${env.APP_NAME}-${VERSION}.jar"

          withCredentials([usernamePassword(credentialsId: 'Access-to-Nexus-Server', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
            sh """
              set -e

              DEPLOY_DIR=/var/lib/jenkins/apps/${env.APP_NAME}
              mkdir -p \$DEPLOY_DIR

              curl -fL -u "\$NEXUS_USER:\$NEXUS_PASS" -o \$DEPLOY_DIR/${env.APP_NAME}.jar "${ART_URL}"

              if [ -f \$DEPLOY_DIR/${env.APP_NAME}.pid ]; then
                OLD_PID=\$(cat \$DEPLOY_DIR/${env.APP_NAME}.pid || true)
                if [ ! -z "\$OLD_PID" ] && kill -0 "\$OLD_PID" 2>/dev/null; then
                  kill "\$OLD_PID" || true
                  sleep 5
                fi
                rm -f \$DEPLOY_DIR/${env.APP_NAME}.pid
              fi

              cd \$DEPLOY_DIR
              rm -f ${env.APP_NAME}.log
              setsid java -jar \$DEPLOY_DIR/${env.APP_NAME}.jar > \$DEPLOY_DIR/${env.APP_NAME}.log 2>&1 < /dev/null & echo \$! > \$DEPLOY_DIR/${env.APP_NAME}.pid
            """
          }
        }
      }
    }
  }

  post {
    success {
      emailext(
        to: "${env.NOTIFY_TO}",
        mimeType: 'text/plain',
        subject: "SUCCESS | ${env.JOB_NAME} #${env.BUILD_NUMBER} | ${env.BUILD_TIMESTAMP}",
        body: """Build Result: SUCCESS
Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}

Git Branch: ${env.BRANCH}
App: ${env.APP_NAME}
Build Name: ${env.RELEASE_NAME}
App Port: ${env.APP_PORT}
"""
      )
    }

    failure {
      emailext(
        to: "${env.NOTIFY_TO}",
        mimeType: 'text/plain',
        subject: "FAILED | ${env.JOB_NAME} #${env.BUILD_NUMBER} | ${env.BUILD_TIMESTAMP}",
        body: """Build Result: FAILED
Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}

Build Name (if generated): ${env.RELEASE_NAME}
Log Location (if deploy started): /var/lib/jenkins/apps/${env.APP_NAME}/${env.APP_NAME}.log
"""
      )
    }
  }
}
