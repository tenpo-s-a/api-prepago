#!/bin/bash

#se construye el artefacto
mvn clean package -DskipTests=true

#se obtiene el nombre del artefacto a desplegar
artifact=$(ls target/api-**.war)

#se obtiene el nombre del artefacto
name=$(echo $artifact | sed -e "s/target\///") #elimina target/ del string
name=$(echo $name | sed -e "s/.war//") #elimina .war del string

echo "Desplegando [$artifact] con el nombre [$name]"

#Se despliega el artefacto, si falla el redeploy se realiza un deploy
{ #try
 asadmin redeploy --name=$name $artifact
} || { #catch
 asadmin deploy --name=$name $artifact
}
