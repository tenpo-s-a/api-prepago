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

-- // mc_expire_old_reconciliation_movements_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_expire_old_reconciliation_movements_v10(
  IN _in_nombre_columna   VARCHAR,
  IN _in_tipo_archivo     VARCHAR,
  IN _in_tipo_movimiento  VARCHAR,
  IN _in_indnorcor        NUMERIC,
  OUT _error_code         VARCHAR,
  OUT _error_msg          VARCHAR
) AS $$
BEGIN

  _error_code := '0';
  _error_msg := '';

  IF TRIM(COALESCE(_in_nombre_columna, '')) = '' THEN
    _error_code := 'MC001';
    _error_msg := '[mc_expire_old_reconciliation_movements_v10] El _in_nombre_columna es obligatorio';
    RETURN;
  END IF;

  IF TRIM(COALESCE(_in_tipo_archivo, '')) = '' THEN
    _error_code := 'MC002';
    _error_msg := '[mc_expire_old_reconciliation_movements_v10] El _in_tipo_archivo es obligatorio';
    RETURN;
  END IF;

  EXECUTE
    format(
      'UPDATE '
      '   ${schema}.prp_movimiento mov '
      'SET '
      '   %s = ''NOT_RECONCILED'' '
      'WHERE '
      '   (COALESCE( $1 , '''') = '''' OR mov.tipo_movimiento = $1 ) AND '
      '   (COALESCE( $2 , -1) = -1 OR mov.indnorcor = $2 ) AND '
      '   mov.%s = ''PENDING'' AND '
      '   mov.tipo_movimiento != ''SUSCRIPTION'' AND '
      '   mov.tipo_movimiento != ''PURCHASE'' AND '
      '   (SELECT COUNT(f.id) '
      '     FROM ${schema}.prp_archivos_conciliacion f '
      '     WHERE f.created_at >= mov.fecha_creacion AND f.tipo = ''%s'' AND f.status = ''OK'') >= 2', /* Contar solo los de un tipo */
      _in_nombre_columna, _in_nombre_columna, _in_tipo_archivo
    )
  USING
    _in_tipo_movimiento, _in_indnorcor;

  EXCEPTION
    WHEN OTHERS THEN
      _error_code := SQLSTATE;
      _error_msg := '[mc_expire_old_reconciliation_movements_v10] Error al expirar conciliaciones. CAUSA ('|| SQLERRM ||')';
    RETURN;

RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_expire_old_reconciliation_movements_v10(VARCHAR, VARCHAR, VARCHAR, NUMERIC);
