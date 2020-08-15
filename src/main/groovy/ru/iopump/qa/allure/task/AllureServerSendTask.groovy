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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA
import static ru.iopump.qa.allure.AllureServerPlugin.EXTENSION_NAME
import static ru.iopump.qa.allure.AllureServerPlugin.TASK_ARCHIVE_NAME

@CompileStatic
class AllureServerSendTask extends DefaultTask {
    public static final String UPLOADED_RESULT_UUID = 'uploaded-result-uuid.txt'

    @Internal
    final Property<URL> allureServerUrl = project.objects.property(URL)

    @Internal
    final RegularFileProperty archiveResult = project.objects.fileProperty()

    @OutputFile
    final RegularFileProperty resultUuidFile = project.objects.fileProperty()

    AllureServerSendTask() {
        this.description = 'Send zip archive to server'
        this.group = 'allure-server'
        this.resultUuidFile.set(new File("$project.buildDir/$UPLOADED_RESULT_UUID"))
    }

    @TaskAction
    makeArchive() {
        def archiveResultFile = archiveResult.get().asFile
        if (!archiveResultFile.exists()) throw new GradleException("Allure result archive '$archiveResultFile' is not exist. Check task '$TASK_ARCHIVE_NAME' configuration")
        if (!archiveResultFile.size()) throw new GradleException("Allure result archive '$archiveResultFile' is empty. Check extension '$EXTENSION_NAME' configuration or skip task on empty result")

        logger.lifecycle "Send results '$archiveResultFile' to server '${allureServerUrl.get()}'"

        def resultUuid = send(archiveResultFile)['uuid']
        resultUuidFile.get().asFile.text = resultUuid

        logger.lifecycle "Send results to server. Uuid: '$resultUuid' in file '${resultUuidFile.asFile.get()}' [SUCCESS]"
    }

    private Map<String, String> send(File archiveResultFile) {
        def httpClient = HttpClientBuilder.create().build()

        def entity = MultipartEntityBuilder
                .create()
                .setContentType(MULTIPART_FORM_DATA)
                .addBinaryBody('allureResults', archiveResultFile, ContentType.create('application/zip'), archiveResultFile.name)
                .build()

        def httpPost = new HttpPost(new URIBuilder(allureServerUrl.get().toURI()).setPath('/api/result').build()).tap { it.setEntity(entity) }

        return httpClient.execute(httpPost).withCloseable { response ->
            def res = new JsonSlurper().parse(response.entity.getContent()) as Map<String, String>
            logger.lifecycle("Allure Server response: $response")
            res
        }
    }
}
