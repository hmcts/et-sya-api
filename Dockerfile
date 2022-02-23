ARG APP_INSIGHTS_AGENT_VERSION=3.2.4
FROM hmctspublic.azurecr.io/base/java:11-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/et-sya-api.jar /opt/app/

EXPOSE 4550
CMD [ "et-sya-api.jar" ]
