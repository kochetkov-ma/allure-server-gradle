package ru.iopump.qa.allure

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.iopump.qa.allure.task.AllureArchiveTask

@CompileStatic
class AllureServerPlugin implements Plugin<Project> {

    private final String EXTENSION_NAME = "allureServer"
    private final String TASK_NAME = "allureArchive"

    void apply(Project project) {
        final AllureServerPluginExtension extension = project.getExtensions().create(EXTENSION_NAME, AllureServerPluginExtension.class, project)

        project.getTasks().register(TASK_NAME, AllureArchiveTask.class, new Action<AllureArchiveTask>() {
            void execute(AllureArchiveTask task) {
                task.from(extension.calculateResultDirs())
            }
        })
    }
}