package ru.iopump.qa.allure

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import ru.iopump.qa.allure.schema.GenerationBodyTemplate

import javax.inject.Inject

@CompileStatic
class AllureServerPluginExtension {

    final private Project p
    final private Logger log

    @Input
    Property<String> allureServerUrl = p.objects.property(String)

    @Input
    Property<String> relativeResultDir = p.objects.property(String)

    @InputFiles
    ListProperty<File> resultDirs = p.objects.listProperty(File)

    @Input
    Object requestToGeneration = GenerationBodyTemplate.GITLAB

    @Input
    Property<Boolean> gitLabCallbackEnable = p.objects.property(Boolean).value(false)

    @Input
    Property<String> gitLabToken = p.objects.property(String).value(System.getenv('SERVICE_USER_API_TOKEN'))

    @Input
    Property<String> gitLabApiUrl = p.objects.property(String).value(System.getenv('CI_API_V4_URL'))

    @Input
    Property<String> gitLabProjectId = p.objects.property(String).value(System.getenv('CI_PROJECT_ID'))

    @Input
    Property<String> gitLabMergeRequestId = p.objects.property(String).value(System.getenv('CI_MERGE_REQUEST_IID'))


    @Inject
    AllureServerPluginExtension(Project p) {
        this.p = p
        this.log = p.logger
    }

    @Internal
    Provider<URL> checkAndGetAllureServerUrl() {
        allureServerUrl.map { new URL(it) }
    }

    @Internal
    @CompileDynamic
    Provider<String> makeRequestToGeneration(Provider<String> uuid) {
        uuid.map {
            if (requestToGeneration instanceof Closure<String>) {
                return (requestToGeneration as Closure<String>).call(uuid.get())
            } else if (requestToGeneration instanceof GenerationBodyTemplate) {
                return (requestToGeneration as GenerationBodyTemplate).requestToGeneration.call(uuid.get())
            } else if (requestToGeneration instanceof String) {
                GenerationBodyTemplate.valueOf((requestToGeneration as String).toUpperCase()).requestToGeneration.call(uuid.get())
            } else {
                throw new GradleException("requestToGeneration can be:\n" +
                        "   - Closure<String> with incoming argument as 'uuid' String type\n" +
                        "   - GenerationBodyTemplate enum\n" +
                        "   - String representation of GenerationBodyTemplate [${GenerationBodyTemplate.values()}]\n" +
                        "   - Or default value $GenerationBodyTemplate.GITLAB, if not specified\n" +
                        "but your 'makeRequestToGeneration' has type '${requestToGeneration.getClass()}'")
            }
        }
    }

    @Internal
    Provider<List<File>> calculateResultDirs() {
        (resultDirs as Provider<List<File>>).map { list ->
            if (list.isEmpty()) {
                log.info "No result directory define by extension 'allureServer'"
                return findResultDirs()
            } else {
                log.info "User defined result directories: $list"
                return list
            }
        }
    }

    private List<File> findResultDirs() {
        p.allprojects
                .collect { it.file("$it.projectDir/${relativeResultDir.get()}") }
                .findAll { file ->
                    def exists = file.exists() && file.isDirectory()
                    log.info "For results directory '$file' ${exists ? 'exists' : 'not exists'}"
                    exists
                }
                .tap { log.info "Found result directories '$it'" }
    }
}
