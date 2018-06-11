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

-- // create_table_cdt_fase_acumulador
-- Migration SQL that makes the change goes here.

  CREATE TABLE ${schema.cdt}.cdt_fase_acumulador (
      id_fase_movimiento        BIGINT REFERENCES ${schema.cdt}.cdt_fase_movimiento(id) ,
      id_regla_acumulacion      BIGINT REFERENCES ${schema.cdt}.cdt_regla_acumulacion(id),
      signo                     NUMERIC(1) NOT NULL,
      CONSTRAINT cdt_categoria_mov_fase_pk PRIMARY KEY(id_fase_movimiento,id_regla_acumulacion)
  );

-- //@UNDO
-- SQL to undo the change goes here.
  DROP TABLE IF EXISTS ${schema.cdt}.cdt_fase_acumulador;
