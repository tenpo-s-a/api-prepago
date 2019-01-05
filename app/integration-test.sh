#!/bin/bash

mvn clean package
mvn test --quiet -DskipTests=false -P integration-test $1 $2 $3