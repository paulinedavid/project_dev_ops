pipeline {
  agent any

  stages {
    stage('Cloning Git') {
      steps {
        retry(3) { // Retry up to 3 times
          deleteDir()
          sh '''
          git config --global --add safe.directory ~/.jenkins/workspace/pipeline
          git clone -b kuberntes https://github.com/paulinedavid/project_dev_ops.git
          '''
        }
      }
    }

    stage('Create Namespaces') {
        steps {
            script {
                echo "Creating namespaces if they don't already exist..."
                sh '''
                kubectl get namespace development || kubectl create namespace development
                kubectl get namespace production || kubectl create namespace production
                '''
            }
        }
    }

    stage('Deploy to Development Environment') {
        steps {
            script {
                echo "Deploying to development environment..."

                // Apply Kubernetes manifests for the development environment
                sh '''
                kubectl apply -f project_dev_ops/k8s/development/deployment.yaml
                kubectl apply -f project_dev_ops/k8s/development/service.yaml
                '''
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
                        sh 'nohup kubectl port-forward svc/dev-webapi-service 8081:8080 -n development &> /dev/null &'
                    }
                }
            }
            stage('Health Check') {
                steps {
                    script {
                        echo "Running post-deployment validation tests..."
                        sleep(200) // Allow some time for the service to be fully up
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
                sh '''
                kubectl apply -f project_dev_ops/k8s/production/deployment.yaml
                kubectl apply -f project_dev_ops/k8s/production/service.yaml
                '''
            }
        }
    }
  }

}
