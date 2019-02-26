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
  OUT _error_code         VARCHAR,
  OUT _error_msg          VARCHAR
) AS $$
BEGIN

  _error_code := '0';
  _error_msg := '';

  EXECUTE
    format(
      'UPDATE '
      '   ${schema}.prp_movimiento mov '
      'SET '
      '   %s = ''NOT_RECONCILED'' '
      'WHERE '
      '   mov.%s = ''PENDING'' AND '
      '   mov.tipo_movimiento != ''SUSCRIPTION'' AND '
      '   mov.tipo_movimiento != ''PURCHASE'' AND '
      '   (SELECT COUNT(f.id) '
      '     FROM ${schema}.prp_archivos_conciliacion f '
      '     WHERE f.created_at >= mov.fecha_creacion AND f.tipo = ''%s'') >= 2', /* Contar solo los de un tipo */
      _in_nombre_columna, _in_nombre_columna, _in_tipo_archivo
    );

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

DROP FUNCTION IF EXISTS ${schema}.mc_expire_old_reconciliation_movements_v10(VARCHAR, VARCHAR);
