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

-- // create_table_mc_parametro
-- Migration SQL that makes the change goes here.

CREATE SCHEMA IF NOT EXISTS ${schema.parameters};

CREATE TABLE ${schema.parameters}.mc_parametro (
  id                  BIGSERIAL NOT NULL,
  aplicacion          VARCHAR(25) NOT NULL,
  nombre              VARCHAR(100) NOT NULL,
  version             VARCHAR(5) NOT NULL,
  valor               JSON NOT NULL,
  expiracion          BIGINT NOT NULL,
  fecha_creacion      TIMESTAMP NOT NULL,
  CONSTRAINT mc_parametro_pk PRIMARY KEY (id),
  CONSTRAINT mc_parametro_u1 UNIQUE(aplicacion, nombre, version)
);

COMMENT ON COLUMN  ${schema.parameters}.mc_parametro.aplicacion IS 'La aplicacion que utilizara este parametro';
COMMENT ON COLUMN  ${schema.parameters}.mc_parametro.nombre IS 'El nombre del parametro';
COMMENT ON COLUMN  ${schema.parameters}.mc_parametro.version IS 'La version del parametro';
COMMENT ON COLUMN  ${schema.parameters}.mc_parametro.expiracion IS 'Tiempo de expiracion utilizado por el cache. En Milisegundos';
COMMENT ON COLUMN  ${schema.parameters}.mc_parametro.valor IS 'Valor del parametro en formato JSON';

COMMENT ON CONSTRAINT mc_parametro_u1 ON ${schema.parameters}.mc_parametro IS 'La aplicacion junto al nombre y a la version no puede repetirse';

CREATE INDEX mc_parametro_i1 ON ${schema.parameters}.mc_parametro (aplicacion);
CREATE INDEX mc_parametro_i2 ON ${schema.parameters}.mc_parametro (nombre);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS ${schema.parameters}.mc_parametro CASCADE;

DROP SCHEMA ${schema.parameters} CASCADE;
