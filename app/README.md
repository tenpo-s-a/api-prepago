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
