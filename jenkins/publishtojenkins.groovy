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
                    docker.withRegistry('http://' + registry, registryCredentials ) {
                        dockerImage.push("${env.BUILD_NUMBER}")
                        dockerImage.push('latest')
                    }
                }
            }
        }

        // Startin
        stage ('Invoke_deployment_pipeline') {
            steps {
                script{
                    try {
                        build job: 'DeployDevApplications', parameters: [
                            string(name: 'env', value: "dev"),
                            string(name: 'image', value: imageName)
                        ]
                    } catch (err) {
                        echo err.getMessage()
                    }
                }
            }
        }
    }
}