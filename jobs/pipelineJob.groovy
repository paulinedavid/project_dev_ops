pipeline {

  agent {
    label 'agent'
  }

  stages {
    stage('Cloning Git') {
    steps {
        retry(3) { // Retry up to 3 times
            deleteDir()
            sh 'sudo chmod 777 /var/run/docker.sock'
            sh 'git config --global --add safe.directory /home/jenkins/workspace/my-pipeline-job'
            git branch: 'main', url: 'https://github.com/paulinedavid/project_dev_ops.git'
        }
    }
}


    stage('Building backend image') {
      steps {
        script {
          dir('webapi') {
            webApiImage = docker.build("paulinedav/devopswebapi")
          }
        }
      }
    }
    stage('Publish backend Image') {
      steps {
        script {
          withDockerRegistry(credentialsId: 'dockerCredentials') {
            webApiImage.push()
          }

        }
      }
    }

    stage('Deploy backend container') {
      steps {
        // Stop previous container
        script {
          sh 'docker stop dev-webapi-container || true'
          sh 'docker rm dev-webapi-container || true'
        }
        // Run the container
        script {
          sh 'docker run -d --name dev-webapi-container -p 8081:8080 paulinedav/devopswebapi'
        }
      }
    }
  }
}