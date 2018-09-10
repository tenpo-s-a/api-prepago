FROM payara/micro:latest

# Setup Configuration
USER payara

COPY postgresql-42.2.2.jre7.jar /opt/payara5/glassfish/domains/domain1/lib

COPY domain.xml /opt/payara5/glassfish/domains/domain1/config
