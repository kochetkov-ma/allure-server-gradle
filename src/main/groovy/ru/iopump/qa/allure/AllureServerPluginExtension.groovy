package ru.iopump.qa.allure

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal

import javax.inject.Inject

@CompileStatic
class AllureServerPluginExtension {

    @Input
    String relativeResultDir

    @InputFiles
    Collection<File> resultDirs

    final private Project project

    @Inject
    AllureServerPluginExtension(Project project) {
        this.project = project
    }

    @Internal
    Collection<File> calculateResultDirs() {
        boolean noResultDirsDefine = (resultDirs ?: []).empty

        if (noResultDirsDefine) {
            project.logger.lifecycle "No result directory define by extension 'allureServer'"
            findResultDirs()
        } else {
            project.logger.lifecycle "User defined result directories: $resultDirs"
            resultDirs
        }
    }

    @Internal
    Collection<File> findResultDirs() {
        (project.allprojects
                .collect {
                    def dir = it.file "$it.projectDir/$relativeResultDir"
                    def isResultDir = dir.exists() && dir.isDirectory()
                    project.logger.lifecycle "For '$it' results directory '$dir' is ${isResultDir ? 'exist' :'not exist'}"
                    return dir
                } - (null as File) as Collection<File>)
                .tap { project.logger.lifecycle "Found result directories '$it'" }
    }
}
