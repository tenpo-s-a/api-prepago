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

-- // create_sp_insert_intermediate_reconciliation_files
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.prp_inserta_archivo_reconciliacion
(
  IN _nombre_de_archivo VARCHAR,
  IN _proceso           VARCHAR,
  IN _tipo              VARCHAR,
  IN _status            VARCHAR,
  OUT _r_id             BIGINT,
  OUT _error_code       VARCHAR,
  OUT _error_msg        VARCHAR
) AS $$
 DECLARE

 BEGIN

  _error_code := '0';
  _error_msg := '';

IF COALESCE(_nombre_de_archivo, '') = '' THEN
  _error_code := 'CF001';
  _error_msg := 'El _nombre_de_archivo es obligatorio';
RETURN;
END IF;

IF COALESCE(_proceso, '') = '' THEN
  _error_code := 'CF002';
  _error_msg := 'El _proceso es obligatorio';
RETURN;
END IF;

IF COALESCE(_tipo, '') = '' THEN
  _error_code := 'CF003';
  _error_msg := 'El _tipo es obligatorio';
RETURN;
END IF;

IF COALESCE(_status, '') = '' THEN
  _error_code := 'CF004';
  _error_msg := 'El _status es obligatorio';
RETURN;
END IF;

INSERT INTO ${schema}.prp_archivos_reconciliacion (
nombre_de_archivo,
proceso,
tipo,
status,
created_at,
updated_at
)
VALUES (
_nombre_de_archivo,
_proceso,
_tipo,
_status,
timezone('utc', now()),
timezone('utc', now())
) RETURNING id INTO _r_id;

EXCEPTION
WHEN OTHERS THEN
_error_code := SQLSTATE;
_error_msg := '[prp_inserta_archivo_reconciliacion] Error al guardar archivo reconciliacion. CAUSA ('|| SQLERRM ||')';
RETURN;

END;
$$ LANGUAGE plpgsql;


-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.prp_inserta_archivo_reconciliacion(VARCHAR, VARCHAR, VARCHAR, VARCHAR);
