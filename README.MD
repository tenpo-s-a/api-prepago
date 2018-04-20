
# Requisitos

- Ubuntu 16.04
- Postgresql 9.6
- kong 0.13.0
- jenkins 2.107.1
- java 1.8
- Payara Server 5.181 Full

# Instalacion de postgresql

## Ubuntu 12.04, 14.04, 16.04
```sh
$ sudo add-apt-repository "deb http://apt.postgresql.org/pub/repos/apt/ xenial-pgdg main"
$ wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
$ sudo apt-get update
$ sudo apt-get install postgresql-9.6
```

## Ubuntu 17.04 - 17.10
```sh
$ sudo apt-get install postgresql-9.6
```


# Configuración de postgresql

## Cambiar clave de usuario postgres:

1. conectarse
    
    ```sh
    $ sudo -u postgres psql postgres
    ```

2. cambiar la clave, usar clave: postgres
    ```sh
    $ postgres=# \password postgres
    Enter new password: postgres
    ```

3. Salir de la consola postgresql con \q
    ```sh
    $ postgres=# \q
    ```

# Instalación de kong

## Descargar kong

- ir al sitio y descargar kong:    https://getkong.org/install/ubuntu/

- archivo para ubuntu 16.04: https://bintray.com/kong/kong-community-edition-deb/download_file?file_path=dists/kong-community-edition-0.13.0.xenial.all.deb

## Instalar kong y sus dependencias

```sh
$ sudo apt-get update
$ sudo apt-get install openssl libpcre3 procps perl
$ sudo dpkg -i kong-community-edition-0.13.0.*.deb
```

# Configurar kong

## Creación de usuario y base de datos kong en postgresql

1. Conectarse a postgresql
    ```sh
    $ sudo -u postgres psql postgres
    ```
2. Crear usuario, crear base de datos y establecer clave
    ```sql
    CREATE USER kong; CREATE DATABASE kong OWNER kong;
    ```
    ```sql
    ALTER USER kong WITH PASSWORD 'kong';
    ```

## Configurar kong

1. Crear archivo de configuración a partir del por defecto
    ```sh
    $ cp /etc/kong/kong.conf.default /etc/kong/kong.conf
    ```

2. Descomentar y editar las siguientes configuraciones en el archivo /etc/kong/kong.conf

        database = postgres             
        pg_host = 127.0.0.1
        pg_port = 5432
        pg_user = kong
        pg_password = kong
        pg_database = kong

3. Ejecutar migraciones de kong
    ```sh
    $ kong migrations up -c /etc/kong/kong.conf
    ```

# Iniciar / detener kong

## Iniciar kong
```sh
$ sudo kong start -c /etc/kong/kong.conf
```
## Detener kong
```sh
$ sudo kong stop
```
## Verificar si kong se encuentra en ejecución
```sh
$ curl -i http://localhost:8001/
```

# Instalación de jenkins (Se puede instalar de 2 formas como servicio o como aplicación war)

## Instalar jenkins como aplicación war

1. Descargar jenkins
    ```sh
    $ wget http://mirrors.jenkins.io/war-stable/latest/jenkins.war
    ```
2. Ejecutar jenkins
    ```sh
    > java -jar jenkins.war --httpPort=8099
    ```
## Instalar jenkins como servicio

1. Descargar e instalar jenkins
    ```sh
    $ wget https://pkg.jenkins.io/debian-stable/binary/jenkins_2.89.4_all.deb        
    $ sudo dpkg -i jenkins_2.89.4_all.deb
    ```
2. Establecer puerto 8090

    - Editar archivo __/etc/default/jenkins__ y agregar _--httpPort=8099_ a __JENKINS_ARGS__, luego reiniciar el servicio

3. Reiniciar servicio de jenkins
    ```sh
    $ sudo service jenkins restart
    ```
# Configuracion de jenkins

## Cuando se ejecuta por primera vez jenkins dará por consola un password de administración el cual se deberá
    ingresar la primera vez que se visite jenkins

