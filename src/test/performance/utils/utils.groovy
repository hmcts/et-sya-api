import groovy.json.JsonSlurper
import groovy.json.JsonOutput

// Performance in pipelines

//==========================================    
// Request to send dynatrace custom_info event
//==========================================    
def postDynatraceEvent(dynatraceApiHost, dynatraceSyntheticTest, dynatraceDashboardId, dynatraceEntitySelector) {
    def response = null
    try {
    response = httpRequest(
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        httpMode: 'POST',
        quiet: true,
        customHeaders: [
            [name: 'Authorization', value: "Api-Token ${env.PERF_EVENT_TOKEN}"]
        ],
        url: "${dynatraceApiHost}api/v2/events/ingest",
        requestBody: """{
            "entitySelector": "${dynatraceEntitySelector}",
            "eventType": "CUSTOM_INFO",
            "properties": {
                "Workspace": "${env.WORKSPACE}",
                "Branch": "${env.BRANCH_NAME}",
                "Build Number": "${env.BUILD_NUMBER}",
                "Change URL": "${env.CHANGE_URL}",
                "Commit ID": "${env.GIT_COMMIT}",
                "Build URL": "${env.BUILD_URL}", 
                "Synthetic Performance Test": "${dynatraceSyntheticTest}",
                "Performance Dashboard": "${dynatraceApiHost}#dashboard;id=${dynatraceDashboardId};applyDashboardDefaults=true"
            },
            "timeout": 1,
            "title": "ET-SYA-API Performance Event"
        }"""
    )
    echo "Dynatrace event posted successfully. Response ${response}"
    } catch (Exception e)
    {
        echo "Failure posting Dynatrace Event: ${e.message}"
    }
    return response
}

//==========================================    
/// POST Metric to highlight a deployment
//==========================================
def postDynatraceMetric(dynatraceApiHost, dynatraceMetricIngestEndpoint, dynatraceMetricType, dynatraceMetricTag, environment) {
    def response = null
    try {
    response = httpRequest(
        acceptType: 'APPLICATION_JSON',
        contentType: 'TEXT_PLAIN',
        httpMode: 'POST',
        quiet: true,
        customHeaders: [
            [name: 'Authorization', value: "Api-Token ${env.PERF_METRICS_TOKEN}"]
        ],
        url: "${dynatraceApiHost}${dynatraceMetricIngestEndpoint}",
        requestBody: "env.release.value,type=${dynatraceMetricType},tag=${dynatraceMetricTag},env=${environment} 3"
    ) 
    echo "Dynatrace metric posted successfully. Response ${response}"
    } catch (Exception e)
    {
    echo "Failure posting Dynatrace Metric: ${e.message}"
    }
    return response
}

// //==========================================   
// //Trigger Dynatrace Synthetic Test
// //==========================================
def postDynatraceSyntheticTest(dynatraceApiHost, dynatraceTriggerSyntheticEndpoint,dynatraceSyntheticTest) {
    def response = null
    try {
    response = httpRequest(
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        httpMode: 'POST',
        quiet: true,
        customHeaders: [
            [name: 'Authorization', value: "Api-Token ${env.PERF_SYNTHETIC_MONITOR_TOKEN}"]
        ],
        url: "${dynatraceApiHost}${dynatraceTriggerSyntheticEndpoint}",
        requestBody: """{
            "monitors": [
                {
                    "monitorId": "${dynatraceSyntheticTest}"
                }
            ]
        }"""
    )
    echo "Dynatrace synthetic test triggered. Response ${response}"
    }
    catch (Exception e) {
        echo "Error while sending Request: ${e.message}"
    }
    
    echo "Raw JSON response:\n${response.content}"

    // Parse the JSON string into an object
    def json = new JsonSlurper().parseText(response.content)

    // Extract individual values
    def triggeredCount = json.triggeredCount
    def executedSynthetics = json.triggered[0].executions
    
    // Get last execution ID using index
    def selectedIndex = triggeredCount - 1
    def lastExecutionId = executedSynthetics[selectedIndex].executionId

    echo "Triggered Count: ${triggeredCount}"
    echo "Last Execution ID: ${lastExecutionId}"

    return [
        response: response,
        triggeredCount: triggeredCount,
        lastExecutionId: lastExecutionId
    ]
}

