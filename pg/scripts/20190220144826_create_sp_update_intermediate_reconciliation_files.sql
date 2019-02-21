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

-- // crea_sp_prp_actualiza_archivo_reconciliacion
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.prp_actualiza_archivo_conciliacion (
  IN  _in_id                 BIGINT,
  IN  _in_status             VARCHAR,
  OUT _error_code           VARCHAR,
  OUT _error_msg            VARCHAR
) AS $$

BEGIN
_error_code := '0';
_error_msg := '';

IF COALESCE(_in_id, 0) = 0 THEN
_error_code := 'MC000';
_error_msg := '[prp_actualiza_archivo_conciliacion] El id de registro es obligatoria';
RETURN;
END IF;

IF TRIM(COALESCE(_in_status, '')) = '' THEN
_error_code := 'MC004';
_error_msg := '[prp_actualiza_archivo_conciliacion] El status es obligatorio';
RETURN;
END IF;

UPDATE
${schema}.prp_archivos_conciliacion
SET
status = _in_status,
updated_at = timezone('utc', now())
WHERE
id = _in_id;

EXCEPTION
WHEN OTHERS THEN
_error_code := SQLSTATE;
_error_msg := '[prp_actualiza_archivo_conciliacion] Error al actualizar archivo conciliacion. CAUSA ('|| SQLERRM ||')';
RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.prp_actualiza_archivo_conciliacion(BIGINT, VARCHAR);