1. Acceder a jenkins

    - ir a: http://localhost:8099

2. Instalar plugins

    - Presionar la opción: Intall suggested plugins

3. Crear usuario de jenkins

    - Ingresar los datos:

                Username: jenkins 
                Password: jenkins
                Confirm Password: jenkins
                E-amil address: tu_email

4. Presionar el boton: Save and Finish

# Configuración y ejecición de pipeline local

1. Acceder a jenkins

    - Ir a: http://localhost:8099

2. Crear pipeline

    - Hacer click en __`New Item`__

    - Ingresar el nombre del pipeline

    - Seleccionar el tipo __`Pipeline`__ 

    - Hacer click en el boton __`Ok`__

    
3. Configurar el pipeline

    - General

        - Seleccionar `This project is parameterized` y agregar el parametro __PROJECT_FOLDER_PATH__, en la que se colocara como valor por defecto el path absoluto de la carpeta en la cual se encuentra el proyecto

    - Pipeline
        
        - Copiar el contenido del archivo __local_jenkinsfile__ en el input _Script_.

        - Salvar los cambios haciendo click en el boton `Save`
    
4. Ejecutar el pipeline de forma manual
        
    - hacer click en `Build with Parameters`
    
    - de ser necesario, podemos cambiar el path de la carpeta

    - hacer click en el boton `Build`

# Creación de las base de datos

1. Conectarse a postgresql
    ```sh
    $ sudo -u postgres psql postgres
    ```
2. Crear las base de datos
    ```sql
    CREATE DATABASE prepago_api_dev OWNER postgres;
    ```
    ```sql
    CREATE DATABASE prepago_api_test OWNER postgres;
    ```

# Instalación y configuración de Payara

    > Descargar payara server full desde: https://www.payara.fish/downloads
    > Descomprimir en algun directorio, ej: /home/usuario/dev

## Configuración variables de entorno para payara

    Agregar las siguientes variables de entorno y alias (Es importante que la ruta sea donde se instalo payara)

    export PAYARA="/home/usuario/dev/payara5"
    export PATH=$PAYARA/bin:$PATH
    alias payara-start='asadmin start-domain --verbose'

## Iniciar payara server

    > payara-start

## Detener payara server

    > Presionar ctrl+x en la consola de ejecución de payara

## Instalación del driver postgresql para payara

    > Copiar el archivo "postgresql-42.2.2.jre7.jar" desde el directorio "pg/pg-prepaid/drivers" al directorio de payara server: /home/usuario/dev/payara5/glassfish/domains/domain1/lib

    > Reiniciar el servidor Payara

## Crear el datasource en payara

    > iniciar payara: payara-start
    > ir a la url: http://localhost:4848/common/index.jsf
    > ir a: Resources > JDBC > JDBC Connection Pools > Botón [New...]

        Step 1 of 2:

            Establecer los siguientes datos:

            Pool Name: prepagoPoolDS
            Resource Type: javax.sql.ConnectionPoolDataSource
            Database Driver Vendor: Postgresql

            Presionar botón [Next]

        Step 2 of 2:

            En tabla: Additional Properties, establecer las siguientes propiedades:

            DatabaseName: prepago_api_dev
            PortNumber: 5432
            User: postgres
            Password: postgres
            URL: jdbc:postgresql://localhost/prepago_api_dev
            Url: jdbc:postgresql://localhost/prepago_api_dev

            Presionar botón [Finish]

    > ir a: Resources > JDBC > JDBC Resources > Bóton [New...]

            Establecer los siguientes datos:

            JNDI Name: jdbc/prepagoDS
            Pool Name: prepagoPoolDS

            Presionar botón [OK]

## Configurar el embiente al cual será destinado el servidor Payara

    > ir a: Configurations > server-config > System Properties > Bóton [Add Property]

        Agregar la propiedad env con algunos de los ambientes: development, production:

        env: development

        Presiona botón [Save]