// //==========================================   
// //Check Status of Synthetic Test
// //==========================================
def getDynatraceSyntheticStatus(dynatraceApiHost, lastExecutionId) {
    def response = null
    try {
    response = httpRequest(
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        httpMode: 'GET',
        quiet: true,
        customHeaders: [
            [name: 'Authorization', value: "Api-Token ${env.PERF_SYNTHETIC_MONITOR_TOKEN}"]
        ],
        url: "${dynatraceApiHost}api/v2/synthetic/executions/${lastExecutionId}"
    )
    echo "Check Synthetic Status: Response ${response}"
    }
    catch (Exception e) {
        echo "Error while sending Request: ${e.message}"
    }
      echo "Raw JSON response:\n${response.content}"

    // Parse the JSON string into an object
    def json = new JsonSlurper().parseText(response.content)

    // Extract Status 
    def executionStatus = json.executionStage
        
    echo "Current Status: ${executionStatus}"
    return [
        response: response,
        executionStatus: executionStatus
    ]
}

// //==========================================   
// Get & Put Dynatrace
// //==========================================
def putDynatraceSyntheticTest(dynatraceApiHost, dynatraceUpdateSyntheticEndpoint, dynatraceSyntheticTest, dynatraceSyntheticEnabled, previewUrl = "https://et-sya.DEFAULT.platform.hmcts.net/", dynatraceScriptName) {
    def response = null
    def dynatraceScript = null
    //def requestBodyOne = null
    //def requestBody = rawRequestBody
    //.replace('AKS_TEST_URL', env.AKS_TEST_URL)
    //.replace('"${DYNATRACE_SYNTHETIC_ENABLED}"', dynatraceSyntheticEnabled)
    
    echo "DynatraceScript Name: ${dynatraceScriptName}"
    //try {
    //    dynatraceScript = load "src/test/performance/scripts/et_cui_applicant_previewEscaped.groovy"
        //echo "REQUEST BODY 1:\n"
        //echo dynatraceScript.requestBodyOne
        //echo "REQUEST BODY 2:\n"
        //echo dynatraceScript.requestBodyTwo
    //} catch (Exception e) {
    // echo "Error while loading and outputting script Message: ${e.message}"
    //}
    //try {
    //    requestBodyOne = dynatraceScript.requestBodyOne
    //    .replace('"DYNATRACE_SYNTHETIC_ENABLED"', dynatraceSyntheticEnabled)
    //    .replace('AKS_TEST_URL', env.AKS_TEST_URL)
    //    echo requestBodyOne
    //} catch (Exception e) {
    //    echo "Error while replacing vals in requestBodyOneMessage: ${e.message}"
   //}
    //try {
    //echo "full request body:${requestBodyOne}${dynatraceScript.requestBodyTwo}"
    //def jsonRequestBodyOne = JsonOutput.toJson(requestBodyOne)
    //def jsonRequestBodyTwo = JsonOutput.toJson(dynatraceScript.requestBodyTwo)
    // Overwrite or create the file with the first part
   //writeFile file: 'dynatrace_request.json', text: jsonRequestBodyOne

    // Append the second part
    //writeFile file: 'dynatrace_requestTwo.json', text: jsonRequestBodyTwo
    
    try {
    response = httpRequest(
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        httpMode: 'PUT',
        quiet: true,
        customHeaders: [
            [name: 'Authorization', value: "Api-Token ${env.PERF_SYNTHETIC_UPDATE_TOKEN}"]
        ],
        url: "${dynatraceApiHost}${dynatraceUpdateSyntheticEndpoint}${dynatraceSyntheticTest}",
        requestBody: "${dynatraceScript.requestBodyOne}${dynatraceScript.requestBodyTwo}"
    )
    echo "Dynatrace synthetic test updated. Response ${response}"
    }
    catch (Exception e) {
        echo "Error while updating synthetic in utils: ${e.message}"
        echo "response detail: ${response.content}"
    } 
}

