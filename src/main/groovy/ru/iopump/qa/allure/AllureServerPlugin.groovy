package ru.iopump.qa.allure

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.iopump.qa.allure.task.AllureArchiveTask
import ru.iopump.qa.allure.task.AllureServerGenerateTask
import ru.iopump.qa.allure.task.AllureServerSendTask

@CompileStatic
class AllureServerPlugin implements Plugin<Project> {

    public static final String EXTENSION_NAME = "allureServer"

    public static final String TASK_ARCHIVE_NAME = "allureArchive"
    public static final String TASK_SEND_NAME = "allureServerSend"
    public static final String TASK_GENERATE_NAME = "allureServerGenerate"

    void apply(Project project) {
        def extension = project.getExtensions().create(EXTENSION_NAME, AllureServerPluginExtension.class, project)

        project.getTasks().register(TASK_ARCHIVE_NAME, AllureArchiveTask.class, new Action<AllureArchiveTask>() {
            void execute(AllureArchiveTask task) {
                task.from(extension.calculateResultDirs())
            }
        })

        project.getTasks().register(TASK_SEND_NAME, AllureServerSendTask.class, new Action<AllureServerSendTask>() {
            void execute(AllureServerSendTask task) {
                task.dependsOn TASK_ARCHIVE_NAME
                task.allureServerUrl = extension.checkAndGetAllureServerUrl()
                task.archiveResult = (project.tasks[TASK_ARCHIVE_NAME] as AllureArchiveTask).archiveFile.get().asFile
            }
        })

        project.getTasks().register(TASK_GENERATE_NAME, AllureServerGenerateTask.class, new Action<AllureServerGenerateTask>() {
            void execute(AllureServerGenerateTask task) {
                def sendTask = project.tasks[TASK_SEND_NAME] as AllureServerSendTask

                task.dependsOn TASK_SEND_NAME
                task.allureServerUrl = extension.checkAndGetAllureServerUrl()
                task.bodyToGeneration = { extension.makeRequestToGeneration(sendTask.resultUuidFile.get().asFile.text) }
            }
        })
    }
}