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

-- // create_table_prp_valor_usd
-- Migration SQL that makes the change goes here.
CREATE TABLE ${schema}.prp_valor_usd (
  id                    BIGSERIAL NOT NULL,
  nombre_archivo        VARCHAR(50) NOT NULL,
  fecha_creacion        TIMESTAMP NOT NULL,
  fecha_termino         TIMESTAMP NOT NULL,
  fecha_expiracion_usd  TIMESTAMP NOT NULL,
  precio_venta          NUMERIC(15,7) NOT NULL,
  precio_compra         NUMERIC(15,7) NOT NULL,
  precio_medio          NUMERIC(15,7) NOT NULL,
  exponente             NUMERIC NOT NULL,

  CONSTRAINT prp_valor_usd_pk PRIMARY KEY(id),
  CONSTRAINT prp_valor_usd_u1 UNIQUE(fecha_creacion, precio_venta, precio_compra, precio_medio)

);

COMMENT ON CONSTRAINT prp_valor_usd_u1 ON ${schema}.prp_valor_usd IS 'No pueden duplicarse los valores de conversion para un mismo dia';
-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema}.prp_valor_usd CASCADE;

