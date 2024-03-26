ARG APP_INSIGHTS_AGENT_VERSION=3.5.1
FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/et-sya-api.jar /opt/app/

EXPOSE 4550
CMD [ "et-sya-api.jar" ]
