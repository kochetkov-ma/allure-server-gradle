package ru.iopump.qa.allure.schema

import groovy.transform.CompileStatic
import groovy.transform.Internal

@Internal
@CompileStatic
class GitLabTemplate {

    static Closure<String> requestToGeneration() {
        def reportPostfix = System.getenv('PATH_POSTFIX')
        def pipelineId = System.getenv('CI_PIPELINE_ID') ?: '0'
        def pipelineUrl = System.getenv('CI_PIPELINE_URL') ?: 'localhost'
        def jobName = System.getenv('CI_JOB_NAME') ?: 'manual'
        def mr = System.getenv('CI_MERGE_REQUEST_IID')
        def branch = System.getenv('CI_COMMIT_REF_NAME') ?: 'master'
        def path = (mr ? mr.trim() ?: branch : branch) + "/" + jobName + (reportPostfix ? "/$reportPostfix" : '')

        {
            uuid ->
                """
{
  "reportSpec": {
    "path": [ "$path" ],
    "executorInfo": {
      "name": "GitLab CI",
      "type": "GitLab CI",
      "buildName": "$pipelineId",
      "buildUrl": "$pipelineUrl",
      "reportName": "$jobName"
    }
  },
  "results": [ "$uuid" ],
  "deleteResults": true
}""" as String
        }
    }
}
