package ru.iopump.qa.allure

import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import ru.iopump.qa.allure.schema.GenerationBodyTemplate

import javax.inject.Inject

@CompileStatic
class AllureServerPluginExtension {

    @Input
    String allureServerUrl

    @Input
    String relativeResultDir

    @InputFiles
    Collection<File> resultDirs

    @Input
    Object requestToGeneration = GenerationBodyTemplate.GITLAB

    final private Project project

    @Inject
    AllureServerPluginExtension(Project project) {
        this.project = project
    }

    URL checkAndGetAllureServerUrl() {
        new URL(allureServerUrl)
    }

    String makeRequestToGeneration(String uuid) {
        if (requestToGeneration instanceof Closure<String>) {
            return (requestToGeneration as Closure<String>).call(uuid)
        } else if (requestToGeneration instanceof GenerationBodyTemplate) {
            return (requestToGeneration as GenerationBodyTemplate).requestToGeneration.call(uuid)
        } else if (requestToGeneration instanceof String) {
            GenerationBodyTemplate.valueOf(requestToGeneration.toUpperCase()).requestToGeneration.call(uuid)
        } else {
            throw new GradleException("requestToGeneration can be:\n" +
                    "   - Closure<String> with incoming argument as 'uuid' String type\n" +
                    "   - GenerationBodyTemplate enum\n" +
                    "   - String representation of GenerationBodyTemplate [${GenerationBodyTemplate.values()}]\n" +
                    "   - Or default value $GenerationBodyTemplate.GITLAB, if not specified\n" +
                    "but your 'makeRequestToGeneration' has type '${requestToGeneration.getClass()}'")
        }

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
                    project.logger.lifecycle "For '$it' results directory '$dir' is ${isResultDir ? 'exist' : 'not exist'}"
                    return dir
                } - (null as File) as Collection<File>)
                .tap { project.logger.lifecycle "Found result directories '$it'" }
    }
}
