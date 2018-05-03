# Operar con las migraciones

## Parametro env

  Algunos script soportan el envio del ambiente de ejecución mediante el parametro --env, o -Denv
  Ambientes disponibles: test, development, production, jenkins

## Crear base de datos

  > ./db-create.sh [-Denv]

  Ejemplo: Creará la base de datos definida para el ambiente definido en el parametro "development"

  > ./db-create.sh -Denv=development

## Borrar base de datos

  > ./db-drop.sh [-Denv]

  Ejemplo: Borrará la bade de datos definida para el ambiente definido en el parametro "development"

  > ./db-drop.sh -Denv=development
  
## Ver estado de migraciones

  > ./migrate-status.sh [-Denv]

  Ejemplo: Mostrará el estado de las migraciones en el ambiente definido en el parametro "development"

    > ./migrate-status.sh -Denv=development

## Ejecucion de migraciones

  > ./migrate-up.sh [-Denv]

  Ejemplo: Ejecutará las migraciones en el ambiente definido en el parametro "development"

    > ./migrate-up.sh -Denv=development

## Rollback de migraciones

  > ./migrate-down.sh [-Denv] [steps]

  Ejemplo 1: Realizará rollback de la última migración en el ambiente definido en el parametro "development"

      > ./migrate-down.sh -Denv=development

  Ejemplo 2: Realizará rollback de las últimas 2 migraciones en el ambiente definido en el parametro "development"

        > ./migrate-down.sh -Denv=development 2

## Crear nueva migración

  > ./migrate-new.sh NOMBRE_DE_LA_MIGRACION

  Ejemplo 1: Creara una migracion con e nombre "create_table_users"

    > ./migrate-new.sh create_table_users

# Ejecución de test

  > ./test.sh [-Denv]

  Ejemplo: Ejecutar los test en el ambiente definido en el parametro "development"

    > ./test.sh -Denv=development
