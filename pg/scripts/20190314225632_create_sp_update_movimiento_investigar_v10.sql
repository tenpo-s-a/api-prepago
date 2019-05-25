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

-- // create_sp_update_movimiento_investigar_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_actualiza_movimiento_investigar_v10 (
IN  _in_id                BIGINT,
IN  _in_sent_status       VARCHAR,
OUT _error_code           VARCHAR,
OUT _error_msg            VARCHAR
) AS $$

BEGIN
_error_code := '0';
_error_msg := '';

IF COALESCE(_in_id, 0) = 0 AND TRIM(COALESCE(_in_sent_status, '')) = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_actualiza_movimiento_investigar_v10] El id y el status son obligatorios';
RETURN;
END IF;

IF COALESCE(_in_id, 0) = 0 THEN
_error_code := '101000';
_error_msg := '[mc_prp_actualiza_movimiento_investigar_v10] El id de registro es obligatoria';
RETURN;
END IF;

IF TRIM(COALESCE(_in_sent_status, '')) = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_actualiza_movimiento_investigar_v10] El sent_status es obligatorio';
RETURN;
END IF;

UPDATE
  ${schema}.prp_movimiento_investigar
SET
  sent_status = _in_sent_status,
  fecha_registro = timezone('utc', now())
WHERE
  id = _in_id;
IF NOT FOUND THEN
_error_code := '101000';
_error_msg := '[mc_prp_actualiza_movimiento_investigar_v10] El id no se encuentra, el registro no se pudo actualizar';
RETURN;
END IF;

EXCEPTION
WHEN OTHERS THEN
_error_code := SQLSTATE;
_error_msg := '[mc_prp_actualiza_movimiento_investigar_v10] Error al actualizar sent_status en tabla prp_movimiento_investigar. CAUSA ('|| SQLERRM ||')';
RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.


DROP FUNCTION IF EXISTS ${schema}.mc_prp_actualiza_movimiento_investigar_v10(BIGINT, NUMERIC, VARCHAR);
