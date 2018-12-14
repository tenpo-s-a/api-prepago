#!/bin/bash

mvn clean package
mvn test -DskipTests=false -P integration-test $1 $2 $3