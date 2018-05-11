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

-- // create_table_prp_tarjeta
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema}.prp_tarjeta (
  id                  BIGSERIAL NOT NULL,
  id_usuario          BIGINT NOT NULL,
  pan                 VARCHAR(16) NOT NULL,
  pan_encriptado      VARCHAR(100) NOT NULL,
  contrato            VARCHAR(20) NOT NULL,
  fecha_expiracion    INTEGER NOT NULL,
  estado              VARCHAR(10) NOT NULL,
  nombre_tarjeta      VARCHAR(100) NOT NULL,
  fecha_creacion      TIMESTAMP NOT NULL,
  fecha_actualizacion TIMESTAMP NOT NULL,
  CONSTRAINT prp_tarjeta_u1 UNIQUE(id),
  CONSTRAINT prp_tarjeta_u2 UNIQUE(id, id_usuario)
);

COMMENT ON CONSTRAINT prp_tarjeta_u1 ON ${schema}.prp_tarjeta IS 'El id no puede repetirse';
COMMENT ON CONSTRAINT prp_tarjeta_u2 ON ${schema}.prp_tarjeta IS 'El id junto al id_usuario no puede repetirse';

CREATE INDEX prp_tarjeta_i1 ON ${schema}.prp_tarjeta (id);
--COMMENT ON INDEX prp_tarjeta_i1 IS 'Permite realizar la busqueda por id';

CREATE INDEX prp_tarjeta_i2 ON ${schema}.prp_tarjeta (id_usuario);
--COMMENT ON INDEX prp_tarjeta_i2 IS 'Permite realizar la busqueda por id_usuario';

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS ${schema}.prp_tarjeta CASCADE;


