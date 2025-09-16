// ====== Set up default values ======
//this.dynatraceApiHost = "https://yrk32651.live.dynatrace.com/"
//this.dynatraceEventIngestEndpoint = "api/v2/events/ingest"
//this.dynatraceMetricIngestEndpoint = "api/v2/metrics/ingest"
//this.dynatraceTriggerSyntheticEndpoint = "api/v2/synthetic/executions/batch"
//this.dynatraceUpdateSyntheticEndpoint = "api/v1/synthetic/monitors/"
this.dynatraceMetricType = 'et-sya-api'
this.dynatraceMetricTag = 'namespace:et'
//this.dynatraceSyntheticTest = ""
//this.dynatraceDashboardId = ""
//this.dynatraceDashboardURL = ""
//this.dynatraceEntitySelector = " "
//Preview Config
this.dynatraceSyntheticTestPreview = "HTTP_CHECK-467146A25406C026"
this.dynatraceDashboardIdPreview = "f44ff1f7-e13a-4a6a-95b9-4e3ab44e6587"
this.dynatraceDashboardURLPreview = "https://yrk32651.live.dynatrace.com/#dashboard;id=f44ff1f7-e13a-4a6a-95b9-4e3ab44e6587;applyDashboardDefaults=true"
this.dynatraceEntitySelectorPreview = 'type(service),tag(\\"[Kubernetes]namespace:et\\"),tag(\\"Environment:Preview\\"),entityId(\\"SERVICE-85A65FBFF3C9F37E\\")'
//AAT Config
this.dynatraceSyntheticTestAAT = "HTTP_CHECK-8BD69A31834A24FC"
this.dynatraceDashboardIdAAT = "a529a685-8c36-4e8c-8137-67de8bfcf104"
this.dynatraceDashboardURLAAT = "https://yrk32651.live.dynatrace.com/#dashboard;gtf=-2h;gf=all;id=a529a685-8c36-4e8c-8137-67de8bfcf104"
this.dynatraceEntitySelectorAAT = 'type(service),tag(\\"[Kubernetes]namespace:et\\"),tag(\\"Environment:AAT\\"),entityId(\\"SERVICE-A930A031C9C0359E\\")'
//Perftest Config
this.dynatraceSyntheticTest = "HTTP_CHECK-0A7CF661E2924918"
this.dynatraceDashboardIdPerfTest = "a4576442-06a9-4a76-baa5-5342a525679f"
this.dynatraceDashboardURLPerfTest = "https://yrk32651.live.dynatrace.com/#dashboard;id=a4576442-06a9-4a76-baa5-5342a525679f;applyDashboardDefaults=true"
this.dynatraceEntitySelectorPerfTest = 'type(service),tag(\\"[Kubernetes]namespace:et\\"),tag(\\"Environment:PERF\\"),entityId(\\"SERVICE-894163B308FBDD78\\")'

echo "Completed Config Load..." 

return this
