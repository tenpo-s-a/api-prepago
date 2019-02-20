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

-- // create_sp_prp_busca_archivo_reconciliacion
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.prp_busca_archivo_reconciliacion
(
    IN _in_nombre_de_archivo  VARCHAR,
    IN _in_proceso            VARCHAR,
    IN _in_tipo               VARCHAR,
    IN _in_status             VARCHAR,
    OUT _id                 BIGINT,
    OUT _nombre_de_archivo  VARCHAR,
    OUT _proceso            VARCHAR,
    OUT _tipo               VARCHAR,
    OUT _status             VARCHAR,
    OUT _created_at         TIMESTAMP,
    OUT _updated_at         TIMESTAMP
)RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
  SELECT
    id,
    nombre_de_archivo,
    proceso,
    tipo,
    status,
    created_at,
    updated_at
  FROM
   ${schema}.prp_archivos_reconciliacion
  WHERE
    (TRIM(COALESCE(_in_nombre_de_archivo,'')) = '' OR nombre_de_archivo = _in_nombre_de_archivo) AND
    (TRIM(COALESCE(_in_proceso,'')) = '' OR proceso = _in_proceso) AND
    (TRIM(COALESCE(_in_tipo,'')) = '' OR tipo = _in_tipo) AND
    (TRIM(COALESCE(_in_status,'')) = '' OR status = _in_status) 
    ORDER BY id DESC;
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.prp_busca_archivo_reconciliacion(VARCHAR,VARCHAR,VARCHAR,VARCHAR);
