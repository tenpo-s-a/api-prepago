--
--    Copyright 2010-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- // create_table_prp_usuario
-- Migration SQL that makes the change goes here.

CREATE SCHEMA IF NOT EXISTS ${schema};

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE ${schema}.prp_usuario (
  id                  BIGSERIAL NOT NULL,
  uuid                VARCHAR(100) NOT NULL DEFAULT '',
  numero_documento    VARCHAR(30) NOT NULL DEFAULT '',
  tipo_documento      VARCHAR(20) NOT NULL DEFAULT '',
  nombre              VARCHAR(30) NOT NULL DEFAULT '',
  apellido            VARCHAR(30) NOT NULL DEFAULT '',
  nivel               VARCHAR(20) NOT NULL DEFAULT '',
  estado              VARCHAR(20) NOT NULL DEFAULT '',
  plan                VARCHAR(20) NOT NULL DEFAULT '',
  fecha_creacion      TIMESTAMP NOT NULL,
  fecha_actualizacion TIMESTAMP NOT NULL,
  CONSTRAINT prp_usuario_pk PRIMARY KEY(id),
  CONSTRAINT prp_usuario_u1 UNIQUE(uuid),
  CONSTRAINT prp_usuario_u2 UNIQUE(numero_documento)
);

CREATE INDEX prp_usuario_i1 ON ${schema}.prp_usuario (id);
--COMMENT ON INDEX prp_usuario_u2_i1 IS 'Permite realizar la busqueda por id';



-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS ${schema}.prp_usuario CASCADE;

DROP EXTENSION  IF EXISTS "uuid-ossp";

DROP SCHEMA IF EXISTS ${schema} CASCADE;

