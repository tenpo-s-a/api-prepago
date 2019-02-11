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

-- // create_sp_mc_acc_search_accounting_data_for_file_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_search_accounting_data_for_file_v10
(
  IN _in_from VARCHAR,
  IN _in_to VARCHAR,
  IN _in_status VARCHAR,
  OUT _id BIGINT,
  OUT _id_tx BIGINT,
  OUT _type VARCHAR,
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
  OUT _transaction_date TIMESTAMP without time zone,
  OUT _conciliation_date TIMESTAMP without time zone,
  OUT _create_date TIMESTAMP without time zone,
  OUT _update_date TIMESTAMP without time zone,
  OUT _accounting_status VARCHAR
)
RETURNS SETOF record
LANGUAGE plpgsql
AS $function$
BEGIN

RETURN QUERY
  SELECT
    a.id,
	  a.id_tx,
	  a.type,
	  a.origin,
	  a.amount,
	  a.currency,
	  a.amount_usd,
    a.amount_mcar,
    a.exchange_rate_dif,
    a.fee,
    a.fee_iva,
    a.collector_fee,
    a.collector_fee_iva,
    a.amount_balance,
    a.status,
    a.file_id,
    a.transaction_date,
    a.conciliation_date,
    a.create_date,
    a.update_date,
    a.accounting_status
  FROM
    ${schema.acc}.accounting a
    LEFT join ${schema.acc}.accounting_files f ON a.file_id = f.id
  WHERE
    (COALESCE(_in_from,'') = '' OR a.transaction_date >= TO_TIMESTAMP(_in_from, 'YYYY-MM-DD HH24:MI:SS')) AND
    (COALESCE(_in_to,'') = '' OR a.transaction_date <= TO_TIMESTAMP(_in_to, 'YYYY-MM-DD HH24:MI:SS')) AND
    (COALESCE(_in_status,'') = '' OR a.status = _in_status)
  ORDER BY
    a.transaction_date ASC;
RETURN;
END;
$function$

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_search_accounting_data_for_file_v10(VARCHAR, VARCHAR,VARCHAR);
