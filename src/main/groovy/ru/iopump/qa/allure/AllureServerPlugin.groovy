package ru.iopump.qa.allure


import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.iopump.qa.allure.task.AllureArchiveTask
import ru.iopump.qa.allure.task.AllureServerGenerateTask
import ru.iopump.qa.allure.task.AllureServerSendTask
import ru.iopump.qa.allure.task.GitLabMRCallbackTask

class AllureServerPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "allureServer"

    public static final String TASK_ARCHIVE_NAME = "allureArchive"
    public static final String TASK_SEND_NAME = "allureServerSend"
    public static final String TASK_GENERATE_NAME = "allureServerGenerate"
    public static final String TASK_GITLAB_CALLBACK = "allureGitLabCallback"

    void apply(Project project) {
        def e = project.extensions
        def extension = e.create(EXTENSION_NAME, AllureServerPluginExtension.class, project)

        project.getTasks().register(TASK_ARCHIVE_NAME, AllureArchiveTask.class, new Action<AllureArchiveTask>() {
            void execute(AllureArchiveTask task) {
                task.from(extension.calculateResultDirs())
            }
        })

        project.getTasks().register(TASK_SEND_NAME, AllureServerSendTask.class, new Action<AllureServerSendTask>() {
            void execute(AllureServerSendTask task) {
                task.dependsOn TASK_ARCHIVE_NAME

                task.allureServerUrl.set extension.checkAndGetAllureServerUrl()
                task.archiveResult.set((project.tasks[TASK_ARCHIVE_NAME] as AllureArchiveTask).archiveFile)
            }
        })

        project.getTasks().register(TASK_GENERATE_NAME, AllureServerGenerateTask.class, new Action<AllureServerGenerateTask>() {
            void execute(AllureServerGenerateTask task) {
                def sendTask = project.tasks[TASK_SEND_NAME] as AllureServerSendTask
                task.dependsOn TASK_SEND_NAME

                task.allureServerUrl.set extension.checkAndGetAllureServerUrl()
                task.bodyToGeneration.set extension.makeRequestToGeneration(sendTask.resultUuidFile.map { it.asFile.text })

                if (extension.gitLabCallbackEnable.getOrElse(false)) {
                    task.finalizedBy TASK_GITLAB_CALLBACK
                }
            }
        })

        project.getTasks().register(TASK_GITLAB_CALLBACK, GitLabMRCallbackTask.class, new Action<GitLabMRCallbackTask>() {
            void execute(GitLabMRCallbackTask task) {
                def genTask = project.tasks[TASK_GENERATE_NAME] as AllureServerGenerateTask
                task.dependsOn TASK_GENERATE_NAME

                if (!task.reportUrlString.isPresent()) task.reportUrlString.set(genTask.reportUrlFile.map { it.asFile.text })
                task.gitLabToken.set(extension.gitLabToken)
                task.gitLabApiUrl.set(extension.gitLabApiUrl)
                task.gitLabProjectId.set(extension.gitLabProjectId)
                task.gitLabMergeRequestId.set(extension.gitLabMergeRequestId)
            }
        })
    }
}