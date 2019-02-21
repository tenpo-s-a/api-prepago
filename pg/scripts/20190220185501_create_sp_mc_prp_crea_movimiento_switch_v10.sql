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

-- // create_sp_mc_prp_crea_movimiento_switch_v10
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.prp_crea_movimiento_switch_v10 (
  IN _id_archivo        BIGINT,
  IN _id_multicaja      VARCHAR,
  IN _id_cliente        BIGINT,
  IN _id_multicaja_ref  BIGINT,
  IN _monto             NUMERIC,
  IN _fecha_trx         TIMESTAMP,
  OUT _r_id             BIGINT,
  OUT _error_code       VARCHAR,
  OUT _error_msg        VARCHAR
) AS $$
BEGIN
  _error_code := '0';
  _error_msg := '';

  IF COALESCE(_id_archivo, 0) = 0 THEN
    _error_code := 'MC001';
    _error_msg := 'El id de archivo es obligatorio';
    RETURN;
  END IF;

  IF COALESCE(_id_multicaja, '') = '' THEN
    _error_code := 'MC002';
    _error_msg := 'El id multicaja es obligatorio';
    RETURN;
  END IF;

  IF COALESCE(_id_cliente, 0) = 0 THEN
    _error_code := 'MC003';
    _error_msg := 'El id de cliente es obligatorio';
    RETURN;
  END IF;

  IF _monto IS NULL THEN
    _error_code := 'MC004';
    _error_msg := 'El monto es obligatorio';
    RETURN;
  END IF;

  IF _fecha_trx IS NULL THEN
    _error_code := 'MC005';
    _error_msg := 'La fecha de transaccion es obligatoria';
    RETURN;
  END IF;


  INSERT INTO ${schema}.prp_movimiento_switch (
    id_archivo,
    id_multicaja,
    id_cliente,
    id_multicaja_ref,
    monto,
    fecha_trx
  )
  VALUES (
    _id_archivo,
    _id_multicaja,
    _id_cliente,
    _id_multicaja_ref,
    _monto,
    _fecha_trx
  ) RETURNING id INTO _r_id;

  INSERT INTO ${schema}.prp_movimiento_switch_hist (
    id_archivo,
    id_multicaja,
    id_cliente,
    id_multicaja_ref,
    monto,
    fecha_trx
  )
  VALUES (
    _id_archivo,
    _id_multicaja,
    _id_cliente,
    _id_multicaja_ref,
    _monto,
    _fecha_trx
  );

  EXCEPTION
    WHEN OTHERS THEN
      _error_code := SQLSTATE;
      _error_msg := '[prp_crea_movimiento_switch] Error al guardar movimiento switch hist. CAUSA ('|| SQLERRM ||')';

  RETURN;
END;
$$ LANGUAGE plpgsql;


-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.prp_crea_movimiento_switch_v10(BIGINT, VARCHAR, BIGINT, BIGINT, NUMERIC, TIMESTAMP);
