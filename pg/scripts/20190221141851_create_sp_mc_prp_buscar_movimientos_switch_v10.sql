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

-- // create_sp_mc_prp_buscar_movimientos_switch_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_buscar_movimientos_switch_v10(
  IN _in_nombre_tabla         VARCHAR,
  IN _in_id                   BIGINT,
  IN _in_id_archivo           BIGINT,
  IN _in_id_multicaja         VARCHAR,
  OUT _id                     BIGINT,
  OUT _id_archivo             BIGINT,
  OUT _id_multicaja           VARCHAR,
  OUT _id_cliente             BIGINT,
  OUT _id_multicaja_ref       BIGINT,
  OUT _monto                  NUMERIC,
  OUT _fecha_trx              TIMESTAMP
)
RETURNS SETOF RECORD AS $$
BEGIN

  IF COALESCE(_in_nombre_tabla, '') = '' THEN
    _in_nombre_tabla = 'prp_movimiento_switch';
  END IF;

  RETURN QUERY
  EXECUTE
    format(
      'SELECT id, id_archivo, id_multicaja, id_cliente, id_multicaja_ref, monto, fecha_trx '
      'FROM ${schema}.%s '
      'WHERE '
      '  (COALESCE( $1 , 0) = 0 OR id = $1 ) AND '
      '  (COALESCE( $2 , 0) = 0 OR id_archivo = $2 ) AND '
      '  (TRIM(COALESCE( $3 ,'''')) = '''' OR id_multicaja = $3 ) '
      'ORDER BY id DESC',
      _in_nombre_tabla
    )
  USING
    _in_id, _in_id_archivo, _in_id_multicaja;
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_movimientos_switch_v10(VARCHAR, BIGINT, BIGINT, VARCHAR);
