#!/bin/bash

export MIGRATIONS="./mybatis-migrations-3.3.3"
export PATH=$PATH:$MIGRATIONS/bin

echo "MyIbatis migrations: $MIGRATIONS"

migrate status $1 $2 $3
