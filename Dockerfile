FROM mccontainerregistry.azurecr.io/payara-py:1.0.0

# Setup Configuration
USER payara
COPY ./pg/drivers/postgresql-42.2.2.jre7.jar /opt/payara5/glassfish/domains/domain1/lib

COPY ./app/target/*.war /opt/payara5/glassfish/domains/domain1/autodeploy
COPY ./mideu.yml /opt/payara5

ENTRYPOINT ${PAYARA_PATH}/generate_deploy_commands.sh && echo 'create-jdbc-connection-pool --datasourceclassname org.postgresql.ds.PGConnectionPoolDataSource --restype javax.sql.ConnectionPoolDataSource --property user=${DB_USERNAME}:password=${DB_PASSWORD}:DatabaseName=${DB_NAME}:ServerName=${DB_HOST_FQDN}:port=5432 prepagoPoolDS' >> mycommands.asadmin && echo 'create-jdbc-resource --connectionpoolid prepagoPoolDS jdbc/prepagoDS' >> mycommands.asadmin && echo 'create-system-properties env=${API_ENVIRONMENT}' >> mycommands.asadmin && cat ${DEPLOY_COMMANDS} >> mycommands.asadmin && ${PAYARA_PATH}/bin/asadmin start-domain -v --postbootcommandfile mycommands.asadmin ${PAYARA_DOMAIN}
