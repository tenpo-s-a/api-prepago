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

-- // create_sp_mc_prp_actualiza_no_conciliados_switch_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_actualiza_no_conciliados_switch_v10(
  IN _in_fecha_inicial   VARCHAR,
  IN _in_fecha_final     VARCHAR,
  IN _in_tipo_movimiento VARCHAR,
  IN _in_indnorcor       NUMERIC,
  IN _in_nuevo_estado    VARCHAR,
  OUT _error_code        VARCHAR,
  OUT _error_msg         VARCHAR
) AS $$
 DECLARE
    _fecha_inicial_timestamp TIMESTAMP;
    _fecha_final_timestamp TIMESTAMP;
 BEGIN
    _error_code := '0';
    _error_msg := '';

    IF TRIM(COALESCE(_in_fecha_inicial, '')) = '' THEN
      _error_code := 'MC001';
      _error_msg := '[mc_prp_actualiza_no_conciliados_switch_v10] La fecha inicial es obligatoria';
      RETURN;
    END IF;

    IF TRIM(COALESCE(_in_fecha_final, '')) = '' THEN
      _error_code := 'MC002';
      _error_msg := '[mc_prp_actualiza_no_conciliados_switch_v10] La fecha final es obligatoria';
      RETURN;
    END IF;

    IF TRIM(COALESCE(_in_tipo_movimiento, '')) = '' THEN
      _error_code := 'MC003';
      _error_msg := '[mc_prp_actualiza_no_conciliados_switch_v10] El tipo movimiento es obligatorio';
      RETURN;
    END IF;

    IF (_in_indnorcor IS NULL) OR (_in_indnorcor != 0 AND _in_indnorcor != 1) THEN
      _error_code := 'MC004';
      _error_msg := '[mc_prp_actualiza_no_conciliados_switch_v10] El indnorcor debe ser 0 o 1';
      RETURN;
    END IF;

    IF TRIM(COALESCE(_in_nuevo_estado, '')) = '' THEN
      _error_code := 'MC005';
      _error_msg := '[mc_prp_actualiza_no_conciliados_switch_v10] El nuevo estado es obligatorio';
      RETURN;
    END IF;

    _fecha_inicial_timestamp := TO_TIMESTAMP(_in_fecha_inicial, 'YYYYMMDDHH24MISSMS')::timestamp without time zone;
    _fecha_final_timestamp := TO_TIMESTAMP(_in_fecha_final, 'YYYYMMDDHH24MISSMS')::timestamp without time zone;

    UPDATE
        ${schema}.prp_movimiento
      SET
        estado_con_switch = _in_nuevo_estado
      WHERE
        estado_con_switch = 'PENDING' AND
        tipo_movimiento = _in_tipo_movimiento AND
        indnorcor = _in_indnorcor AND
        fecha_creacion >= _fecha_inicial_timestamp AND
        fecha_creacion <= _fecha_final_timestamp;

     EXCEPTION
       WHEN OTHERS THEN
           _error_code := SQLSTATE;
           _error_msg := '[mc_prp_actualiza_no_conciliados_switch_v10] Error al actualizar movimientos pendientes. CAUSA ('|| SQLERRM ||')';
       RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_actualiza_no_conciliados_switch_v10(VARCHAR, VARCHAR, VARCHAR, NUMERIC, VARCHAR);
