#!/bin/bash

mvn clean
mvn test --quiet -DskipTests=false $1 $2 $3
