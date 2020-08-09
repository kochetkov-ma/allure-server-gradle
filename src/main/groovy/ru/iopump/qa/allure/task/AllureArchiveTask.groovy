package ru.iopump.qa.allure.task

import groovy.transform.CompileStatic
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Zip

@CompileStatic
class AllureArchiveTask extends Zip {
    public static final String ALLURE_RESULT_ZIP = "allure-results.zip"

    Object[] resultDirs

    AllureArchiveTask() {
        this.description = 'Collect results and make zip archive'
        this.group = 'allure-server'
        this.archiveFileName.set(ALLURE_RESULT_ZIP)
        this.destinationDirectory.set(project.file(project.rootProject.buildDir))
    }

    @Override
    AbstractCopyTask from(Object... sourcePaths) {
        resultDirs = sourcePaths
        return super.from(sourcePaths)
    }

    @TaskAction
    makeArchive() {
        logger.lifecycle "Collect results and make zip archive. From: $resultDirs. [START...]"
        copy()
        logger.lifecycle "Collect results and make zip archive. To: ${archiveFile.get()}. [SUCCESS]"
    }
}
