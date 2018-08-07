#!/bin/bash

mvn clean
mvn test -DskipTests=false $1 $2 $3
