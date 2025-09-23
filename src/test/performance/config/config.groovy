// ====== Set up default values ======
this.dynatraceMetricType = 'et-sya-api'
this.dynatraceMetricTag = 'namespace:et'
//Preview Config
this.dynatraceSyntheticTestPreview = "HTTP_CHECK-467146A25406C026"
this.dynatraceDashboardIdPreview = "f44ff1f7-e13a-4a6a-95b9-4e3ab44e6587"
this.dynatraceDashboardURLPreview = "https://yrk32651.live.dynatrace.com/#dashboard;id=f44ff1f7-e13a-4a6a-95b9-4e3ab44e6587;applyDashboardDefaults=true"
this.dynatraceEntitySelectorPreview = 'type(service),tag(\\"[Kubernetes]namespace:et\\"),tag(\\"Environment:PREVIEW\\"),entityId(\\"SERVICE-A1223BBF43F01226\\")' //SERVICE-85A65FBFF3C9F37E //SERVICE-94E67F588E12D9D6
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
