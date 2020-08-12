package ru.iopump.qa.allure.task

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import static org.apache.http.entity.ContentType.APPLICATION_JSON

@CompileStatic
class AllureServerGenerateTask extends DefaultTask {
    public static final String GENERATED_REPORT_URL = 'generated-report-url.txt'

    @Input
    URL allureServerUrl

    @Input
    Closure<String> bodyToGeneration

    @OutputFile
    RegularFileProperty reportUrlFile

    AllureServerGenerateTask() {
        this.description = 'Generate allure report from uploaded zip archive result'
        this.group = 'allure-server'
        this.reportUrlFile = project.objects.fileProperty()
        this.reportUrlFile.set(new File("$project.buildDir/$GENERATED_REPORT_URL"))
    }

    @TaskAction
    makeArchive() {
        def body = bodyToGeneration.call()
        logger.lifecycle "Generate allure report with body '$body' on server '$allureServerUrl'"
        def reportUrlString = generate(body)['url']
        reportUrlFile.get().asFile.text = reportUrlString
        logger.lifecycle "Generate allure report. Url: '$reportUrlString' in file '${reportUrlFile.asFile.get()}' [SUCCESS]"
    }

    @Internal
    private Map<String, String> generate(String body) {
        def httpClient = HttpClientBuilder.create().build()
        def entity = new StringEntity(body, APPLICATION_JSON)
        def httpPost = new HttpPost(new URIBuilder(allureServerUrl.toURI()).setPath('/api/report').build()).tap { it.setEntity(entity) }

        return httpClient.execute(httpPost).withCloseable { response ->
            def res = new JsonSlurper().parse(response.entity.getContent()) as Map<String, String>
            logger.lifecycle("Allure Server response: $response")
            res
        }
    }
}
