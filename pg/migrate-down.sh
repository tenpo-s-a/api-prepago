#!/bin/bash

mvn exec:java -Dapp.pid="$$" -Dexec.mainClass="cl.multicaja.core.apps.MigratorWrapper" -Dexec.args="down $1 $2 $3 $4 $5 $6 $7 $8 $9"
