#!/bin/bash

mvn clean compile
mvn exec:java -Dexec.mainClass="cl.multicaja.core.kong.CreateMigration"
