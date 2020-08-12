package ru.iopump.qa.allure.schema
/**
 * GITLAB:
 * Using gitlab variables via environment variables:
 * <ul>
 *     <li> PATH_POSTFIX - Optional User variable
 *     <li> CI_PIPELINE_ID - Gitlab Env Var
 *     <li> CI_PIPELINE_URL - Gitlab Env Var
 *     <li> CI_JOB_NAME - Gitlab Env Var
 *     <li> CI_MERGE_REQUEST_IID - Gitlab Env Var
 *     <li> CI_COMMIT_REF_NAME - Gitlab Env Var
 * </ul>
 *
 * Full Report Path will be -> 'CI_MERGE_REQUEST_IID OR CI_COMMIT_REF_NAME/CI_JOB_NAME/PATH_POSTFIX(if exists)'
 *
 */
enum GenerationBodyTemplate {

    GITLAB(new GitLabTemplate().requestToGeneration())

    GenerationBodyTemplate(Closure<String> requestToGeneration) {
        this.requestToGeneration = requestToGeneration
    }
    Closure<String> requestToGeneration
}