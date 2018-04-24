#!/bin/bash

mvn clean package
mvn test -DskipTests=false $1 $2 $3
