pipeline {

    agent any

    environment {
        imageName = "myphpapp"
        registryCredentials = "jenkins"
        registry = "10.0.0.179:8085/"
        repo = "https://github.com/Kromelky/AndersenExamApp1"
        dockerImage = 'kromelky/application1'
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
                    dockerImage = docker.build imageName
                }
            }
        }

        // Starting image
        stage('Docker Run') {
            steps{
                script {
                    sh 'docker run -d -p 8080:8080 --rm --name imageName ' + registry + imageName
                }
            }
        }

        stage('stop previous containers') {
            steps {
                sh 'docker ps -f name=myphpcontainer -q | xargs --no-run-if-empty docker container stop'
                sh 'docker container ls -a -fname=myphpcontainer -q | xargs -r docker container rm'
            }
        }

         // Uploading Docker images into Nexus Registry
        stage('Uploading to Nexus') {
            steps{
                script {
                    docker.withRegistry( 'http://'+registry, registryCredentials ) {
                        dockerImage.push('latest')
                    }
                }
            }
        }


    }
}