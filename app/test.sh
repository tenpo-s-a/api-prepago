#!/bin/bash

mvn clean compile test -DskipTests=false $1 $2 $3
