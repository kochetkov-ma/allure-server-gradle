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
        """
    }

    def 'configure by allureServer and execute allureArchive action'() {
        buildFile << """
            allureServer {
                relativeResultDir = 'allure-results'
            }
        """

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
}
