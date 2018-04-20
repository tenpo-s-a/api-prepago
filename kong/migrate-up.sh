#!/bin/bash

mvn clean compile
mvn exec:java -Dexec.mainClass="cl.multicaja.prepago.kong.ExecuteMigrations" -Dexec.args="$1 $2 $3 $4 $5 $6 $7 $8 $9"
