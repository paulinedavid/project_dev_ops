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
          bat '''
          git config --global --add safe.directory C:\\Users\\me\\.jenkins\\workspace\\pipeline
          git clone -b main https://github.com/paulinedavid/project_dev_ops.git
          '''
        }
      }
    }

    stage('Create Namespaces') {
        steps {
            script {
                echo "Creating namespaces if they don't already exist..."
                bat '''
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
                bat '''
                kubectl apply -f project_dev_ops\\k8s\\development\\deployment.yaml
                kubectl apply -f project_dev_ops\\k8s\\development\\service.yaml
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
                        bat 'start /B kubectl port-forward svc/dev-webapi-service 8081:8080 -n development'
                    }
                }
            }
            stage('Health Check') {
                steps {
                    script {
                        echo "Running post-deployment validation tests..."
                        sleep(200) // Allow some time for the service to be fully up
                        def result = bat(script: "curl -s http://localhost:8081", returnStdout: true).trim()

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
                bat '''
                kubectl apply -f project_dev_ops\\k8s\\production\\deployment.yaml
                kubectl apply -f project_dev_ops\\k8s\\production\\service.yaml
                '''
            }
        }
    }
  }

}
