#!/bin/bash

if [ -f "db-id.txt" ]
then
    id=$(cat db-id.txt)
    echo "Creando base de datos: $id"
    createdb $id -U postgres -W postgres -O postgres -w

    #si existe un archivo dump.sql lo ejecuta sobre la base de datos nueva
    if [ -f "dump.sql" ]
    then
      echo "Cargando datos en la base de datos: $id"
        psql -U postgres -W postgres -w -d $id -a -f dump.sql
    else
      echo "No existe el archivo dump.sql"
    fi
else
	echo "No existe el archivo db-id.txt"
fi
