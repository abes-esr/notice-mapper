//this is the scripted method with groovy engine
/*
Ce Jenkinsfile permet de compiler et de deployer la partie web et/ou la partie batch du projet.
sur les environnements de DEV, TEST et PROD
Ce script se veut le plus generique possible et comporte 2 zones a editer specifiquement au projet
 */
import hudson.model.Result

node {

    // **** DEBUT DE ZONE A EDITER n°1 ****

    /*
    Cette zone contient la definition de la structure interne du projet.
    Le tableau modulesNames contient les modules concernes par la compilation et le deploiement.
    Dans cette exemple, il s'agit des modules 'web' et 'batch'
    Les variables de definition des modules (backTargetDir, backServiceName,...)
    permettent de renseigner les specifications sur les environements cibles.
     */

    // Configuration du projet
    def gitURL = "https://github.com/abes-esr/notice-mapper.git"
    def gitCredentials = 'Github'
    def slackChannel = "#notif-periscope"
    def artifactoryBuildName = "biblioModel"

    // **** FIN DE ZONE A EDITER n°1 ****

    // Variables de configuration d'execution
    def candidateModules = []
    def executeBuild = []
    def executeTests = false
    def deployArtifactoy = false
    def buildNumber = -1
    def executeDeploy = []

    // Variables globales
    def ENV
    def maventool
    def rtMaven
    def artifactoryServer
    def downloadSpec

    // Definition des actions
    def choiceParams = ['Compiler', 'Compiler & Déployer', 'Déployer un précédent build']

    // Configuration du job Jenkins
    // On garde les 5 derniers builds par branche
    // On scanne les branches et les tags du Git
    properties([
            buildDiscarder(
                    logRotator(
                            artifactDaysToKeepStr: '',
                            artifactNumToKeepStr: '',
                            daysToKeepStr: '',
                            numToKeepStr: '5')
            ),
            parameters([
                    choice(choices: choiceParams.join('\n'), description: 'Que voulez-vous faire ?', name: 'ACTION'),
                    gitParameter(
                            branch: '',
                            branchFilter: 'origin/(.*)',
                            defaultValue: 'develop',
                            description: 'Sélectionner la branche ou le tag',
                            name: 'BRANCH_TAG',
                            quickFilterEnabled: false,
                            selectedValue: 'NONE',
                            sortMode: 'DESCENDING_SMART',
                            tagFilter: '*',
                            type: 'PT_BRANCH_TAG'),
                    stringParam(defaultValue: '', description: "Numéro du build à déployer. Retrouvez vos précédents builds sur https://artifactory.abes.fr/artifactory/webapp/#/builds/${artifactoryBuildName}", name: 'BUILD_NUMBER'),
                    booleanParam(defaultValue: false, description: 'Voulez-vous deployer sur Artifactory ?', name: 'deployArtifactoy'),
                    booleanParam(defaultValue: false, description: 'Voulez-vous exécuter les tests ?', name: 'executeTests')
            ])
    ])

    //-------------------------------
    // Etape 1 : Configuration
    //-------------------------------
    stage('Set environnement variables') {
        try {
            // Java
            env.JAVA_HOME = "${tool 'Open JDK 11'}"
            env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

            // Maven & Artifactory
            maventool = tool 'Maven 3.3.9'
            rtMaven = Artifactory.newMavenBuild()
            artifactoryServer = Artifactory.server '-1137809952@1458918089773'
            rtMaven.tool = 'Maven 3.3.9'
            rtMaven.opts = "-Xms1024m -Xmx4096m"

            // Action a faire
            if (params.ACTION == null) {
                throw new Exception("Variable ACTION is null")
            }

            if (params.ACTION == 'Compiler') {
                candidateModules.add("")
                executeBuild.add(true)
                executeDeploy.add(false)
            } else if (params.ACTION == 'Compiler & Déployer') {
                candidateModules.add("")
                executeBuild.add(true)
                executeDeploy.add(true)
            } else if (params.ACTION == "Déployer un précédent build") {

                if (params.BUILD_NUMBER == null || params.BUILD_NUMBER == -1) {
                    throw new Exception("No build number specified")
                }
                buildNumber = params.BUILD_NUMBER
                candidateModules.add("")
                executeBuild.add(false)
                executeDeploy.add(true)
            }

            if (candidateModules.size() == 0) {
                throw new Exception("Unable to decode variable ACTION")
            }

            // Branche a deployer
            if (params.BRANCH_TAG == null) {
                throw new Exception("Variable BRANCH_TAG is null")
            } else {
                echo "Branch to deploy =  ${params.BRANCH_TAG}"
            }

            // Booleen d'execution des tests
            if (params.executeTests == null) {
                executeTests = false
            } else {
                executeTests = params.executeTests
            }
            echo "executeTests =  ${executeTests}"

            // Booleen de deploiement sur Artifactory
            if (params.deployArtifactoy == null) {
                deployArtifactoy = false
            } else {
                deployArtifactoy = params.deployArtifactoy
            }
            echo "deployArtifactoy =  ${deployArtifactoy}"

        } catch (e) {
            currentBuild.result = hudson.model.Result.NOT_BUILT.toString()
            notifySlack(slackChannel, "Failed to set environnement variables: " + e.getLocalizedMessage())
            throw e
        }
    }

    if (buildNumber == -1) {

        //-------------------------------
        // Etape 2 : Recuperation du code
        //-------------------------------
        stage('SCM checkout') {
            try {
                checkout([
                        $class                           : 'GitSCM',
                        branches                         : [[name: "${params.BRANCH_TAG}"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[credentialsId: "${gitCredentials}", url: "${gitURL}"]]
                ])

            } catch (e) {
                currentBuild.result = hudson.model.Result.FAILURE.toString()
                notifySlack(slackChannel, "Failed to fetch SCM: " + e.getLocalizedMessage())
                throw e
            }
        }
    }
    for (int moduleIndex = 0; moduleIndex < candidateModules.size(); moduleIndex++) { //Pour chaque module du projet

        //-------------------------------
        // Etape 3 : Compilation
        //-------------------------------
        if ("${executeBuild[moduleIndex]}" == 'true') {

            //-------------------------------
            // Etape 3.2 : Compilation
            //-------------------------------
            stage("[${candidateModules[moduleIndex]}] Compile package") {
                try {
                    sh "'${maventool}/bin/mvn' -Dmaven.test.skip='${!executeTests}' clean package"
                    // ATTENTION #1, rtMaven.run ne tient pas compte des arguments de compilation -D
                    //buildInfo = rtMaven.run pom: 'pom.xml', goals: "clean package -Dmaven.test.skip=${!executeTests} -pl ${candidateModules[moduleIndex]} -am -P${mavenProfil} -DfinalName=${backApplicationFileName} -DwebBaseDir=${backTargetDir}${backApplicationFileName} -DbatchBaseDir=${batchTargetDir}${backApplicationFileName}".toString()

                } catch (e) {
                    currentBuild.result = hudson.model.Result.FAILURE.toString()
                    notifySlack(slackChannel, "Failed to build module ${candidateModules[moduleIndex]}: "+e.getLocalizedMessage())
                    throw e
                }
            }
        }

        if (deployArtifactoy && "${executeBuild[moduleIndex]}" == 'true') {

            //-------------------------------
            // Etape 3.3 : Deploiement sur Artifactory
            //-------------------------------
            stage("[${candidateModules[moduleIndex]}] Archive to Artifactory") {
                try {
                    rtMaven.deployer server: artifactoryServer, releaseRepo: 'libs-release-local', snapshotRepo: 'libs-snapshot-local'
                    buildInfo = rtMaven.run pom: 'pom.xml', goals: "clean package -Dmaven.test.skip=${!executeTests}".toString()
                    buildInfo.name = "${artifactoryBuildName}"
                    rtMaven.deployer.deployArtifacts buildInfo
                    artifactoryServer.publishBuildInfo buildInfo

                } catch (e) {
                    currentBuild.result = hudson.model.Result.FAILURE.toString()
                    notifySlack(slackChannel, "Failed to deploy and publish module ${candidateModules[moduleIndex]} to Artifactory: " + e.getLocalizedMessage())
                    throw e
                }
            }
        }
    } //Pour chaque module du projet

    currentBuild.result = hudson.model.Result.SUCCESS.toString()
    notifySlack(slackChannel,"Congratulation !")
}

def notifySlack(String slackChannel, String info = '') {
    def colorCode = '#848484' // Gray

    switch (currentBuild.result) {
        case 'NOT_BUILT':
            colorCode = '#FFA500' // Orange
            break
        case 'SUCCESS':
            colorCode = '#00FF00' // Green
            break
        case 'UNSTABLE':
            colorCode = '#FFFF00' // Yellow
            break
        case 'FAILURE':
            colorCode = '#FF0000' // Red
            break;
    }

    String message = """
        *Jenkins Build*
        Job name: `${env.JOB_NAME}`
        Build number: `#${env.BUILD_NUMBER}`
        Build status: `${currentBuild.result}`
        Branch or tag: `${params.BRANCH_TAG}`
        Target environment: `${params.ENV}`
        Message: `${info}`
        Build details: <${env.BUILD_URL}/console|See in web console>
    """.stripIndent()

    return slackSend(tokenCredentialId: "slack_token",
            channel: "${slackChannel}",
            color: colorCode,
            message: message)
}