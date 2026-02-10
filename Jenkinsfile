pipeline {
    agent any

    tools {
        maven 'Maven-3.9.2'
    }

    environment {
        BUILD_TIMESTAMP = "${new Date().format('yyyy-MM-dd_HH:mm')}"
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
                echo 'My code is deployed'
            }
        }
    }
}
