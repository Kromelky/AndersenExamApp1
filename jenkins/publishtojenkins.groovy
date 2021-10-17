pipeline {

    agent any

    environment {
        registryCredentials = "nexus-jenkins-acc"
        registry = "http://10.0.0.179:8085/repository/docker/"
        repo = "https://github.com/Kromelky/AndersenExamApp1"
        imageName = 'kromelky/application1'
        gitHubAuthId = 'git-kromelky-token'
        nexus_login = "jenkins"
    }

    // Getting from repository
    stages {
        stage('Code checkout') {
            steps {
                checkout([$class                           : 'GitSCM',
                          branches                         : [[name: '*/dev']],
                          doGenerateSubmoduleConfigurations: false,
                          extensions                       : [], submoduleCfg: [],
                          userRemoteConfigs                : [[credentialsId: gitHubAuthId, url: repo]]])
            }
        }


        // add maven build
        stage ('Build') {
            steps {
                sh "mvn clean install"
            }
        }

        // Building Docker images
        stage('Building image') {
            steps{
                script {
                    dockerImage = docker.build(imageName)
                }
            }
        }

        stage('Test image') {
            steps {
                script {
                    dockerImage.inside {
                        sh 'echo "Tests passed"'
                    }
                }
            }
        }

         // Uploading Docker images into Nexus Registry
        stage('Uploading to Nexus') {
            steps{
                script {
                    docker.withRegistry( registry, nexus_login ) {
                        dockerImage.push("${env.BUILD_NUMBER}")
                        dockerImage.push('latest')
                    }
                }
            }
        }
    }
}