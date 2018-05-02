# Operar con las migraciones

## Parametro env

  Algunos script soportan el envio del ambiente de ejecución mediante el parametro --env, o -Denv
  Ambientes disponibles: test, development, production, jenkins

## Crear base de datos

  > ./db-create.sh [--env]

  Ejemplo: Creará la base de datos definida para el ambiente definido en el parametro "development"

  > ./db-create.sh --env=development

## Borrar base de datos

  > ./db-drop.sh [--env]

  Ejemplo: Borrará la bade de datos definida para el ambiente definido en el parametro "development"

  > ./db-drop.sh --env=development

## Ver estado de migraciones

  > ./migrate-status.sh [--env]

  Ejemplo: Mostrará el estado de las migraciones en el ambiente definido en el parametro "development"

    > ./migrate-status.sh --env=development

## Ejecucion de migraciones

  > ./migrate-up.sh [--env]

  Ejemplo: Ejecutará las migraciones en el ambiente definido en el parametro "development"

    > ./migrate-up.sh --env=development

## Rollback de migraciones

  > ./migrate-down.sh [--env] [steps]

  Ejemplo 1: Realizará rollback de la última migración en el ambiente definido en el parametro "development"

      > ./migrate-down.sh --env=development

  Ejemplo 2: Realizará rollback de las últimas 2 migraciones en el ambiente definido en el parametro "development"

        > ./migrate-down.sh --env=development 2

## Crear nueva migración

  > ./migrate-new.sh NOMBRE_DE_LA_MIGRACION

  Ejemplo 1: Creara una migracion con e nombre "create_table_users"

    > ./migrate-new.sh create_table_users

# Ejecución de test

  > ./test.sh [-Denv]

  Ejemplo: Ejecutar los test en el ambiente definido en el parametro "development"

    > ./test.sh -Denv=development
