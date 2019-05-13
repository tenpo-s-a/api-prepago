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

-- // create_table_prp_cuenta
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema}.prp_cuenta (
  id                BIGSERIAL NOT NULL,
  uuid              VARCHAR(50) NOT NULL DEFAULT uuid_generate_v4()::VARCHAR,
  id_usuario        BIGINT REFERENCES  ${schema}.prp_usuario,
  cuenta            VARCHAR(100) NOT NULL,
  procesador        VARCHAR(30) NOT NULL,
  saldo_info        TEXT DEFAULT '' NOT NULL,
  saldo_expiracion  BIGINT DEFAULT 0 NOT NULL,
  estado            VARCHAR(30) NOT NULL,
  creacion          TIMESTAMP NOT NULL,
  actualizacion     TIMESTAMP NOT NULL,
  CONSTRAINT prp_cuenta_pk PRIMARY KEY(id),
  CONSTRAINT prp_cuenta_u1 UNIQUE(uuid),
  CONSTRAINT prp_cuenta_u2 UNIQUE(cuenta,procesador)
);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS ${schema}.prp_cuenta CASCADE;

DROP EXTENSION  IF EXISTS "uuid-ossp";
