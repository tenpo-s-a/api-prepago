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

-- // create_sp_mc_buscar_parametro_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.parameters}.mc_buscar_parametro_v10
(
 IN _in_aplicacion      VARCHAR,
 IN _in_nombre          VARCHAR,
 IN _in_version         VARCHAR,
 OUT _id                BIGINT,
 OUT _aplicacion        VARCHAR,
 OUT _nombre            VARCHAR,
 OUT _version           VARCHAR,
 OUT _valor             TEXT,
 OUT _expiracion        BIGINT,
 OUT _fecha_creacion    TIMESTAMP
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
  SELECT
    id,
    aplicacion,
    nombre,
    version,
    valor::text,
    expiracion,
    fecha_creacion
  FROM
    ${schema.parameters}.mc_parametro
  WHERE
    (TRIM(COALESCE(_in_aplicacion,'')) = '' OR aplicacion = _in_aplicacion) AND
    (TRIM(COALESCE(_in_nombre,'')) = '' OR nombre = _in_nombre) AND
    (TRIM(COALESCE(_in_version,'')) = '' OR version = _in_version);
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.parameters}.mc_buscar_parametro_v10(VARCHAR, VARCHAR, VARCHAR);
