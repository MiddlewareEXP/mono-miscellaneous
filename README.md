# Automated CI/CD Pipeline for Secure Java Application Deployment

This repository contains a Jenkins pipeline configuration for building, testing, and deploying a Java application. The pipeline integrates tools like Maven, SonarQube, OWASP Dependency Check, Nexus, and Tomcat to ensure high-quality and secure deployments.

1. Pipeline Workflow
The pipeline consists of several stages to streamline development and deployment:

2. Initialization
Prints a startup message and sets the environment variables.

3. Git Clone Miscellaneous
Clones the source code from the specified Git repository.

4. Compile Miscellaneous
Compiles the application source code using Maven.

5. SonarQube Code Review
Performs static code analysis to identify code smells, bugs, and technical debt.

6. OWASP Dependency Scan
Scans the project dependencies for known vulnerabilities.

7. Build Miscellaneous
Builds the application and generates the .war artifact.

8. Deploy to Nexus Repository
Uploads the generated artifact to Nexus for centralized storage and version management.

9. Deploy to Application Server
Deploys the application to a Tomcat server, ensuring smooth rollbacks if needed.

# Pipeline Script

The Jenkins pipeline script is written in declarative syntax. Below is the detailed script:

<details> <summary>Click to view the full script</summary>

pipeline {
    agent any

    tools {
        jdk 'Java17'
        maven 'Maven_3_9_9'
    }

    environment {
        mono_miscellaneous_repo = 'https://github.com/SHAJAL987/mono-miscellaneous.git'
        git_credentials_id = 'GIT_ACCESS_000000111222'
        nexus_credentials_id = '391a3d38-676a-42b5-a5ce-6d66a236eb7f'
        nexus_repo_url = '192.168.0.102:8081'
        group_id = 'com.mono'
        nexus_version = 'nexus3'
        nexus_repository = 'monolithic-war-repo'
        nexus_protocol = 'http'
        tomcat_host= '192.168.0.102'
        tomcat_port= '8181'
    }

    stages {
        stage('Initialization') {
            steps {
                echo 'Pipeline has started!'
            }
        }

        stage('Git Clone Miscellaneous') {
            steps {
                dir('miscellaneous') {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: '*/main']],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [],
                        userRemoteConfigs: [[credentialsId: env.git_credentials_id, url: env.mono_miscellaneous_repo]]
                    ])
                }
            }
        }

        stage('Compile Miscellaneous') {
            steps {
                dir('miscellaneous') {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('SonarQube Code Review') {
            steps {
                dir('miscellaneous') {
                    withSonarQubeEnv('sonarserver') {
                        sh 'mvn sonar:sonar -Dsonar.projectKey=miscellaneous_project_key -Dsonar.java.binaries=target/classes'
                    }
                }
            }
        }

        stage('OWASP Dependency Scan') {
            steps {
                dir('miscellaneous') {
                    dependencyCheck additionalArguments: '-s target', nvdCredentialsId: 'ODC_ID', odcInstallation: 'ODC_Installation'
                }
            }
        }

        stage('OWASP Dependency Scan Report') {
            steps {
                dir('miscellaneous') {
                    dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
                }
            }
        }
        
        stage('Build Miscellaneous') {
            steps {
                dir('miscellaneous') {
                    sh 'mvn clean install'
                }
            }
        }

        stage('Deploy to Nexus Repository') {
            steps {
                dir('miscellaneous') {
                    script {
                        pom = readMavenPom file: "pom.xml"
                        filesByGlob = findFiles(glob: "target/*.${pom.packaging}")
                        
                        if (filesByGlob.length > 0) {
                            env.artifactPath = filesByGlob[0].path
                            
                            nexusArtifactUploader artifacts: [
                                [
                                    artifactId: 'miscellaneous', 
                                    classifier: '',          
                                    file: filesByGlob[0].path,  // Use the dynamically fetched artifactPath
                                    type: 'war'
                                ]
                            ], 
                            credentialsId: env.nexus_credentials_id, 
                            groupId: env.group_id, 
                            nexusUrl: env.nexus_repo_url, 
                            nexusVersion: env.nexus_version, 
                            protocol: env.nexus_protocol, 
                            repository: env.nexus_repository, 
                            version: pom.version  // Use the dynamically fetched version
                            
                        } else {
                            error "Artifact file not found in target directory"
                        }
                    }
                }
            }
        }
        
        stage('Deploy to Application Server') {
            steps {
                dir('miscellaneous') {
                    script {
                        def sourceFilePath = "${env.WORKSPACE}/miscellaneous/target/miscellaneous-${pom.version}.war"
                        def renamedFilePath = "${env.WORKSPACE}/miscellaneous/target/app.war"
                        def TOMCAT_HOST = env.tomcat_host
                        def TOMCAT_PORT = env.tomcat_port
                        
                        // Rename the WAR file to miscellaneous.war
                        sh "mv ${sourceFilePath} ${renamedFilePath}"
                        
                        // Use credentials for Tomcat deployment
                        withCredentials([usernamePassword(credentialsId: 'TOMCAT_ID', usernameVariable: 'TOMCAT_USERNAME', passwordVariable: 'TOMCAT_PASSWORD')]) {
                            
                            // Check if miscellaneous.war exists on the Tomcat server
                            def warExists = sh(script: "curl -u ${TOMCAT_USERNAME}:${TOMCAT_PASSWORD} --silent --fail http://${TOMCAT_HOST}:${TOMCAT_PORT}/manager/text/list | grep '/app:'", returnStatus: true)
                            echo "Checking if miscellaneous app exists on Tomcat: ${warExists}"
                            
                            // If miscellaneous exists, undeploy it
                            if (warExists == 0) {
                                sh "curl -u ${TOMCAT_USERNAME}:${TOMCAT_PASSWORD} --silent --request GET http://${TOMCAT_HOST}:${TOMCAT_PORT}/manager/text/undeploy?path=/app"
                                echo "Previous app undeployed successfully"
                            } else {
                                echo "No existing  app found. Proceeding with deployment."
                            }
                            
                            // Deploy the new WAR file
                            sh """
                                curl -u ${TOMCAT_USERNAME}:${TOMCAT_PASSWORD} \
                                    --silent --upload-file ${renamedFilePath} \
                                    http://${TOMCAT_HOST}:${TOMCAT_PORT}/manager/text/deploy?path=/app
                            """
                            echo "New app deployed successfully"
                        }
                    }
                }
            }
        }


    }

    post {
        always {
            cleanWs()
        }
    }
}
</details>

# Prerequisites
To run this pipeline, ensure the following are installed and configured:

1. Jenkins
  Jenkins with Pipeline, Maven, SonarQube, and OWASP Dependency Check plugins.

2. Tools
  Java 17: Set up in Jenkins under Manage Jenkins > Global Tool Configuration.
  Maven 3.9.9: Added to Jenkins tools configuration.

3. Credentials
  Git access credentials (git_credentials_id).
  Nexus credentials (nexus_credentials_id).
  Tomcat credentials (TOMCAT_ID).
  OWASP Dependency Check NVD credentials (ODC_ID).

3. External Tools
  SonarQube Server: Integrated with Jenkins for code quality analysis.
  Nexus Repository Manager: To store and manage artifacts.
  Tomcat Server: To deploy the application.

## Environment Variables

These variables are defined in the pipeline for flexibility:

| Variable               | Description                                            |
|------------------------|--------------------------------------------------------|
| `mono_miscellaneous_repo` | Git repository URL for the application.               |
| `git_credentials_id`   | Jenkins credentials ID for Git access.                 |
| `nexus_credentials_id` | Jenkins credentials ID for Nexus access.               |
| `nexus_repo_url`       | URL of the Nexus repository.                           |
| `group_id`             | Maven group ID for the project.                        |
| `nexus_version`        | Nexus version (e.g., `nexus3`).                        |
| `nexus_repository`     | Nexus repository name (e.g., `monolithic-war-repo`).   |
| `nexus_protocol`       | Protocol to access Nexus (e.g., `http`).               |
| `tomcat_host`          | Hostname of the Tomcat server.                         |
| `tomcat_port`          | Port number of the Tomcat server.                      |


# Setup Instructions

1. Clone the Repository
Clone the source code repository mentioned in the pipeline.
  git clone https://github.com/SHAJAL987/mono-miscellaneous.git

3. Configure Jenkins
  Add the pipeline script in a Jenkins pipeline job.
  Update credentials IDs and environment variables as required.
4. Run the Pipeline
  Trigger the Jenkins pipeline to execute the stages in sequence.

# Key Features

Automation: Automates compilation, testing, and deployment processes.
Quality Assurance: Includes SonarQube for code review and OWASP for security checks.
Centralized Storage: Uploads artifacts to Nexus for better version control.
Seamless Deployment: Ensures smooth application deployment to Tomcat.

# Contributions

Feel free to submit pull requests or open issues to suggest improvements or report bugs.




