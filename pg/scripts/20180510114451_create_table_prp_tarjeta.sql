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

-- // create_table_prp_tarjeta
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema}.prp_tarjeta (
  id                  BIGSERIAL NOT NULL,
  id_cuenta           BIGINT NOT NULL REFERENCES ${schema}.prp_cuenta(id),
  pan                 VARCHAR(16) NOT NULL,
  pan_encriptado      VARCHAR(100) NOT NULL,
  pan_hash            VARCHAR(200) NOT NULL DEFAULT '',
  estado              VARCHAR(20) NOT NULL,
  nombre_tarjeta      VARCHAR(100) NOT NULL,
  producto            VARCHAR(2) NOT NULL,
  numero_unico        VARCHAR(8) NOT NULL,
  uuid                VARCHAR(50) NOT NULL DEFAULT uuid_generate_v4()::VARCHAR,
  fecha_creacion      TIMESTAMP NOT NULL,
  fecha_actualizacion TIMESTAMP NOT NULL,
  CONSTRAINT prp_tarjeta_pk PRIMARY KEY(id)
);
CREATE INDEX prp_tarjeta_i1 ON ${schema}.prp_tarjeta (id);

CREATE INDEX prp_tarjeta_i3 ON ${schema}.prp_tarjeta (estado);
-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS ${schema}.prp_tarjeta CASCADE;


