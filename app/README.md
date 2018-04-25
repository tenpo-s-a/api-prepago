# Crear artefactos .war y .jar

  > ./package.sh

# Crear artefactos .war y .jar e instalarlos en el repositorio local maven 

  ### Al instalarlos en maven quedan disponibles para ser usados por otros proyectos

  > ./install.sh

# Desplegar en servidor payara

  > ./deploy.sh

# Ejecutar test

  > ./test.sh [-Denv]

  Ejemplo: Ejecutar los test en el ambiente definido en el parametro "test"

  > ./test.sh -Denv=test

## Parametros de test.sh

  - env: OBLIGATORIO Ambiente test, development, production, jenkins u otro
  - api_host: OPCIONAL: nombre del host donde se encuentra el api, por defecto es http://localhost
  
  ### IMPORTANTE:
  
  Los ambientes de ejecución definen los lugares donde se ejecuta el api, servidor y puerto
  
  - test: Ambientes test o test-* indican que el api se ejecutará en un servidor embebido con un puerto aleatorio
  - jenkins: Ambiente jenkins indica que el api se ejecutará en un servidor embebido con un puerto aleatorio
  - development: Ambientes development o development-* indican que el api se ejecuta en un servidor local o externo generalmente en el pueto 8080
  - production: Ambientes production o production-* indican que el api se ejecuta en un servidor local o externo generalmente en el pueto 8080
  
  ### Ejemplos:
  
    Ejecuta los test en el ambiente "test", por defecto al "api_host" http://localhost + puerto aleatorio
    
      > ./test.sh -Denv=test
    
    Ejecuta los test en el ambiente "test", reescribiendo el "api_host" a http://127.0.0.1 + puerto aleatorio
    
      > ./test.sh -Denv=test -Dapi_host=http://127.0.0.1
    
    Ejecuta los test en el ambiente "development" por defecto apuntando el "api_host" a http://localhost:8080
    
      > ./test.sh -Denv=development
    
    Ejecuta los test en el ambiente "development", reescribiendo el "api_host" a http://otramaquina:9090
          
      > ./test.sh -Denv=development -Dapi_host=http://otramaquina:9090
