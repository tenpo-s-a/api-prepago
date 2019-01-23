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

-- // create_table_conciliacion
-- Migration SQL that makes the change goes here.

 CREATE TABLE ${schema}.prp_conciliaciones (
      id                    BIGSERIAL NOT NULL,
      id_movimiento         BIGINT  REFERENCES ${schema}.prp_movimiento(id),
      tipo                  VARCHAR(20) NOT NULL,
      status                VARCHAR(20) NOT NULL,
      created               TIMESTAMP NOT NULL,
      updated               TIMESTAMP NOT NULL,
      CONSTRAINT conciliaciones_pk PRIMARY KEY(id),
      CONSTRAINT conciliaciones_u1 UNIQUE(id_movimiento,tipo)
  );
  CREATE INDEX conciliaciones_i1 ON ${schema}.prp_conciliaciones (tipo);

-- //@UNDO
-- SQL to undo the change goes here.
  DROP TABLE IF EXISTS ${schema}.prp_conciliaciones;
