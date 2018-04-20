#!/bin/bash

mvn clean compile
mvn exec:java -Dexec.mainClass="cl.multicaja.prepago.kong.CreateMigration"
