--
--    Copyright 2010-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apa_id_faseche.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- // create_sp_mc_cdt_carga_movimientos
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.cdt}.mc_cdt_carga_fases_movimientos_v10
(
    IN  _in_nombre      VARCHAR,
    IN  _in_id_fase     NUMERIC,
    OUT _id             BIGINT,
    OUT _nombre         VARCHAR,
    OUT _descripcion    VARCHAR
)RETURNS SETOF RECORD AS $$

BEGIN
    RETURN QUERY
        SELECT
            id,
            nombre,
            descripcion
        FROM
             ${schema.cdt}.cdt_fase_movimiento
        WHERE
            estado = 'ACTIVO' AND
            (TRIM(COALESCE(_in_nombre,'')) = '' OR LOWER(nombre) LIKE '%'||LOWER(_in_nombre)||'%') AND
            (COALESCE(_in_id_fase,0) = 0 OR id = _in_id_fase );
     RETURN;
END;
$$
LANGUAGE 'plpgsql';

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema.cdt}.mc_cdt_carga_fases_movimientos_v10(VARCHAR, NUMERIC);
