#!/bin/bash

cd ./libs
jar xf multicaja-base-1.0.0.jar META-INF/maven/cl.multicaja/multicaja-base/pom.xml
mv  META-INF/maven/cl.multicaja/multicaja-base/pom.xml .
rm -rf META-INF
cd ..
mvn install:install-file -Dfile=./libs/multicaja-base-1.0.0.jar -DgroupId=cl.multicaja -DartifactId=multicaja-base -Dversion=1.0.0 -Dpackaging=jar -DpomFile=./libs/pom.xml
rm -rf ./libs/pom.xml

cd ./libs
jar xf multicaja-async-1.0.0.jar META-INF/maven/cl.multicaja/multicaja-async/pom.xml
mv  META-INF/maven/cl.multicaja/multicaja-async/pom.xml .
rm -rf META-INF
cd ..
mvn install:install-file -Dfile=./libs/multicaja-async-1.0.0.jar -DgroupId=cl.multicaja -DartifactId=multicaja-async -Dversion=1.0.0 -Dpackaging=jar -DpomFile=./libs/pom.xml
rm -rf ./libs/pom.xml

cd ./libs
jar xf tecnocom-gateway-1.0.jar META-INF/maven/cl.multicaja/tecnocom-gateway/pom.xml
mv  META-INF/maven/cl.multicaja/tecnocom-gateway/pom.xml .
rm -rf META-INF
cd ..
mvn install:install-file -Dfile=./libs/tecnocom-gateway-1.0.jar -DgroupId=cl.multicaja -DartifactId=tecnocom-gateway -Dversion=1.0 -Dpackaging=jar -DpomFile=./libs/pom.xml
rm -rf ./libs/pom.xml