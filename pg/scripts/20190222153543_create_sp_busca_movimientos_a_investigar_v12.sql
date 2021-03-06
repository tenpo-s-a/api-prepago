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

-- // create_sp_busca_movimientos_a_investigar_v12
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_busca_movimientos_a_investigar_v12
(
    IN  _in_id_archivo_origen VARCHAR,
    IN  _in_fecha_desde       TIMESTAMP,
    IN  _in_fecha_hasta       TIMESTAMP,
    OUT _id                   BIGINT,
    OUT _id_archivo_origen    VARCHAR,
    OUT _origen               VARCHAR,
    OUT _nombre_archivo       VARCHAR,
    OUT _fecha_registro       TIMESTAMP,
    OUT _fecha_de_transaccion TIMESTAMP,
    OUT _responsable          VARCHAR,
    OUT _descripcion          VARCHAR,
    OUT _mov_ref              BIGINT
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
    SELECT id,
      id_archivo_origen,
      origen,
      nombre_archivo,
      fecha_registro,
      fecha_de_transaccion,
      responsable,
      descripcion,
      mov_ref
    FROM
      ${schema}.prp_movimiento_investigar
    WHERE
      (TRIM(COALESCE(_in_id_archivo_origen,'')) = '' OR id_archivo_origen = _in_id_archivo_origen) AND
      (_in_fecha_desde IS NULL OR _in_fecha_desde <= fecha_registro) AND
      (_in_fecha_hasta IS NULL OR _in_fecha_hasta >= fecha_registro)
    ORDER BY id DESC;
   RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_busca_movimientos_a_investigar_v12(VARCHAR, TIMESTAMP, TIMESTAMP);


