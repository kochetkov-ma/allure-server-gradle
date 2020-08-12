package ru.iopump.qa.allure.task

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA
import static ru.iopump.qa.allure.AllureServerPlugin.TASK_ARCHIVE_NAME
import static ru.iopump.qa.allure.AllureServerPlugin.EXTENSION_NAME

@CompileStatic
class AllureServerSendTask extends DefaultTask {
    public static final String UPLOADED_RESULT_UUID = 'uploaded-result-uuid.txt'

    @Input
    URL allureServerUrl

    @Input
    File archiveResult

    @OutputFile
    RegularFileProperty resultUuidFile

    AllureServerSendTask() {
        this.description = 'Send zip archive to server'
        this.group = 'allure-server'
        this.resultUuidFile = project.objects.fileProperty()
        this.resultUuidFile.set(new File("$project.buildDir/$UPLOADED_RESULT_UUID"))
    }

    @TaskAction
    makeArchive() {
        if (!archiveResult.exists()) throw new GradleException("Allure result archive '$archiveResult' is not exist. Check task '$TASK_ARCHIVE_NAME' configuration")
        if (!archiveResult.size()) throw new GradleException("Allure result archive '$archiveResult' is empty. Check extension '$EXTENSION_NAME' configuration or skip task on empty result")

        logger.lifecycle "Send results '$archiveResult' to server '$allureServerUrl'"

        def resultUuid = send()['uuid']
        resultUuidFile.get().asFile.text = resultUuid

        logger.lifecycle "Send results to server. Uuid: '$resultUuid' in file '${resultUuidFile.asFile.get()}' [SUCCESS]"
    }

    @Internal
    private Map<String, String> send() {
        def httpClient = HttpClientBuilder.create().build()

        def entity = MultipartEntityBuilder
                .create()
                .setContentType(MULTIPART_FORM_DATA)
                .addBinaryBody('allureResults', archiveResult, ContentType.create('application/zip'), archiveResult.name)
                .build()

        def httpPost = new HttpPost(new URIBuilder(allureServerUrl.toURI()).setPath('/api/result').build()).tap { it.setEntity(entity) }

        return httpClient.execute(httpPost).withCloseable { response ->
            def res = new JsonSlurper().parse(response.entity.getContent()) as Map<String, String>
            logger.lifecycle("Allure Server response: $response")
            res
        }
    }
}
