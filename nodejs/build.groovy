pipeline {
agent any
options {
    buildDiscarder(logRotator(numToKeepStr:'2' , artifactNumToKeepStr: '2'))
    timestamps()
    }
  stages {
    stage('CheckOut') {
      steps {
        echo 'Checking out project from Bitbucket....'
          git branch: 'main', url: 'git@github.com:vamsi8977/nodejs_sample.git'
      }
    }
stage('build') {
      steps {
        ansiColor('xterm') {
          echo 'NPM Build....'
           sh '''
            npm install
            npm test
            '''
        }
      }
    }
stage('SonarQube') {
    steps {
        withSonarQubeEnv('SonarQube') {
            sh "sonar-scanner"
        }
    }
}
  }//end stages
post {
      success {
          echo "The build passed."
      }
      failure {
          echo "The build failed."
      }
      cleanup{
        deleteDir()
      }
    }
}