#!/bin/bash


mkdir /drone/src/bitbucket.org/multicajalabs/api-prepago/.m2
mkdir ~/.m2

cat >~/.m2/settings.xml <<EOL
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository>/drone/src/bitbucket.org/multicajalabs/api-prepago/.m2</localRepository>
</settings>
EOL
