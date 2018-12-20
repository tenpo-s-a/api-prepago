FROM mccontainerregistry.azurecr.io/payara-py:1.0.0

# Setup Configuration
USER payara
COPY ./pg/drivers/postgresql-42.2.2.jre7.jar /opt/payara5/glassfish/domains/domain1/lib
COPY ./domain.xml /opt/payara5/glassfish/domains/domain1/config
#COPY api-users WAR
COPY ./app/target/*.war /opt/payara5/glassfish/domains/domain1/autodeploy