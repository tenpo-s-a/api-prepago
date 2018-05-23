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

-- // create_table_prp_usuario
-- Migration SQL that makes the change goes here.

CREATE SCHEMA IF NOT EXISTS ${schema};

CREATE TABLE ${schema}.prp_usuario (
  id                  BIGSERIAL NOT NULL,
  id_usuario_mc       BIGINT NOT NULL,
  rut                 INTEGER NOT NULL,
  estado              VARCHAR(10) NOT NULL,
  fecha_creacion      TIMESTAMP NOT NULL,
  fecha_actualizacion TIMESTAMP NOT NULL,
  CONSTRAINT prp_usuario_pk PRIMARY KEY(id),
  CONSTRAINT prp_usuario_u1 UNIQUE(id_usuario_mc),
  CONSTRAINT prp_usuario_u2 UNIQUE(rut),
  CONSTRAINT prp_usuario_u3 UNIQUE(id_usuario_mc, rut)
);
COMMENT ON CONSTRAINT prp_usuario_u1 ON ${schema}.prp_usuario IS 'id_usuario_mc no puede repetirse';
COMMENT ON CONSTRAINT prp_usuario_u2 ON ${schema}.prp_usuario IS 'El rut no puede repetirse';
COMMENT ON CONSTRAINT prp_usuario_u2 ON ${schema}.prp_usuario IS 'El id_usuario_mc junto al rut no puede repetirse';

CREATE INDEX prp_usuario_i1 ON ${schema}.prp_usuario (id);
--COMMENT ON INDEX prp_usuario_u2_i1 IS 'Permite realizar la busqueda por id';

CREATE INDEX prp_usuario_i2 ON ${schema}.prp_usuario (id_usuario_mc);
--COMMENT ON INDEX prp_usuario_i2 IS 'Permite realizar la busqueda por id_usuario_mc';

CREATE INDEX prp_usuario_i3 ON ${schema}.prp_usuario (rut);

--COMMENT ON INDEX prp_usuario_i2 IS 'Permite realizar la busqueda por id_usuario_mc';
CREATE INDEX prp_usuario_i4 ON ${schema}.prp_usuario (estado);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS ${schema}.prp_usuario CASCADE;

DROP SCHEMA IF EXISTS ${schema} CASCADE;

