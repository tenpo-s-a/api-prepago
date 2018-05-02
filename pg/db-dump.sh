#!/bin/bash
##########################################################
#
# Script que permite hacer un dump de una base de datos
#
##########################################################

db_name="NOMBRE_BASE_DE_DATOS"
pg_dump $db_name > dump.sql -U postgres
