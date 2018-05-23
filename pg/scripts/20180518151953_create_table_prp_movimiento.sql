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
  tipo_movimiento     VARCHAR(10) NOT NULL,
  monto               NUMERIC NOT NULL,
  moneda              VARCHAR(3) NOT NULL,
  estado              VARCHAR(10) NOT NULL,
  fecha_movimiento    TIMESTAMP NOT NULL,
  fecha_creacion      TIMESTAMP NOT NULL,
  cod_entidad         VARCHAR(4) NOT NULL,
  cen_alta            VARCHAR(4) NOT NULL,
  cuenta              VARCHAR(12) NOT NULL,
  cod_moneda          NUMERIC(3) NOT NULL,
  ind_norcor          NUMERIC(1) NOT NULL,
  tipo_factura        NUMERIC(4) NOT NULL,
  fecha_factura       TIMESTAMP NOT NULL,
  num_factura_ref     VARCHAR(23) NOT NULL,
  pan                 VARCHAR(22) NOT NULL,
  cod_mondiv          NUMERIC(3) NOT NULL,
  imp_div             NUMERIC(17) NOT NULL,
  imp_fac             NUMERIC(17) NOT NULL,
  cmp_apli            NUMERIC(9) NOT NULL,
  num_autorizacion    VARCHAR(6) NOT NULL,
  ind_proaje          VARCHAR(1) NOT NULL,
  cod_comercio        VARCHAR(15) NOT NULL,
  cod_actividad       VARCHAR(4) NOT NULL,
  imp_liq             NUMERIC(17) NOT NULL,
  cod_monliq          NUMERIC(3) NOT NULL,
  cod_pais            NUMERIC(3) NOT NULL,
  nom_poblacion       VARCHAR(26) NOT NULL,
  num_extracto        NUMERIC(3) NOT NULL,
  num_mov_extracto    NUMERIC(7) NOT NULL,
  clave_moneda        NUMERIC(3) NOT NULL,
  tipo_linea          VARCHAR(4) NOT NULL,
  referencia_linea    NUMERIC(8) NOT NULL,
  num_benef_cta       NUMERIC(5) NOT NULL,
  numero_plastico     NUMERIC(12) NOT NULL,
  CONSTRAINT prp_movimiento_pk PRIMARY KEY(id)
);

CREATE INDEX prp_movimiento_i1 ON ${schema}.prp_movimiento (id);
CREATE INDEX prp_movimiento_i2 ON ${schema}.prp_movimiento (id_usuario);
CREATE INDEX prp_movimiento_i3 ON ${schema}.prp_movimiento (id_usuario,tipo_movimiento);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema}.prp_movimiento CASCADE;
