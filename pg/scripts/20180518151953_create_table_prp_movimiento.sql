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

-- // create_table_prp_movimiento
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema}.prp_movimiento (
  id                  BIGSERIAL NOT NULL,
  id_movimiento_ref   BIGINT NOT NULL,
  id_usuario          BIGINT NOT NULL,
  tipo_movimiento     VARCHAR(10) NOT NULL,
  monto               NUMERIC NOT NULL,
  moneda              VARCHAR(3) NOT NULL,
  fecha_movimiento    TIMESTAMP NOT NULL,
  fecha_creacion      TIMESTAMP NOT NULL
);


CREATE INDEX prp_movimiento_i1 ON ${schema}.prp_usuario (id);
--COMMENT ON INDEX prp_usuario_u2_i1 IS 'Permite realizar la busqueda por id';

CREATE INDEX prp_movimiento_i2 ON ${schema}.prp_usuario (id_usuario);
--COMMENT ON INDEX prp_usuario_i2 IS 'Permite realizar la busqueda por id_usuario_mc';

CREATE INDEX prp_movimiento_i3 ON ${schema}.prp_usuario (id_usuario,tipo_movimiento);
--COMMENT ON INDEX prp_usuario_i2 IS 'Permite realizar la busqueda por id_usuario_mc';

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS ${schema}.prp_movimiento CASCADE;
