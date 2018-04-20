# Requisitos

    -java jdk 8
    -maven 3.5.0

# Scripts

    ## Crear una migracion nueva

        > ./migrate-new.sh

    ## Ejecutar migraciones en modo silecioso

        NOTA: Este modo continua ejecutando todas las migraciones si una de ellas falla

        > ./migrate-up.sh -kong_host http://localhost:8001 -api_host http://localhost:3100 -silence true

    ## Ejecutar migraciones en modo no silecioso

        NOTA: Este modo falla completamente si una de las migraciones falla

        > ./migrate-up.sh -kong_host http://localhost:8001 -api_host http://localhost:3100 -silence false

    ## Probar apis desde kong

        > curl -i -X GET 'http://localhost:8000/users/ping' --header 'Host: users-1-0-0'
        > curl -i -X GET 'http://localhost:8000/users/ping' --header 'Host: users-1-0-1'

# Ejecución de test

  ## Ejecutar los test en el kong por defecto http://localhost:8000

    > ./test.sh

  ## Ejecutar los test en el kong pasado como parametro

      > ./test.sh -Dkong_host=http://maquina:puerto
