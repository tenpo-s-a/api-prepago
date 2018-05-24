pipeline {
  agent any
  tools {
    jdk 'Java 8'
    maven 'maven-3.5.3'
  }
  stages {
    stage('Init CI database') {
      steps {
        dir(path: 'pg/') {
          sh './db-init.sh'
          sh './db-create.sh -Denv=jenkins'
        }
      }
    }
    stage('Migrate CI database') {
      steps {
        dir(path: 'pg/') {
          script {
            sh './migrate-bootstrap.sh -Denv=jenkins'
            sh './migrate-status.sh -Denv=jenkins'
            try {
              sh './migrate-pending.sh -Denv=jenkins'
            } catch (Exception e) {
              echo 'Cant execute migrate-pending.sh'
            }
            sh './migrate-up.sh -Denv=jenkins'
            sh './migrate-status.sh -Denv=jenkins'
          }
        }
      }
    }
    stage('Test CI database') {
      steps {
        dir(path: 'pg/') {
          sh './test.sh -Denv=jenkins'
        }
      }
      post {
        always {
          dir(path: 'pg/') {
            junit '**/target/surefire-reports/TEST-*.xml'
          }
        }
      }
    }
    stage('Copy DEV environment') {
      when {
        branch 'master'
      }
      steps {
        dir(path: 'pg/') {
          echo 'Copying pg dev environment'
        }
        dir(path: 'app/') {
          echo 'Copying app dev environment '
        }
      }
    }
    stage('Test API App') {
      steps {
        dir(path: 'app/') {
          sh './test.sh -Denv=jenkins'
        }
      }
      post {
        always {
          dir(path: 'app/') {
            junit '**/target/surefire-reports/TEST-*.xml'
          }
        }
      }
    }
    stage('Archive artifact') {
      when {
        branch 'master'
      }
      steps {
        dir(path: 'app/') {
          archiveArtifacts 'target/*.war'
        }
      }
    }
    stage('Install artifact in maven') {
      when {
        branch 'master'
      }
      steps {
        dir(path: 'app/') {
          sh './install.sh'
        }
      }
    }
    stage('Migrate Dev database') {
      when {
        branch 'master'
      }
      steps {
        dir(path: 'pg/') {
          script {
            sh './migrate-bootstrap.sh -Denv=development'
            sh './migrate-status.sh -Denv=development'
            try {
              sh './migrate-pending.sh -Denv=development'
            } catch (Exception e) {
              echo 'Cant execute migrate-pending.sh'
            }
            sh './migrate-up.sh -Denv=development'
            sh './migrate-status.sh -Denv=development'
          }
        }
      }
    }
    stage('Deploy App') {
      when {
        branch 'master'
      }
      steps {
        dir(path: 'app/') {
          sh './deploy.sh'
        }
      }
    }
    stage('Migrate Kong') {
      when {
        branch 'master'
      }
      steps {
        dir(path: 'kong/') {
          sh './migrate-up.sh -kong_host http://localhost:8001 -api_host http://localhost:8080 -silence true'
        }
      }
    }
    stage('Test kong migrations') {
      when {
        branch 'master'
      }
      steps {
        dir(path: 'kong/') {
          sh './test.sh -Dkong_host=http://localhost:8000'
        }
      }
      post {
        always {
          dir(path: 'kong/') {
            junit '**/target/surefire-reports/TEST-*.xml'
          }
        }
      }
    }
  }
  post {
    success {
      script {
        if(params.BRANCH_NAME != 'master') {
          dir(path: 'pg/') {
            echo 'Deleting CI database'
            sh './db-drop.sh -Denv=jenkins'
          }
        }
      }
    }
    unstable {
      script {
        dir(path: 'pg/') {
          echo 'Deleting CI database'
          sh './db-drop.sh -Denv=jenkins'
        }
      }
    }
    failure {
      script {
        dir(path: 'pg/') {
          echo 'Deleting CI database'
          sh './db-drop.sh -Denv=jenkins'
        }
      }
    }
  }
}
