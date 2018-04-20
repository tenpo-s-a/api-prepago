#!/bin/bash
##########################################################
#
# Script que permite borrar la base de datos para jenkins
# y elimina los archivos jenkins.properties temporales
#
##########################################################

if [ -f "db-id.txt" ]
then
    id=$(cat db-id.txt)
    echo "Borrando base de datos: $id"
    dropdb $id -U postgres
    rm db-id.txt
else
	echo "No existe el archivo db-id.txt"
fi

pg_jenkins_properties="../pg/environments/jenkins.properties"
app_jenkins_properties="../app/src/main/resources/jenkins.properties"
echo "Borrando archivo de test en jenkins: $pg_jenkins_properties"
rm $pg_jenkins_properties
echo "Borrando archivo de test en jenkins: $app_jenkins_properties"
rm $app_jenkins_properties
