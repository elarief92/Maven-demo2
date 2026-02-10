pipeline {
    agent any
    
    tools {
        maven 'maven'
    }
    
    stages {
        stage('code') {
            steps {
                git url: 'https://github.com/elarief92/Maven-demo2.git'
            }
        }
        stage('build') {
            steps {
                sh 'mvn compile'
            }
        }
        stage('test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('artifact') {
            steps {
                sh 'mvn package'
            }
        }
        stage('Artifacts Upload') {
            steps {
			nexusArtifactUploader artifacts: [[artifactId: 'Elarief', classifier: '', file: '/Target/test.war', type: '.war']], credentialsId: 'Access-to-Nexus-Server', groupId: 'org.springframework.boot', nexusUrl: 'localhost:8081', nexusVersion: 'nexus3', protocol: 'http', repository: 'netflix', version: '2.3.0.RELEASE'
            }
        }
        stage('deploy') {
            steps {
                echo "my code is deployed"
            }
        }
    }
}
