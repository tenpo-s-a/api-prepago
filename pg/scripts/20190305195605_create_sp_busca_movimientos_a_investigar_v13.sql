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

-- // create_sp_busca_movimientos_a_investigar_v13
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_busca_movimientos_a_investigar_v13
(
IN  _in_id                BIGINT,
IN  _in_fecha_desde       TIMESTAMP,
IN  _in_fecha_hasta       TIMESTAMP,
IN  _in_sent_status       VARCHAR,
IN  _in_mov_ref           NUMERIC,
OUT _id                   BIGINT,
OUT _informacion_archivos TEXT,
OUT _origen               VARCHAR,
OUT _fecha_registro       TIMESTAMP,
OUT _fecha_de_transaccion TIMESTAMP,
OUT _responsable          VARCHAR,
OUT _descripcion          VARCHAR,
OUT _mov_ref              NUMERIC,
OUT _tipo_movimiento      VARCHAR,
OUT _sent_status          VARCHAR
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
    SELECT id,
           informacion_archivos::text,
           origen,
           fecha_registro,
           fecha_de_transaccion,
           responsable,
           descripcion,
           mov_ref,
           tipo_movimiento,
           sent_status
    FROM
      ${schema}.prp_movimiento_investigar
    WHERE
      (_in_id IS NULL OR id = _in_id) AND
      (_in_fecha_desde IS NULL OR _in_fecha_desde <= fecha_registro) AND
      (_in_fecha_hasta IS NULL OR _in_fecha_hasta >= fecha_registro) AND
      (TRIM(COALESCE(_in_sent_status,'')) = '' OR sent_status = _in_sent_status) AND
      (_in_mov_ref IS NULL OR mov_ref = _in_mov_ref)
    ORDER BY id DESC;
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_busca_movimientos_a_investigar_v13(BIGINT, TIMESTAMP, TIMESTAMP, VARCHAR, NUMERIC);
