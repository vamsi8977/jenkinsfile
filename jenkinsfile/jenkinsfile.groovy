pipeline {
agent any
options {
    buildDiscarder(logRotator(numToKeepStr:'10' , artifactNumToKeepStr: '10'))
    timestamps()
    }
environment {
      inventoryName    = 'Bommasani'
}
  stages {
    stage('CheckOut') {
      steps {
        echo 'Checking out project from Bitbucket....'
        dir("vamsi") {
          git branch: 'master', url: 'git@github.com:vamsi8977/jenkinsfile.git'
        }
      }
    }
stage('SHELL') {
      steps {
        ansiColor('xterm') {
          echo 'Cleaning workspace....'
           sh """
           java -version
           mvn --version
           gradle -version
           ant -version
           python --version
           ansible --version
           git --version
           terraform -v
          """
        }
      }
    }
stage('MAVEN') {
      steps {
        ansiColor('xterm') {
          echo 'Cleaning workspace....'
           sh """
          cd ${WORKSPACE}/vamsi/maven;
          mvn clean install
          """
        }
      }
    }
stage('GRADLE') {
      steps {
        ansiColor('xterm') {
          echo 'Cleaning workspace....'
           sh """
          cd ${WORKSPACE}/vamsi/gradle;
          gradle clean build
          """
        }
      }
    }
stage('ANT') {
      steps {
        ansiColor('xterm') {
          echo 'Cleaning workspace....'
           sh """
          cd ${WORKSPACE}/vamsi/ant;
          ant -buildfile build.xml
          """
        }
      }
    }
  }//end stages
  post {
      success {
          archiveArtifacts artifacts: "vamsi/ant/build/jar/*.jar"
          archiveArtifacts artifacts: "vamsi/gradle/build/libs/*.jar"
          archiveArtifacts artifacts: "vamsi/maven/target/*.war"
      }
      failure {
          echo "The build failed."
          mail to: 'bommasani.vamsikrishna@gmail.com',
                  subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                  body: "Something is wrong with ${env.BUILD_URL}"
      }
  }
}
