#!/bin/bash

mvn exec:java -Dexec.mainClass="cl.multicaja.core.apps.MainDropDatabase" -Dexec.args="$1 $2 $3 $4 $5 $6 $7 $8 $9"

pg_jenkins_properties="./environments/jenkins.properties"
app_jenkins_properties="../app/src/main/resources/jenkins.properties"

echo "Borrando archivo de test en jenkins: $pg_jenkins_properties"
rm $pg_jenkins_properties

echo "Borrando archivo de test en jenkins: $app_jenkins_properties"
rm $app_jenkins_properties
