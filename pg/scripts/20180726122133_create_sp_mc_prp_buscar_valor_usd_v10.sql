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

-- // create_sp_prp_buscar_valor_usd_v10
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.mc_prp_buscar_valor_usd_v10
(
  OUT _id                   BIGINT,
  OUT _nombre_archivo       VARCHAR,
  OUT _fecha_creacion       TIMESTAMP,
  OUT _fecha_termino        TIMESTAMP,
  OUT _fecha_expiracion_usd TIMESTAMP,
  OUT _precio_venta         NUMERIC(8,7),
  OUT _precio_compra        NUMERIC(8,7),
  OUT _precio_medio         NUMERIC(8,7),
  OUT _exponente            NUMERIC
)
RETURNS SETOF RECORD AS $$
DECLARE
BEGIN
  RETURN QUERY
  SELECT
    id,
    nombre_archivo,
    fecha_creacion,
    fecha_termino,
    fecha_expiracion_usd,
    precio_venta,
    precio_compra,
    precio_medio,
    exponente
  FROM
    ${schema}.prp_valor_usd
  WHERE
    now()::date between fecha_creacion::date and fecha_termino::date;
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_valor_usd_v10();


