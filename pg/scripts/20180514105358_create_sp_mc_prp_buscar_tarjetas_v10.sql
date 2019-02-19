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

-- // create_sp_mc_prp_buscar_tarjetas_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_buscar_tarjetas_v10
(
  IN _in_id              BIGINT,
  IN _in_id_usuario      BIGINT,
  IN _in_expiracion      INTEGER,
  IN _in_estado          VARCHAR,
  IN _in_contrato        VARCHAR,
  IN _in_pan_encriptado  VARCHAR,
  OUT _id                BIGINT,
  OUT _id_usuario        BIGINT,
  OUT _pan               VARCHAR,
  OUT _pan_encriptado    VARCHAR,
  OUT _contrato          VARCHAR,
  OUT _expiracion        INTEGER,
  OUT _estado            VARCHAR,
  OUT _nombre_tarjeta    VARCHAR,
  OUT _producto          VARCHAR,
  OUT _numero_unico      VARCHAR,
  OUT _fecha_creacion    TIMESTAMP,
  OUT _fecha_actualizacion TIMESTAMP
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
  SELECT
    id,
    id_usuario,
    pan,
    pan_encriptado,
    contrato,
    expiracion,
    estado,
    nombre_tarjeta,
    producto,
    numero_unico,
    fecha_creacion,
    fecha_actualizacion
  FROM
    ${schema}.prp_tarjeta
  WHERE
    (COALESCE(_in_id, 0) = 0 OR id = _in_id) AND
    (COALESCE(_in_id_usuario, 0) = 0 OR id_usuario = _in_id_usuario) AND
    (COALESCE(_in_expiracion, 0) = 0 OR expiracion = _in_expiracion) AND
    (TRIM(COALESCE(_in_estado,'')) = '' OR estado = _in_estado) AND
    (TRIM(COALESCE(_in_contrato,'')) = '' OR contrato = _in_contrato) AND
    (TRIM(COALESCE(_in_pan_encriptado,'')) = '' OR pan_encriptado = _in_pan_encriptado)
  ORDER BY id DESC;
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_tarjetas_v10(BIGINT, BIGINT, INTEGER, VARCHAR, VARCHAR,VARCHAR);

