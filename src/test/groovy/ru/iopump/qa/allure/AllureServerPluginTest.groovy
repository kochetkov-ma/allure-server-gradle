package ru.iopump.qa.allure

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AllureServerPluginTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins { id 'ru.iopump.qa.allure' }

            allureGitLabCallback {
                reportUrlString = 'test_override'
                gitLabMergeRequestNotesEndpointPath { 
                    "\${->apiUrl}/projects_override/\${->projectId}/merge_requests/\${->mrId}/notes" 
                }
            }

            allureServer {
                relativeResultDir = 'allure-results'
                allureServerUrl = 'http://localhost:8080'
                requestToGeneration = { uuid ->
                    \"\"\"{ "reportSpec": { "path": [ "max" ], 
"executorInfo": { "name": "GitLab CI", "type": "GitLab CI", "buildName": "max-pipeline", "buildUrl": "localhost", "reportName": "max-job" } }, 
"results": [ "\$uuid" ], "deleteResults": true }\"\"\"
                }
                gitLabCallbackEnable = false
                gitLabApiUrl = 'http://localhost:8081'
                gitLabMergeRequestId = '0'
                gitLabProjectId = '0'
                gitLabToken = '1q2w3e' 
            }  
        """
    }

    def 'allureArchive task should create zip file'() {

        def resultDir = testProjectDir.newFolder('allure-results')
        new File("$resultDir/result.txt").text = 'result text'
        def zip = new File("$testProjectDir.root/build/allure-results.zip")

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('allureArchive')
                .withPluginClasspath()
                .build()

        then:
        println "Task result:\n$result.output"
        result.output.contains '[SUCCESS]'
        zip.size() > 0
        result.task(":allureArchive").outcome == TaskOutcome.SUCCESS
    }


    def 'allureServerSend task should send zip to allure-server'() {
        def resultDir = testProjectDir.newFolder('allure-results')
        new File("$resultDir/result.txt").text = 'result text'

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('allureServerSend')
                .withPluginClasspath()
                .build()

        then:
        println "Task result:\n$result.output"
        result.output.contains '[SUCCESS]'
        result.task(':allureServerSend').outcome == TaskOutcome.SUCCESS
    }

    def 'allureServerGenerate task should generate allure report and obtain url'() {
        def resultDir = testProjectDir.newFolder('allure-results')
        new File("$resultDir/result.txt").text = 'result text'

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('allureServerGenerate')
                .withPluginClasspath()
                .build()

        then:
        println "Task result:\n$result.output"
        result.output.contains '[SUCCESS]'
        result.task(':allureServerSend').outcome == TaskOutcome.SUCCESS
    }

    def 'allureGitLabCallback task should send report url to GitLab MR comments'() {
        def resultDir = testProjectDir.newFolder('allure-results')
        new File("$resultDir/result.txt").text = 'result text'

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('allureGitLabCallback')
                .withPluginClasspath()
                .build()

        then:
        println "Task result:\n$result.output"
        result.output.contains '[SUCCESS]'
        result.task(':allureServerSend').outcome == TaskOutcome.SUCCESS
    }
}
