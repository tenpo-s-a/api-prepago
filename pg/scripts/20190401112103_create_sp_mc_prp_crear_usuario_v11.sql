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

-- // create_sp_mc_prp_crear_usuario_v11
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_crear_usuario_v11
(
IN _rut integer,
IN _estado character varying(20),
IN _nombre character varying(30),
IN _apellido character varying(30),
IN _numero_documento character varying(30),
IN _nivel character varying(20),
IN _uuid character varying(100),
OUT _r_id           BIGINT,
OUT _error_code     VARCHAR,
OUT _error_msg      VARCHAR
) AS $$
DECLARE
BEGIN
_error_code := '0';
_error_msg := '';

IF COALESCE(_rut, 0) = 0 THEN
_error_code := 'MC001';
_error_msg := 'El _rut es obligatorio';
RETURN;
END IF;

IF TRIM(COALESCE(_estado, '')) = '' THEN
_error_code := 'MC002';
_error_msg := 'El _estado es obligatorio';
RETURN;
END IF;

IF TRIM(COALESCE(_uuid, '')) = '' THEN
_error_code := 'MC003';
_error_msg := 'El _uiid es obligatorio';
RETURN;
END IF;

INSERT INTO ${schema}.prp_usuario
(
  rut,
  estado,
  fecha_creacion,
  fecha_actualizacion,
  nombre,
  apellido,
  numero_documento,
  nivel,
  uuid,
  id_usuario_mc,
  saldo_info,
  saldo_expiracion,
  intentos_validacion,
  tipo_documento
)
VALUES
(
  _rut,
  _estado,
  timezone('utc', now()),
  timezone('utc', now()),
  _nombre,
  _apellido,
  _numero_documento,
  _nivel,
  _uuid,
  random() * 1000000 + 1,
  '',
  0,
  0,
  ''
)
RETURNING id INTO _r_id;

EXCEPTION
WHEN OTHERS THEN
_error_code := SQLSTATE;
_error_msg := '[mc_prp_crear_usuario_v11] Error al insertar usuario. CAUSA ('|| SQLERRM ||')';
RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_crear_usuario_v11(INTEGER, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR );

