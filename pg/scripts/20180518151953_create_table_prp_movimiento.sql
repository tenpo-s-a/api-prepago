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
  id_usuario          BIGINT REFERENCES ${schema}.prp_usuario(id),
  id_tx_externo       VARCHAR(50) NOT NULL,
  tipo_movimiento     VARCHAR(50) NOT NULL,
  monto               NUMERIC NOT NULL,
  estado              VARCHAR(20) NOT NULL,
  fecha_creacion      TIMESTAMP NOT NULL,
  fecha_actualizacion TIMESTAMP NOT NULL,
  codent              VARCHAR(4) NOT NULL,
  centalta            VARCHAR(4) NOT NULL,
  cuenta              VARCHAR(12) NOT NULL,
  clamon              NUMERIC(3) NOT NULL,
  indnorcor           NUMERIC(1) NOT NULL,
  tipofac             NUMERIC(4) NOT NULL,
  fecfac              DATE NOT NULL,
  numreffac           VARCHAR(23) NOT NULL,
  pan                 VARCHAR(22) NOT NULL,
  clamondiv          NUMERIC(3) NOT NULL,
  impdiv             NUMERIC(17) NOT NULL,
  impfac             NUMERIC(17) NOT NULL,
  cmbapli            NUMERIC(9) NOT NULL,
  numaut            VARCHAR(6) NOT NULL,
  indproaje          VARCHAR(1) NOT NULL,
  codcom            VARCHAR(15) NOT NULL,
  codact            NUMERIC(4) NOT NULL,
  impliq             NUMERIC(17) NOT NULL,
  clamonliq          NUMERIC(3) NOT NULL,
  codpais            NUMERIC(3) NOT NULL,
  nompob            VARCHAR(26) NOT NULL,
  numextcta        NUMERIC(3) NOT NULL,
  nummovext         NUMERIC(7) NOT NULL,
  clamone         NUMERIC(3) NOT NULL,
  tipolin          VARCHAR(4) NOT NULL,
  linref          NUMERIC(8) NOT NULL,
  numbencta       NUMERIC(5) NOT NULL,
  numplastico     NUMERIC(12) NOT NULL,
  CONSTRAINT prp_movimiento_pk PRIMARY KEY(id)
);

CREATE INDEX prp_movimiento_i1 ON ${schema}.prp_movimiento (id);
CREATE INDEX prp_movimiento_i2 ON ${schema}.prp_movimiento (id_usuario);
CREATE INDEX prp_movimiento_i3 ON ${schema}.prp_movimiento (id_usuario,tipo_movimiento);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema}.prp_movimiento CASCADE;
