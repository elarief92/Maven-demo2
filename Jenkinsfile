pipeline {
    agent any

    tools {
        maven 'Maven-3.9.2'
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
                nexusArtifactUploader artifacts: [[artifactId: 'spring-boot-starter-parent', classifier: '', file: 'target/test.war', type: 'war']],
                    credentialsId: 'Access-to-Nexus-Server',
                    groupId: 'org.springframework.boot',
                    nexusUrl: 'localhost:8081',
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    repository: 'netflix',
                    version: '2.3.0.RELEASE'
            }
        }

        stage('Deploy') {
            steps {
                echo 'My code is deployed'
            }
        }
    }
}
