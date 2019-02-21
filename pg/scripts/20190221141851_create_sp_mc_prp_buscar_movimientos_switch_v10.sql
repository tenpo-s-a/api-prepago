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

-- // create_sp_mc_prp_buscar_movimientos_switch_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_buscar_movimientos_switch_v10(
  IN _in_id                   BIGINT,
  IN _in_id_archivo           BIGINT,
  IN _in_id_multicaja         VARCHAR,
  OUT _id                     BIGINT,
  OUT _id_archivo             BIGINT,
  OUT _id_multicaja           VARCHAR,
  OUT _id_cliente             BIGINT,
  OUT _id_multicaja_ref       BIGINT,
  OUT _monto                  NUMERIC,
  OUT _fecha_trx              TIMESTAMP
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
  SELECT
    id,
    id_archivo,
    id_multicaja,
    id_cliente,
    id_multicaja_ref,
    monto,
    fecha_trx
  FROM
    ${schema}.prp_movimiento_switch
  WHERE
    (COALESCE(_in_id, 0) = 0 OR id = _in_id) AND
    (COALESCE(_in_id_archivo, 0) = 0 OR id_archivo = _in_id_archivo) AND
    (TRIM(COALESCE(_in_id_multicaja,'')) = '' OR id_multicaja = _in_id_multicaja)
   ORDER BY id DESC;
   RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_movimientos_switch_v10(BIGINT, BIGINT, VARCHAR);
