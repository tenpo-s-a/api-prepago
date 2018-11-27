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

CREATE OR REPLACE FUNCTION prepaid_accounting.mc_prp_insert_accounting_data_v10(
_id_tx bigint,
_type character varying,
_origin character varying,
_amount numeric,
_currency numeric,
_ammount_usd numeric,
_exchange_rate_dif numeric,
_fee numeric,
_fee_iva numeric,
_transaction_date timestamp without time zone,
OUT _id bigint,
OUT _error_code character varying,
OUT _error_msg character varying
)
RETURNS record
LANGUAGE plpgsql
AS $function$
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

IF _transaction_date = NULL THEN
_error_code := 'MC004';
_error_msg := 'El _transaction_date es obligatorio';
RETURN;
END IF;

INSERT INTO prepaid_accounting.accounting
(
  id_tx,
  type,
  origin,
  amount,
  currency,
  ammount_usd,
  exchange_rate_dif,
  fee,
  fee_iva,
  transaction_date,
  create_date,
  update_date
)
VALUES
(
  _id_tx,
  _type,
  _origin,
  _amount,
  _currency,
  _ammount_usd,
  _exchange_rate_dif,
  _fee,
  _fee_iva,
  _transaction_date,
  timezone('utc', now()),
  timezone('utc', now())
)
RETURNING id INTO _id;


EXCEPTION
WHEN OTHERS THEN
_error_code := SQLSTATE;
_error_msg := '[mc_prp_insert_accounting_data] Error al Insertar Nuevo Dato Contable. CAUSA ('|| SQLERRM ||')';
RETURN;
END;
$function$


-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_prp_insert_accounting_data_v10(BIGINT, VARCHAR, VARCHAR);
