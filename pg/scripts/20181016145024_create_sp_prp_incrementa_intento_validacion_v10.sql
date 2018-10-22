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

-- // create_sp_prp_aumenta_intento_validacion_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_incrementa_intento_validacion_v10
(
  IN _in_id               BIGINT,
  OUT _intentos_validacion BIGINT,
  OUT _error_code         VARCHAR,
  OUT _error_msg          VARCHAR
) AS $$
DECLARE
  _id_usuario BIGINT;
BEGIN
  _error_code := '0';
  _error_msg := '';

  IF COALESCE(_in_id, 0) = 0 THEN
    _error_code := 'MC001';
    _error_msg := '[mc_prp_incrementa_intento_validacion_v10] El id de usuario es obligatorio';
    RETURN;
  END IF;

  -- BUSCA EL USUARIO
  SELECT
     id
  INTO
     _id_usuario
  FROM
     ${schema}.prp_usuario
  WHERE
     id = _in_id;

  IF COALESCE(_id_usuario, 0) = 0 THEN
    _error_code := 'MC002';
    _error_msg := '[mc_prp_incrementa_intento_validacion_v10] El usuario  no existe';
    RETURN;
  END IF;

  UPDATE
    ${schema}.prp_usuario
  SET
    intentos_validacion = intentos_validacion + 1
  WHERE
    id = _id_usuario
  RETURNING intentos_validacion INTO _intentos_validacion;

  EXCEPTION
  WHEN OTHERS THEN
     _error_code := SQLSTATE;
     _error_msg := '[mc_prp_incrementa_intento_validacion_v10] Error al incrementar intento de validacion. CAUSA ('|| SQLERRM ||')';
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_incrementa_intento_validacion_v10(BIGINT);

