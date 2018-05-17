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

-- // create_table_cdt_cuenta
-- Migration SQL that makes the change goes here.
  CREATE SCHEMA IF NOT EXISTS ${schema.cdt};


  CREATE TABLE ${schema.cdt}.cdt_cuenta (
      id              BIGSERIAL NOT NULL,
      id_externo      VARCHAR(50) NOT NULL,
      descripcion     VARCHAR(100) NOT NULL,
      estado          VARCHAR(10) NOT NULL,
      fecha_estado    TIMESTAMP NOT NULL,
      fecha_creacion  TIMESTAMP NOT NULL,
      CONSTRAINT cdt_cuenta_pk PRIMARY KEY(id),
      CONSTRAINT cdt_cuenta_u1 UNIQUE (id_externo)
  );
  CREATE INDEX cdt_cuenta_i1 ON ${schema.cdt}.cdt_cuenta (estado);

-- //@UNDO
-- SQL to undo the change goes here.
  DROP TABLE IF EXISTS ${schema.cdt}.cdt_cuenta;

  DROP SCHEMA IF EXISTS ${schema.cdt};

