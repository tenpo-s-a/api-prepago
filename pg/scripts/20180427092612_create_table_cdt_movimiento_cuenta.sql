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

-- // create_table_cdt_movimiento_cuenta
-- Migration SQL that makes the change goes here.




  CREATE TABLE ${schema.cdt}.cdt_movimiento_cuenta(
      id                  BIGSERIAL NOT NULL,
      id_cuenta           BIGINT REFERENCES ${schema.cdt}.cdt_cuenta(id),
      id_fase_movimiento  BIGINT REFERENCES ${schema.cdt}.cdt_fase_movimiento(id),
      id_mov_referencia   BIGINT NOT NULL,
      id_tx_externo       VARCHAR(50) NOT NULL,
      glosa               VARCHAR(100) NOT NULL,
      monto               NUMERIC NOT NULL,
      fecha_registro      TIMESTAMP NOT NULL,
      estado              VARCHAR(10) NOT NULL,
      fecha_estado        TIMESTAMP NOT NULL,
      fecha_tx            DATE NOT NULL,
      CONSTRAINT cdt_movimiento_cuenta_pk PRIMARY KEY(id),
      CONSTRAINT cdt_movimiento_cuenta_u1 UNIQUE(id_fase_movimiento,id_tx_externo,fecha_tx)
  );

  CREATE INDEX cdt_movimiento_cuenta_i1 ON ${schema.cdt}.cdt_movimiento_cuenta (id_fase_movimiento);
-- //@UNDO
-- SQL to undo the change goes here.
  DROP TABLE IF EXISTS ${schema.cdt}.cdt_movimiento_cuenta;
