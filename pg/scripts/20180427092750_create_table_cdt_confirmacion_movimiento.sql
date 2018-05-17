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

-- // create_table_cdt_confirmacion_movimiento
-- Migration SQL that makes the change goes here.
  CREATE TABLE ${schema.cdt}.cdt_confirmacion_movimiento (
      id_mov_cuenta_origen          BIGINT REFERENCES ${schema.cdt}.cdt_movimiento_cuenta(id) ,
      id_mov_cuenta_confirmacion    BIGINT REFERENCES ${schema.cdt}.cdt_movimiento_cuenta(id),
      CONSTRAINT cdt_confirmacion_movimiento_pk PRIMARY KEY(id_mov_cuenta_origen,id_mov_cuenta_confirmacion),
      CONSTRAINT cdt_confirmacion_movimiento_u1 UNIQUE (id_mov_cuenta_origen)
  );

-- //@UNDO
-- SQL to undo the change goes here.
  DROP TABLE IF EXISTS ${schema.cdt}.cdt_confirmacion_movimiento;
