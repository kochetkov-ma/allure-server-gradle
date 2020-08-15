package ru.iopump.qa.allure.task


import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class GitLabMRCallbackTask extends DefaultTask {

    @Input
    @Internal
    final Property<String> gitLabToken = project.objects.property(String)

    @Input
    @Internal
    final Property<String> gitLabApiUrl = project.objects.property(String)

    @Input
    @Internal
    final Property<String> gitLabProjectId = project.objects.property(String)

    @Input
    @Internal
    final Property<String> gitLabMergeRequestId = project.objects.property(String)

    @Input
    Property<String> reportUrlString = project.objects.property(String)

    @Input
    @Optional
    Closure<String> gitLabMergeRequestNotesEndpointPath = { "$apiUrl/projects/$projectId/merge_requests/$mrId/notes" }

    GitLabMRCallbackTask() {
        this.description = 'Post generated report URL to GitLab MR comments'
        this.group = 'allure-server'
    }

    def gitLabMergeRequestNotesEndpointPath(Closure<String> c) { this.gitLabMergeRequestNotesEndpointPath = c }

    @TaskAction
    sendUrl() {
        def reportUrl = reportUrlString.get()
        gitLabMergeRequestNotesEndpointPath.resolveStrategy = Closure.DELEGATE_ONLY
        gitLabMergeRequestNotesEndpointPath.delegate = [apiUrl: gitLabApiUrl.get(), projectId: gitLabProjectId.get(), mrId: gitLabMergeRequestId.get()]
        String gitLabUrl = gitLabMergeRequestNotesEndpointPath()

        logger.lifecycle "Send report URL '$reportUrl' to GitLab '$gitLabUrl'"
        send(reportUrl, gitLabUrl, gitLabToken.get())
        logger.lifecycle "Send report URL to GitLab [SUCCESS]"
    }

    private send(String reportUrl, String gitLabUrl, String token) {
        def httpClient = HttpClientBuilder.create().build()
        def httpPost = new HttpPost(new URIBuilder(gitLabUrl)
                .addParameter('body', 'ALLURE REPORT: ' + reportUrl)
                .build()
        ).tap { it.addHeader('Private-Token', token) }

        return httpClient.execute(httpPost).withCloseable { response ->
            logger.lifecycle("GitLab response: $response")
        }
    }
}
