pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                 git credentialsId: 'gitub-access-token',
                 branch: 'test',
                 url: 'https://github.com/homealone-front/backend'
            }
        }
        stage('Build') {
            steps {
                echo 'Building...'
                sh 'chmod +x ./gradlew'
                sh './gradlew clean build'
            }
        }
        stage('Archive') {
            steps {
                echo 'Archiving...'
                archiveArtifacts artifacts: '**/build/libs/**/*.jar', fingerprint: true
            }
        }
        stage("Deploy to VM") {
          steps {
            script {
              sshagent(credentials: ['homealone-ec2']) {
                sh 'scp -o StrictHostKeyChecking=no -r build/libs/* ubuntu@home-alone.site:/home/ubuntu/backend'
              }
            }
          }
        }

        stage("Build Docker Image") {
            steps {
                script {
                    // Docker 이미지 빌드
                    sh 'docker build --no-cache -t homealone .'
                }
            }
        }
    }
}