pipeline {

  agent {
    label 'agent'
  }

  environment {
    KUBECONFIG = "$HOME/.kube/config" // Ensure kubeconfig is set
  }

  stages {

    stage('Cloning Git') {
      steps {
        retry(3) { // Retry up to 3 times
          deleteDir()
          sh 'sudo chmod 777 /var/run/docker.sock'
          sh 'git config --global --add safe.directory /home/jenkins/workspace/my-pipeline-job'
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

    stage('Validate Deployment in Development') {
      steps {
        script {
          echo "Validating deployment in development environment..."

          // Wait for the pod to be ready
          sh 'kubectl rollout status deployment/dev-webapi -n development'

          // Validate using curl to ensure endpoint is working
          sh '''
          DEV_URL=$(minikube service dev-webapi-service -n development --url)
          echo "Testing development endpoint: $DEV_URL"
          RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" $DEV_URL/health)
          if [ "$RESPONSE" -ne 200 ]; then
            echo "Validation failed with status code: $RESPONSE"
            exit 1
          fi
          '''
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
