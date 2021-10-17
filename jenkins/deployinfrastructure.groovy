pipeline {

    agent any

    environment {
        registryCredentials = "nexus-jenkins-acc"
        registry = "10.0.0.179:8085/"
        repo = "https://github.com/Kromelky/AndersenExamApp1"
        imageName = 'kromelky/application1'
        gitHubAuthId = 'git-kromelky-token'
        nexus_login = "nexus-acc"
    }

    // Getting from repository
    stages {
        stage('Init terraform') {
            steps {
                dir("terraform"){
                    sh "terraform init"
                }
            }
        }

        stage('Plan terraform') {
            steps {
                dir("terraform"){

                    sh "terraform plan"
                }
            }
        }


    }
}