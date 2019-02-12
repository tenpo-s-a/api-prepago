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

-- // create_sp_busca_movimientos_conciliar
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_busca_movimientos_conciliados_v10
(
    IN  _in_id_mov_ref        BIGINT,
    OUT _id                   BIGINT,
    OUT _id_mov_ref           BIGINT,
    OUT _fecha_registro       TIMESTAMP,
    OUT _accion               VARCHAR,
    OUT _estado               VARCHAR
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
    SELECT
      id,
      id_mov_ref,
      fecha_registro,
      accion,
      estado
    FROM
      ${schema}.prp_movimiento_conciliado
    WHERE
      COALESCE(_in_id_mov_ref, 0) = 0 OR id_mov_ref = _in_id_mov_ref;
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_busca_movimientos_conciliados_v10(BIGINT);


