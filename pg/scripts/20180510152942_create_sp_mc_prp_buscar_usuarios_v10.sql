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

-- // create_sp_mc_prp_buscar_usuarios
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_buscar_usuarios_v10
(
 IN _id              BIGINT,
 IN _id_usuario_mc   BIGINT,
 IN _rut             INTEGER,
 IN _estado          VARCHAR,
 OUT _result         REFCURSOR,
 OUT _error_code     VARCHAR,
 OUT _error_msg      VARCHAR
) AS $$
 DECLARE
 BEGIN
    _error_code := '0';
    _error_msg := '';

    OPEN _result FOR
      SELECT
        id,
        id_usuario_mc,
        rut,
        estado,
        fecha_creacion,
        fecha_actualizacion
      FROM
        ${schema}.prp_usuario
      WHERE
        (COALESCE(_id, 0) = 0 OR id = _id) AND
        (COALESCE(_id_usuario_mc, 0) = 0 OR id_usuario_mc = _id_usuario_mc) AND
        (COALESCE(_rut, 0) = 0 OR rut = _rut) AND
        (TRIM(COALESCE(_estado,'')) = '' OR estado = _estado);

   EXCEPTION
     WHEN OTHERS THEN
         _error_code := SQLSTATE;
         _error_msg := '[mc_prp_buscar_usuarios_v10] Error al buscar usuarios. CAUSA ('|| SQLERRM ||')';
     RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_usuarios_v10(BIGINT, BIGINT, INTEGER, VARCHAR, REFCURSOR, VARCHAR, VARCHAR)


