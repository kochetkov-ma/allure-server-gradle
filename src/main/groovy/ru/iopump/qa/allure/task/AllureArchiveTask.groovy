package ru.iopump.qa.allure.task


import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip

class AllureArchiveTask extends Zip {
    public static final String ALLURE_RESULT_ZIP = "allure-results.zip"

    AllureArchiveTask() {
        this.description = 'Collect results and make zip archive'
        this.group = 'allure-server'
        this.archiveFileName.set(ALLURE_RESULT_ZIP)
        this.destinationDirectory.set(project.file(project.rootProject.buildDir))
    }

    @TaskAction
    makeArchive() {
        copy()
        logger.lifecycle "Collect results and make zip archive. To: ${archiveFile.get()}. [SUCCESS]"
    }
}
