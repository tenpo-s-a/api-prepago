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

-- // create_sp_mc_prp_search_clearing_data
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_search_clearing_data_for_file_v10
(
  IN _in_from VARCHAR,
  IN _in_to VARCHAR,
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
  OUT _transaction_date TIMESTAMP without time zone,
  OUT _conciliation_date TIMESTAMP without time zone,
  OUT _created TIMESTAMP without time zone,
  OUT _updated TIMESTAMP without time zone,
  OUT _user_account_id BIGINT
)
RETURNS SETOF record
LANGUAGE plpgsql
AS $function$
BEGIN

RETURN QUERY
  SELECT
    c.id,
	  a.id_tx,
	  a.type,
	  a.accounting_mov,
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
    c.status,
    c.file_id,
    a.transaction_date,
    a.conciliation_date,
    c.created,
    c.updated,
    c.user_account_id
  FROM
    ${schema.acc}.clearing c
    INNER JOIN ${schema.acc}.accounting a
      ON a.id = c.accounting_id
  WHERE
    c.created >= TO_TIMESTAMP(_in_from, 'YYYY-MM-DD HH24:MI:SS') AND
    c.created <= TO_TIMESTAMP(_in_to, 'YYYY-MM-DD HH24:MI:SS')
  ORDER BY
    c.created DESC;
RETURN;
END;
$function$

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_search_clearing_data_for_file_v10(VARCHAR, VARCHAR);
