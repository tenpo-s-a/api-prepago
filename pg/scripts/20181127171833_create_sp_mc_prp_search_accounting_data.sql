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

-- // create_sp_mc_prp_search_accounting_data
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema.acc}.mc_prp_search_accounting_data_v10
(
  IN _in_create_date VARCHAR,
  OUT _id BIGINT,
  OUT _id_tx BIGINT,
  OUT _type VARCHAR,
  OUT _accounting_mov VARCHAR,
  OUT _origin VARCHAR,
  OUT _amount NUMERIC,
  OUT _currency NUMERIC,
  OUT _amount_usd NUMERIC,
  OUT _amount_mcar NUMERIC,
  OUT _exchange_rate_dif NUMERIC,
  OUT _fee NUMERIC,
  OUT _fee_iva NUMERIC,
  OUT _collector_fee NUMERIC,
  OUT _collector_fee_iva NUMERIC,
  OUT _amount_balance NUMERIC,
  OUT _status VARCHAR,
  OUT _file_id BIGINT,
  OUT _accounting_status VARCHAR,
  OUT _transaction_date TIMESTAMP without time zone,
  OUT _conciliation_date TIMESTAMP without time zone,
  OUT _create_date TIMESTAMP without time zone,
  OUT _update_date TIMESTAMP without time zone
)
RETURNS SETOF record
LANGUAGE plpgsql
AS $function$
BEGIN

RETURN QUERY
  SELECT
    id,
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
    accounting_status,
    transaction_date,
    conciliation_date,
    create_date,
    update_date
  FROM
    ${schema.acc}.accounting
  WHERE
    create_date::DATE >= _in_create_date::DATE AND
    create_date::DATE <= _in_create_date::DATE
  ORDER BY
    create_date DESC;
RETURN;
END;
$function$

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema.acc}.mc_prp_search_accounting_data_v10(VARCHAR);


