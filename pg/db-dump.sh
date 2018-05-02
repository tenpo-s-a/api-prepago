#!/bin/bash
##########################################################
#
# Script que permite hacer un dump de una base de datos
#
##########################################################

if [ -z "$1" ]
then
  if [ -f "dump.sql" ]
  then
    echo "Debe ingresar nombre de la base de datos a cual se cargara el dump"
  else
    echo "Debe ingresar nombre de la base de datos desde cual desea hacer el dump"
  fi
else
  db_name="$1"
  echo "Base de datos: $db_name"
  if [ -f "dump.sql" ]
  then
    echo "Cargando dump en la base de datos: $db_name"
    psql -U postgres -W postgres -w -d $db_name -a -f dump.sql
  else
    echo "Creando archivo dump.sql"
    pg_dump $db_name > dump.sql -U postgres
  fi
fi
