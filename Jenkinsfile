pipeline {
    agent any

    tools {
        maven 'Maven-3.9.2'
    }

    environment {
        BUILD_TIMESTAMP = "${new Date().format('yyyy-MM-dd_HH:mm')}"
        NOTIFY_TO       = "mohamed.elarief30@gmail.com"
        APP_PORT        = "8090"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/elarief92/Maven-demo2.git'
            }
        }

        stage('Build + Sonar Scan') {
            steps {
                withSonarQubeEnv('SonarServer') {
                    withCredentials([string(credentialsId: 'Access-to-SonarQube-Server', variable: 'SONAR_TOKEN')]) {
                        sh '''
                            mvn clean install org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                            -Dsonar.login=$SONAR_TOKEN
                        '''
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
                timeout(time: 1, unit: 'MINUTES') {
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

        stage('Upload Artifact to Nexus') {
            steps {
                nexusArtifactUploader artifacts: [[
                    artifactId: 'demo1',
                    classifier: '',
                    file: 'target/demo1-0.0.1-SNAPSHOT.jar',
                    type: 'jar'
                ]],
                credentialsId: 'Access-to-Nexus-Server',
                groupId: 'com.example',
                nexusUrl: 'localhost:8081',
                nexusVersion: 'nexus3',
                protocol: 'http',
                repository: 'netflix',
                version: "RELEASE${BUILD_TIMESTAMP}"
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def APP_NAME   = "demo1"
                    def GROUP_PATH = "com/example"
                    def VERSION    = "RELEASE${env.BUILD_TIMESTAMP}"
                    def NEXUS_BASE = "http://localhost:8081/repository/netflix"
                    def ART_URL    = "${NEXUS_BASE}/${GROUP_PATH}/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.jar"

                    withCredentials([usernamePassword(credentialsId: 'Access-to-Nexus-Server', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                        sh """
                            set -e

                            echo "Deploying ${APP_NAME} version: ${VERSION}"
                            echo "Artifact URL: ${ART_URL}"

                            DEPLOY_DIR=/var/lib/jenkins/apps/${APP_NAME}
                            mkdir -p \$DEPLOY_DIR

                            curl -fL -u "\$NEXUS_USER:\$NEXUS_PASS" -o \$DEPLOY_DIR/${APP_NAME}.jar "${ART_URL}"

                            if [ -f \$DEPLOY_DIR/${APP_NAME}.pid ]; then
                              OLD_PID=\$(cat \$DEPLOY_DIR/${APP_NAME}.pid || true)
                              if [ ! -z "\$OLD_PID" ] && kill -0 "\$OLD_PID" 2>/dev/null; then
                                echo "Stopping old process \$OLD_PID"
                                kill "\$OLD_PID" || true
                                sleep 5
                              fi
                              rm -f \$DEPLOY_DIR/${APP_NAME}.pid
                            fi

                            cd \$DEPLOY_DIR
                            rm -f ${APP_NAME}.log
                            setsid java -jar \$DEPLOY_DIR/${APP_NAME}.jar > \$DEPLOY_DIR/${APP_NAME}.log 2>&1 < /dev/null & echo \$! > \$DEPLOY_DIR/${APP_NAME}.pid

                            echo "Started new process PID: \$(cat \$DEPLOY_DIR/${APP_NAME}.pid)"
                            echo "Deployment completed successfully"
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

Git Branch: main
App: demo1
Release Version: RELEASE${env.BUILD_TIMESTAMP}

Nexus Repo: netflix
Nexus URL: http://localhost:8081
Deployed Location: /var/lib/jenkins/apps/demo1
App Port: ${env.APP_PORT}

Test URLs:
- http://localhost:${env.APP_PORT}/persons/all
- http://localhost:${env.APP_PORT}/persons/1
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

Please review the Jenkins Console Output for the failure reason.
Log Location (if deploy started): /var/lib/jenkins/apps/demo1/demo1.log
"""
            )
        }

        unstable {
            emailext(
                to: "${env.NOTIFY_TO}",
                mimeType: 'text/plain',
                subject: "UNSTABLE | ${env.JOB_NAME} #${env.BUILD_NUMBER} | ${env.BUILD_TIMESTAMP}",
                body: """Build Result: UNSTABLE
Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}

Check test failures or quality gate warnings in the Jenkins Console Output.
"""
            )
        }
    }
}
