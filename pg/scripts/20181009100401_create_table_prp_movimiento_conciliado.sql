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

-- // create_table_prp_movimiento_conciliado
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema}.prp_movimiento_conciliado
(
    id                BIGSERIAL   NOT NULL,
    id_mov_ref        BIGINT      REFERENCES ${schema}.prp_movimiento(id),
    fecha_registro    TIMESTAMP   NOT NULL,
    accion            VARCHAR(50) NOT NULL,
    estado            VARCHAR(50) NOT NULL,
    CONSTRAINT prp_movimiento_conciliado_pk PRIMARY KEY(id)
);
CREATE INDEX prp_movimiento_conciliado_i1 ON ${schema}.prp_movimiento_conciliado (id);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema}.prp_movimiento_conciliado;


