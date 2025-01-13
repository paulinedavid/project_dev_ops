pipeline {
  agent {   
    label 'laptop'
  }

  environment {
    KUBECONFIG = "C:\\Users\\me\\.kube\\config" // Ensure kubeconfig is set
  }

  stages {

    stage('Cloning Git') {
      steps {
        retry(3) { // Retry up to 3 times
          deleteDir()
          git branch: 'kuberntes', url: 'https://github.com/paulinedavid/project_dev_ops.git'
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

    stage('Create Namespaces') {
      steps {
        script {
          echo "Creating namespaces if they don't already exist..."
          sh '''
          kubectl create namespace development || echo "Namespace 'development' already exists"
          kubectl create namespace production || echo "Namespace 'production' already exists"
          '''
        }
      }
    }

    stage('Deploy to Development Environment') {
      steps {
        script {
          echo "Deploying to development environment..."

          // Apply Kubernetes manifests for the development environment
          sh '''kubectl apply -f k8s/development/deployment.yaml
          kubectl apply -f k8s/development/service.yaml'''
        }
      }
    }

    // Corrected parallel block within a proper stage
    stage('Post Deployment Validation') {
      parallel {
        stage('Port Forwarding') {
          steps {
            script {
              echo "Running port-forwarding..."
              // Run kubectl port-forward in the background
              sh 'kubectl port-forward svc/dev-webapi-service 8081:8080 -n development &'
            }
          }
        }
        stage('Health Check') {
          steps {
            script {
              echo "Running post-deployment validation tests..."
              sleep(10)  // Allow some time for the service to be fully up
              def result = sh(script: "curl -s http://localhost:8081", returnStdout: true).trim()

              if (result) {
                echo "Service is up and running"
              } else {
                error "Service health check failed"
              }
            }
          }
        }
      }
    }

    stage('Deploy to Production Environment') {
      when {
        expression {
          currentBuild.result == null || currentBuild.result == 'SUCCESS'
        }
      }
      steps {
        script {
          echo "Deploying to production environment..."

          // Apply Kubernetes manifests for the production environment
          sh '''kubectl apply -f k8s/production/deployment.yaml
          kubectl apply -f k8s/production/service.yaml'''
        }
      }
    }
  }
}