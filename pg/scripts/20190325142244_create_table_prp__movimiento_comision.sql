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

-- // create_table_prp_ movimiento_comision
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema}.prp_movimiento_comision (
  id                BIGSERIAL NOT NULL,
  id_movimiento     BIGINT REFERENCES ${schema}.prp_movimiento(id),
  tipo_comision     VARCHAR(20) NOT NULL,
  monto             NUMERIC(17,2) NOT NULL,
  iva               NUMERIC(17,2) NOT NULL,
  creacion          TIMESTAMP NOT NULL,
  actualizacion     TIMESTAMP NOT NULL,
  CONSTRAINT prp_movimiento_comision_pk PRIMARY KEY(id),
  CONSTRAINT prp_movimiento_comision_u1 UNIQUE(id_movimiento,tipo_comision)
);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema}.prp_movimiento_comision CASCADE;
