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
  IN _in_id              BIGINT,
  IN _in_id_usuario_mc   BIGINT,
  IN _in_rut             INTEGER,
  IN _in_estado          VARCHAR,
  OUT _id BIGINT,
  OUT _id_usuario_mc BIGINT,
  OUT _rut INTEGER,
  OUT _estado VARCHAR,
  OUT _saldo TEXT,
  OUT _saldo_expiracion BIGINT,
  OUT _fecha_creacion TIMESTAMP,
  OUT _fecha_actualizacion TIMESTAMP
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
  SELECT
    id,
    id_usuario_mc,
    rut,
    estado,
    saldo,
    saldo_expiracion,
    fecha_creacion,
    fecha_actualizacion
  FROM
    ${schema}.prp_usuario
  WHERE
    (COALESCE(_in_id, 0) = 0 OR id = _in_id) AND
    (COALESCE(_in_id_usuario_mc, 0) = 0 OR id_usuario_mc = _in_id_usuario_mc) AND
    (COALESCE(_in_rut, 0) = 0 OR rut = _in_rut) AND
    (TRIM(COALESCE(_in_estado,'')) = '' OR estado = _in_estado);
   RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_usuarios_v10(BIGINT, BIGINT, INTEGER, VARCHAR);


