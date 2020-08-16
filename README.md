# allure-server-gradle

![Build plugin and execute functional tests](https://github.com/kochetkov-ma/allure-server-gradle/workflows/Build%20plugin%20and%20execute%20functional%20tests/badge.svg?branch=master)  
![GitHub release (latest by date)](https://img.shields.io/github/v/release/kochetkov-ma/allure-server-gradle)

Aggregate allure results - pack to zip archive - send to the [allure-server](https://github.com/kochetkov-ma/allure-server) - generate report - add report URL to the (now GitLab Only) CI

## Quick start

Minimal configuration
```
plugins {
    id 'ru.iopump.qa.allure'
}
allureServer {
    relativeResultDir = 'allure-results'
    allureServerUrl = 'http://localhost:8080'
}
```
Deploy [allure server](https://github.com/kochetkov-ma/allure-server) on your local environment (for e.g. as container)
  
Execute your tests
  
Run task `allureServerGenerate` - it will start `allureArchive` and `allureServerSend` tasks

Results from directory `your_project/allure-results` or `your_project/module[N]/allure-results` will be packed to `zip` -> sent to the server `http://localhost:8080` -> report will be generated -> url will be saved to `build/generated-report-url.txt`

Task `allureGitLabCallback` may help you to post Report Url in Git Server (or CI) but now only `GitLab` supported.

## All settings

```
allureServer {
    /**
     * 'allureArchive' task will try to find this directory in each module + root project.
     * If you have multi-module project with 2 modules it will be:
     * - root/module1/allure-results
     * - root/module2/allure-results
     * - root/allure-results
     */
    relativeResultDir = 'allure-results'
    
    /**
     * 'allureArchive' You may override 'relativeResultDir' setting and specify each results directory as file collection
     *
     *  OPTIONAL
     *  This settings or 'relativeResultDir' will be use. I recommend to use 'relativeResultDir'.
     */
    resultDirs = [ file("$buildDir/allure-result"), file("$rootDir/module1/allure-result-any") ]

    /**
     * tasks 'allureServerSend' and 'allureServerGenerate'
     * Your allure-server base url.
     */
    allureServerUrl = 'http://localhost:8080'

    /**
     * task 'allureServerGenerate'
     * You may specify generation request body. Add build id, ci build url and others according to allure-server OpenApi Spec (Swagger).
     * Now plugin has only one predefined request body for GitLab
     * This parameter can be 'String' type with template name - now supported only 'GITLAB'
     * Or enum GenerationBodyTemplate - now supported only 'GITLAB' enum
     * Or Closure returned String like in this example
     *
     * OPTIONAL
     * DEFAULT VALUE = 'GITLAB'
     * If no GitLab environment variable found it will use default value as local build.
     */
    requestToGeneration = { uuid -> """
{
  "reportSpec": {
    "path": [ "my_project" ],
    "executorInfo": {
      "name": "GitLab/Jenkins/Bamboo/GitHub CI",
      "type": "GitLab/Jenkins/Bamboo/GitHub CI",
      "buildName": "my_ci_build_name",
      "buildUrl": "my_ci_build_url",
      "reportName": "my_ci_report_name"
    }
  },
  "results": [ "$uuid" ],
  "deleteResults": true
}"""
    }

    /* ONLY FOR GITLAB (optional settings). For task 'allureGitLabCallback' */
    /**
     * Enable GitLab auto-callback after task 'allureServerGenerate' execution.
     * It will post message in MergeRequest with Report URL.
     * Now callback implemented only for GitLabCI.
     *
     * OPTIONAL
     * DEFAULT VALUE = FALSE (disable)
     */
    gitLabCallbackEnable = true // Optional

    /**
     * 'allureGitLabCallback'
     * GitLab API TOKEN for callback.
     *
     * OPTIONAL
     * DEFAULT VALUE = System.getenv('SERVICE_USER_API_TOKEN') (From GitLab CI environment)
     */
    gitLabToken = "43rbdfui34bhx34rjn3c"
    /**
     * 'allureGitLabCallback'
     * GitLab API url for callback.
     *
     * OPTIONAL
     * DEFAULT VALUE = System.getenv('CI_API_V4_URL') (From GitLab CI environment)
     */
    gitLabApiUrl = 'http://localhost:8081'

    /**
     * 'allureGitLabCallback'
     * GitLab MergeRequest ID for callback.
     *
     * OPTIONAL
     * DEFAULT VALUE = System.getenv('CI_MERGE_REQUEST_IID') (From GitLab CI environment)
     */
    gitLabMergeRequestId = '0'

    /**
     * 'allureGitLabCallback'
     * GitLab project ID for callback.
     *
     * OPTIONAL
     * DEFAULT VALUE = System.getenv('CI_PROJECT_ID') (From GitLab CI environment)
     */
    gitLabProjectId = '0'
}

/**
 * GitLab API may be changed in future. And you have a possibility to specify 'allureGitLabCallback' the task.
 * It's advanced usage.
 * I recommend to create an issue in 'https://github.com/kochetkov-ma/allure-server-gradle' about GitLab API changes.
 */
allureGitLabCallback {
    /**
     * Override report url.
     * Previous task store report url to 'build/generated-report-url.txt'
     * You may get it, transform and set
     */
    reportUrlString = 'test_override'

    /**
     * GitLab endpoint path to send URL in comments.
     * You may change it.
     * It must be a 'Closure<String>' object.
     * This closure has delegate - Map<String, String> with keys 'apiUrl', 'projectId', 'mrId' - you may use it in GString as Lazy Placeholders like '{-> }'.
     * !!! Remember only LAZY !!!
     */
    gitLabMergeRequestNotesEndpointPath {
        "${->apiUrl}/projects_override/${->projectId}/merge_requests/${->mrId}/notes"
    }
}
```
## Links
 - Examples from manual testing: [sample-multi-module](sample-multi-module/)
 - Allure-Server: [allure-server](https://github.com/kochetkov-ma/allure-server)
 - **Kotlin** **Kotest** Allure Extended Listener: [kotest-allure](https://github.com/kochetkov-ma/kotest-allure)
 