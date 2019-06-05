FROM openjdk:8u171-jdk AS build-env

ARG TECNOCOM_CERT_NAME
COPY ./cert cert
RUN apt-get update && apt-get install ca-certificates
RUN cat ./cert | base64 -d > ${TECNOCOM_CERT_NAME}.crt && \
  cp ${TECNOCOM_CERT_NAME}.crt /usr/local/share/ca-certificates && \
  update-ca-certificates

FROM payara/server-full:5.181

COPY --from=build-env /etc/ssl/certs /etc/ssl/certs

# Setup Configuration
USER payara
COPY ./pg/drivers/postgresql-42.2.2.jre7.jar /opt/payara5/glassfish/domains/domain1/lib

COPY ./app/target/*.war /opt/payara5/glassfish/domains/domain1/autodeploy
COPY --chown=payara ./mideu.yml /opt/payara5
COPY --chown=payara ./mock-data/datos-mock.cache /opt/payara5

ENTRYPOINT ${PAYARA_PATH}/generate_deploy_commands.sh && echo 'create-jdbc-connection-pool --datasourceclassname org.postgresql.ds.PGConnectionPoolDataSource --restype javax.sql.ConnectionPoolDataSource --property user=${DB_USERNAME}:password=${DB_PASSWORD}:DatabaseName=${DB_NAME}:ServerName=${DB_HOST_FQDN}:port=5432 prepagoPoolDS' >> mycommands.asadmin && echo 'create-jdbc-resource --connectionpoolid prepagoPoolDS jdbc/prepagoDS' >> mycommands.asadmin && echo 'create-system-properties env=${API_ENVIRONMENT}' >> mycommands.asadmin && cat ${DEPLOY_COMMANDS} >> mycommands.asadmin && ${PAYARA_PATH}/bin/asadmin start-domain -v --postbootcommandfile mycommands.asadmin ${PAYARA_DOMAIN}
