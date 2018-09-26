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

-- // create_sp_mc_prp_buscar_movimiento_estado_switch_no_conciliado_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_actualiza_no_conciliados_switch_v10(
  IN _in_fecha_inicial  VARCHAR,
  IN _in_fecha_final    VARCHAR,
  OUT _in_id                   BIGINT,
  OUT _in_id_movimiento_ref    BIGINT,
  OUT _in_id_usuario           BIGINT,
  OUT _in_id_tx_externo        VARCHAR,
  OUT _in_estado_con_switch    VARCHAR,
  OUT _fecha_creacion          TIMESTAMP
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
  SELECT
    id,
    id_movimiento_ref,
    id_usuario,
    id_tx_externo,
    estado_con_switch,
    fecha_creacion
  FROM
    ${schema}.prp_movimiento
  WHERE
    fecha_creacion BETWEEN
    TO_TIMESTAMP(_in_fecha_inicial, 'YYYY-MM-DD HH24:MI:SS') AND
    TO_TIMESTAMP(_in_fecha_final, 'YYYY-MM-DD HH24:MI:SS')
  ORDER BY fecha_creacion ASC;
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_movimientos_v10(VARCHAR, VARCHAR);
