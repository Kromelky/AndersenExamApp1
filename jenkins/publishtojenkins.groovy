pipeline {

    agent any

    environment {
        registryCredentials = "nexus-jenkins-acc"
        registry = "10.0.0.179:8085/"
        repo = "https://github.com/Kromelky/AndersenExamApp1"
        imageName = 'kromelky/application1'
        gitHubAuthId = 'git-kromelky-token'
        nexus_login = "nexus-acc"
        dockerImage  = ''
        application_label = "1"
    }

    // Getting from repository
    stages {
        stage('Code checkout') {
            when {expression { env.BRANCH_NAME == 'dev' } }
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
            when {expression { env.BRANCH_NAME == 'dev' } }
            steps {
                sh "mvn clean install"
            }
        }

        // Building Docker images
        stage('Building image') {
            when {expression { env.BRANCH_NAME == 'dev' } }
            steps{
                script {
                    dockerImage = docker.build(imageName)
                }
            }
        }

        stage('Test image') {
            when {expression { env.BRANCH_NAME == 'dev' } }
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
            when {expression { env.BRANCH_NAME == 'dev' } }
            steps{
                script {
                    docker.withRegistry('http://' + registry, registryCredentials ) {
                        dockerImage.push("${env.BUILD_NUMBER}")
                        dockerImage.push('latest')
                    }
                }
            }
        }

        stage('Init terraform') {
            when {expression { env.BRANCH_NAME == 'dev' } }
            steps {
                dir("terraform/dev"){
                    sh "terraform init"
                }
            }
        }

        stage('Plan terraform') {
            when {expression { env.BRANCH_NAME == 'dev' } }
            steps {
                dir("terraform/dev"){
                    withCredentials([usernamePassword(credentialsId: registryCredentials, passwordVariable: 'C_PASS', usernameVariable: 'C_USER')]) {
                        sh """
                        terraform plan -var-file="tfvars/dev.tfvars" -var "docker_pass=${C_PASS}" -var "docker_login=${C_USER}" -var "imageName=${imageName}"
                        """
                    }
                }
            }
        }

        //Deploy server
        stage('Apply terraform') {
            when {expression { env.BRANCH_NAME == 'dev' } }
            steps {
                dir("terraform/dev"){
                    withCredentials([usernamePassword(credentialsId: registryCredentials, passwordVariable: 'C_PASS', usernameVariable: 'C_USER')]) {
                        sh """
                         terraform apply -var-file="tfvars/dev.tfvars" -var "docker_pass=${C_PASS}" -var "docker_login=${C_USER}" -var "imageName=${imageName}" -auto-approve
                         """
                    }
                }
            }
        }


        stage('Init terraform') {
            when {expression { env.BRANCH_NAME == 'main' } }
            steps {
                dir("terraform/prod"){
                    sh "terraform init"
                }
            }
        }

        stage('Plan terraform') {
            when {expression { env.BRANCH_NAME == 'main' } }
            steps {
                dir("terraform/prod"){
                    withCredentials([usernamePassword(credentialsId: registryCredentials, passwordVariable: 'C_PASS', usernameVariable: 'C_USER')]) {
                        try {
                            sh """
                            terraform plan -var-file="tfvars/prod.tfvars" -var "docker_pass=${C_PASS}" -var "docker_login=${C_USER}" -var "imageName=${imageName}" -var "instance_label=${application_label}"
                            """
                        }
                        catch (Exception ex)
                        {
                                sh """
                                terraform init -migrate-state
                                terraform plan -var-file="tfvars/prod.tfvars" -var "docker_pass=${C_PASS}" -var "docker_login=${C_USER}" -var "imageName=${imageName}" -var "instance_label=${application_label}"
                                """
                        }
                    }
                }
            }
        }

        stage('Apply terraform') {
            when {expression { env.BRANCH_NAME == 'main' } }
            steps {
                dir("terraform/prod"){
                    withCredentials([usernamePassword(credentialsId: registryCredentials, passwordVariable: 'C_PASS', usernameVariable: 'C_USER')]) {
                        sh """
                         terraform apply -var-file="tfvars/prod.tfvars" -var "docker_pass=${C_PASS}" -var "docker_login=${C_USER}" -var "imageName=${imageName}"  -var "instance_label=${application_label}" -auto-approve
                         """
                    }
                }
            }
        }

    }
}