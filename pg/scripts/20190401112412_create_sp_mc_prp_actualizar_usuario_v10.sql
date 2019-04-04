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

-- // create_sp_mc_prp_actualizar_usuario_v10
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.mc_prp_actualizar_estado_usuario_v11
(
IN _id             BIGINT,
IN _numero_documento VARCHAR,
IN _nombre         VARCHAR,
IN _apellido       VARCHAR,
IN _estado         VARCHAR,
IN _nivel          VARCHAR,
IN _uiid           VARCHAR,
OUT _error_code    VARCHAR,
OUT _error_msg     VARCHAR
) AS $$
DECLARE
BEGIN
_error_code := '0';
_error_msg := '';

IF COALESCE(_id, 0) = 0 THEN
_error_code := 'MC001';
_error_msg := 'El _id es obligatorio';
RETURN;
END IF;

UPDATE ${schema}.prp_usuario
SET
  numero_documento = ( CASE WHEN _numero_documento IS NOT NULL THEN _numero_documento ELSE numero_documento END ),
  nombre = ( CASE WHEN _nombre IS NOT NULL THEN _nombre ELSE nombre END ),
  apellido = ( CASE WHEN _apellido IS NOT NULL THEN _apellido ELSE apellido END ),
  estado = ( CASE WHEN _estado IS NOT NULL THEN _estado ELSE estado END ),
  nivel = ( CASE WHEN _nivel IS NOT NULL THEN _nivel ELSE nivel END ),
  uiid = ( CASE WHEN _uiid IS NOT NULL THEN _uiid ELSE uiid END ),
  fecha_actualizacion = timezone('utc', now())
WHERE
  id = _id;

EXCEPTION
WHEN OTHERS THEN
_error_code := SQLSTATE;
_error_msg := '[mc_prp_actualizar_estado_usuario_v11] Error al actualizar estado de usuario. CAUSA ('|| SQLERRM ||')';
RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_actualizar_estado_usuario_v11(BIGINT, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR);

