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

-- // create_sp_mc_prp_insert_accounting_data
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.mc_prp_insert_accounting_data_v10
(
  IN _id_tx              BIGINT,
  IN _type               VARCHAR,
  IN _accounting_mov     VARCHAR,
  IN _origin             VARCHAR,
  IN _amount             NUMERIC,
  IN _currency           NUMERIC,
  IN _amount_usd         NUMERIC,
  IN _amount_mcar        NUMERIC,
  IN _exchange_rate_dif  NUMERIC,
  IN _fee                NUMERIC,
  IN _fee_iva            NUMERIC,
  IN _collector_fee      NUMERIC,
  IN _collector_fee_iva  NUMERIC,
  IN _amount_balance     NUMERIC,
  IN _transaction_date   VARCHAR,
  IN _conciliation_date  VARCHAR,
  IN _status             VARCHAR,
  IN _file_id            BIGINT,
  OUT _id                BIGINT,
  OUT _error_code        VARCHAR,
  OUT _error_msg         VARCHAR
) AS $$
 DECLARE

 BEGIN

  _error_code := '0';
  _error_msg := '';

  IF COALESCE(_id_tx, 0) = 0 THEN
    _error_code := 'MC001';
    _error_msg := 'El _id_tx es obligatorio';
    RETURN;
  END IF;

  IF COALESCE(_type, '') = '' THEN
    _error_code := 'MC002';
    _error_msg := 'El _type es obligatorio';
    RETURN;
  END IF;

  IF TRIM(COALESCE(_origin, '')) = '' THEN
    _error_code := 'MC003';
    _error_msg := 'El _origin es obligatorio';
    RETURN;
  END IF;

  IF TRIM(COALESCE(_status, '')) = '' THEN
    _error_code := 'MC004';
    _error_msg := 'El _status es obligatorio';
    RETURN;
  END IF;

  IF TRIM(COALESCE(_transaction_date, ''))  = '' THEN
    _error_code := 'MC005';
    _error_msg := 'El _transaction_date es obligatorio';
    RETURN;
  END IF;

  INSERT INTO ${schema.acc}.accounting(
    id_tx,
    type,
    accounting_mov,
    origin,
    amount,
    currency,
    amount_usd,
    amount_mcar,
    exchange_rate_dif,
    fee,
    fee_iva,
    collector_fee,
    collector_fee_iva,
    amount_balance,
    status,
    file_id,
    transaction_date,
    conciliation_date,
    create_date,
    update_date
  )
  VALUES
  (
    _id_tx,
    _type,
    _accounting_mov,
    _origin,
    _amount,
    _currency,
    _amount_usd,
    _amount_mcar,
    _exchange_rate_dif,
    _fee,
    _fee_iva,
    _collector_fee,
    _collector_fee_iva,
    _amount_balance,
    _status,
    _file_id,
    to_timestamp(_transaction_date,'yyyy-mm-dd hh24:mi:ss'),
    to_timestamp(_conciliation_date,'yyyy-mm-dd hh24:mi:ss'),
    timezone('utc', now()),
    timezone('utc', now())
  ) RETURNING id INTO _id;

  EXCEPTION
    WHEN OTHERS THEN
      _error_code := SQLSTATE;
      _error_msg := '[mc_prp_insert_accounting_data] Error al Insertar Nuevo Dato Contable. CAUSA ('|| SQLERRM ||')';
      RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema.acc}.mc_prp_insert_accounting_data_v10(BIGINT,VARCHAR,VARCHAR,VARCHAR,NUMERIC,NUMERIC,NUMERIC,NUMERIC,NUMERIC,NUMERIC,NUMERIC,NUMERIC,NUMERIC,NUMERIC, VARCHAR,VARCHAR,VARCHAR,BIGINT);