// //==========================================   
// GET & Put Dynatrace Synthetic
// GET Synthetic Info 
// //==========================================
def getDynatraceSyntheticBody(dynatraceApiHost,dynatraceUpdateSyntheticEndpoint, dynatraceSyntheticTest, dynatraceSyntheticEnabled) {
    def response = null
    try {
    response = httpRequest(
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        httpMode: 'GET',
        quiet: true,
        customHeaders: [
            [name: 'Authorization', value: "Api-Token ${env.PERF_SYNTHETIC_MONITOR_TOKEN}"]
        ],
        url: "${dynatraceApiHost}${dynatraceUpdateSyntheticEndpoint}${dynatraceSyntheticTest}"
    )
    echo "Check Synthetic Status: Response ${response}"
    }
    catch (Exception e) {
        echo "Error while sending Request: ${e.message}"
    }
      echo "Raw JSON response:\n${response.content}"

    // Parse the JSON string into an object
    def json = new JsonSlurper().parseText(response.content)

    //Edit the JSON
    json.enabled = "${dynatraceSyntheticEnabled}"
    json.script.events[0].url = "${env.AKS_TEST_URL}" //https://et-sya.testURL.platform.hmcts.net/

    //Convert updated JSON to String:
    def modifiedRequestBody = JsonOutput.toJson(json)

    //Updte the synthetic in Dynatrace
    try {
    response = httpRequest(
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_JSON',
        httpMode: 'PUT',
        quiet: true,
        customHeaders: [
            [name: 'Authorization', value: "Api-Token ${env.PERF_SYNTHETIC_UPDATE_TOKEN}"]
        ],
        url: "${dynatraceApiHost}${dynatraceUpdateSyntheticEndpoint}${dynatraceSyntheticTest}",
        requestBody: "${modifiedRequestBody}"
    )
    echo "Dynatrace synthetic test updated. Response ${response}"
    }
    catch (Exception e) {
        echo "Error while updating synthetic in utils: ${e.message}"
        echo "response detail: ${response.content}"
    } 
}

// //==========================================
// Set Environment Config
// //==========================================
def setEnvironmentConfig(config,envName) {
    if (envName == "preview") {
        config.dynatraceSyntheticTest = config.dynatraceSyntheticTestPreview
        config.dynatraceDashboardId = config.dynatraceDashboardIdPreview
        config.dynatraceDashboardURL = config.dynatraceDashboardURLPreview
        config.dynatraceEntitySelector = config.dynatraceEntitySelectorPreview
    } else if (envName == "aat") {
        config.dynatraceSyntheticTest = config.dynatraceSyntheticTestAAT
        config.dynatraceDashboardId = config.dynatraceDashboardIdAAT
        config.dynatraceDashboardURL = config.dynatraceDashboardURLAAT
        config.dynatraceEntitySelector = config.dynatraceEntitySelectorAAT
    } else if (envName == "perftest") {
        config.dynatraceSyntheticTest = config.dynatraceSyntheticTest
        config.dynatraceDashboardId = config.dynatraceDashboardIdPerfTest
        config.dynatraceDashboardURL = config.dynatraceDashboardURLPerfTest
        config.dynatraceEntitySelector = config.dynatraceEntitySelectorPerfTest
    } else {
        error("Unknown environment: ${envName}")
    }
}

return this



//Static vars
//dynatraceApiHost: "https://yrk32651.live.dynatrace.com/"
//dynatraceEventIngestEndpoint = "api/v2/events/ingest"

// //Vars for eventIngest
// def serviceType = "type(service)"
// def kubernetesNamespaceTag = "[Kubernetes]namespace:et"
// def environmentTag = "Environment:PERF"
// def entityId = "SERVICE-894163B308FBDD78"

// def entitySelector = "${serviceType},tag(\"${kubernetesNamespaceTag}\"),tag(\"${environmentTag}\"),entityId(\"${entityId}\")"

// //Object Maps
// // commonHeaders: [
    // // 'Content-Type': 'application/json'
// // ],

// eventIngestDetails: [
    // eventType: "CUSTOM_INFO",
    // timeout: 1,
    // title: "ET-SYA-Frontend Performance Event",
    // entitySelector: "${entitySelector}"
// ]


//  // Print the loaded config
//  echo "INSIDE CONFIG: Dynatrace API URL: ${env.dynatraceApiHost}"
//  //echo "Dynatrace Endpoint: ${config.dynatraceEventIngestEndpoint}"
//  //echo "Entity Selector: ${config.eventIngestDetails.entitySelector}"
//  //Load Config
//  //def config = load '.\src\test\performance\config\config.groovy'
//  // Accessing the entitySelector from the config map
//  echo "INSIDE CONFIG: Entity Selector: ${env.dynatraceEventIngestEndpoint}"


