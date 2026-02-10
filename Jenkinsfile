pipeline {
    agent any

    tools {
        maven 'Maven-3.9.2'
    }

    environment {
        BUILD_TIMESTAMP = "${new Date().format('yyyyMMddHHmmss')}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/elarief92/Maven-demo2.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
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
                version: "2.3.0.RELEASE${BUILD_TIMESTAMP}"
            }
        }

        stage('Deploy') {
            steps {
                echo 'My code is deployed'
            }
        }
    }
}
