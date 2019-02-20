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

-- // create_table_prp_switch_movements_hist
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema}.prp_movimiento_switch_hist (
  id                BIGSERIAL NOT NULL,
  id_archivo        BIGINT NOT NULL,
  id_multicaja      VARCHAR(50) NOT NULL,
  id_cliente        BIGINT NOT NULL,
  id_multicaja_ref  BIGINT,
  monto             NUMERIC NOT NULL,
  fecha_trx         TIMESTAMP NOT NULL,
  CONSTRAINT prp_movimiento_switch_hist_pk PRIMARY KEY(id)
);
CREATE INDEX prp_movimiento_switch_hist_i1 ON ${schema}.prp_movimiento_switch_hist (id_archivo);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema}.prp_movimiento_switch_hist;


