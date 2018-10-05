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

-- // create_sp_cdt_busca_movimiento_referencia
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.cdt}.mc_cdt_busca_movimiento_referencia_v10
(
    IN  _in_idCDT           BIGINT,
    OUT _id                 BIGINT,
    OUT _id_fase_movimiento BIGINT,
    OUT _nombre_fase        VARCHAR
)RETURNS SETOF RECORD AS $$

BEGIN
  RETURN QUERY
  SELECT
    MCT.id,
    MCT.id_fase_movimiento,
    FMO.nombre
  FROM
    ${schema.cdt}.cdt_movimiento_cuenta MCT
  INNER JOIN ${schema.cdt}.cdt_fase_movimiento FMO ON MCT.id_fase_movimiento = FMO.id
  WHERE
    MCT.id = _in_idCDT AND
    MCT.id_mov_referencia = 0;
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema.cdt}.mc_cdt_busca_movimiento_referencia_v10(BIGINT);

