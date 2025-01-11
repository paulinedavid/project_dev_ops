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
            webApiImage = docker.build("hugopanel/devopswebapi")
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
        echo "Run  backend conatiner "
      }
    }
  }
}
