#!/bin/bash

mvn exec:java -Dexec.mainClass="cl.multicaja.core.apps.MainDropDatabase" -Dexec.args="$1 $2 $3 $4 $5 $6 $7 $8 $9"
