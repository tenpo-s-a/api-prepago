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

-- // create_sp_mc_cdt_busca_movimiento_by_idext
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.cdt}.mc_cdt_busca_movimiento_by_idext_v10
(
    IN  _id_ext               VARCHAR,
    OUT _id                   BIGINT,
    OUT _id_cuenta            BIGINT,
    OUT _id_fase_movimiento   BIGINT,
    OUT _id_mov_referencia    BIGINT,
    OUT _id_tx_externo        VARCHAR,
    OUT _glosa                VARCHAR,
    OUT _monto                NUMERIC,
    OUT _movimiento           VARCHAR
)RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
    SELECT
      mcta.id,
      mcta.id_cuenta,
      mcta.id_fase_movimiento,
      mcta.id_mov_referencia,
      mcta.id_tx_externo,
      mcta.glosa,
      mcta.monto,
      fsm.nombre
    FROM
     ${schema.cdt}.cdt_movimiento_cuenta mcta
    INNER JOIN ${schema.cdt}.cdt_fase_movimiento fsm ON fsm.id = mcta.id_fase_movimiento
    WHERE
      id_tx_externo = _id_ext;
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.cdt}.mc_cdt_busca_movimiento_by_idext_v10(VARCHAR);
