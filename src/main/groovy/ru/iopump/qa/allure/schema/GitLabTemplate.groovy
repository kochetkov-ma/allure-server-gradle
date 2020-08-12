package ru.iopump.qa.allure.schema

import groovy.transform.Internal

@Internal
class GitLabTemplate {

    static Closure<String> requestToGeneration() {
        def reportPostfix = System.env.PATH_POSTFIX
        def pipelineId = System.env.CI_PIPELINE_ID ?: '0'
        def pipelineUrl = System.env.CI_PIPELINE_URL ?: 'localhost'
        def jobName = System.env.CI_JOB_NAME ?: 'manual'
        def mr = System.env.CI_MERGE_REQUEST_IID
        def branch = System.env.CI_COMMIT_REF_NAME ?: 'master'
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
}"""
        }
    }
}
