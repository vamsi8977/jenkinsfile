pipeline {
agent any
tools {
    gradle "Gradle-5.3.1"
  }
parameters {
  choice(choices: '\n-v\n-vv\n-vvv\n-vvvv', description: 'Choose a verbosity level', name: 'VERBOSITY')
  string(name: 'versionnumber',defaultValue: "",description: 'Choose a version number to upload latest version')
}
options {
    buildDiscarder(logRotator(numToKeepStr:'10' , artifactNumToKeepStr: '10'))
    timestamps()
    }
environment {
      inventoryName    = 'sandbox'
}
  stages {
    stage('Checkout') {
      steps {
        echo 'Checking out project from Bitbucket....'
        dir("gradle") {
          git branch: 'develop', url: 'ssh://git@bitbucket.us.comerica.net:7999/dcs/debitcardservices.git'
        }
      }
    }
stage('gradle clean build') {
      steps {
        ansiColor('xterm') {
          echo 'Cleaning workspace....'
           sh """
          cd ${WORKSPACE}/dcs/;
          gradle clean build
          chmod 755 ${WORKSPACE}/dcs/build/libs/*
          cd ${WORKSPACE}/dcs/build/libs/
          ls
          """
        }
      }
    }
    stage('AWS Session') {
      steps {
        awsSession("cma-dev")
      }
    }
    stage('AWS CLI Session') {
      steps {
        script {
          def dockerCommand = """\
            source /workspace/exportAwsSession.sh;
            aws s3 cp /workspace/dcs/build/libs/debitcardservices.jar s3://cma-dev/dcs/ --no-verify-ssl
            """.stripIndent()
          // Call Docker
          callDocker("", "1.2", dockerCommand)
        }
      }
    }
    stage('JFrog Artifactory') {
      steps {
        ansiColor('xterm') {
          echo 'Cleaning workspace....'
           sh """
           curl -v -u deployment:deployment123 --upload-file ${WORKSPACE}/dcs/build/libs/debitcardservices.jar https://artifactory.us.comerica.net/artifactory/Applications/com/comerica/DCS/${params.versionnumber}/debitcardservices-${params.versionnumber}.jar
           curl -v -u deployment:deployment123 --upload-file ${WORKSPACE}/dcs/build/libs/debitcardservices.jar https://artifactory.us.comerica.net/artifactory/Applications/com/comerica/DCS/${params.versionnumber}/debitcardservices.jar
          """
        }
      }
    }
  }//end stages
  post {
      success {
          archiveArtifacts artifacts: "dcs/build/libs/*.jar"
      }
      failure {
          echo "The build failed."
          mail to: 'shaddad@comerica.com vkbommasani@comerica.com ITPDTIM_CardServicesSupport@comerica.com ',
                  subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                  body: "Something is wrong with ${env.BUILD_URL}"
      }
  }
}
