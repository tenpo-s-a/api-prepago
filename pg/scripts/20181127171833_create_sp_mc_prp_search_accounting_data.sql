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
CREATE OR REPLACE FUNCTION ${schema.acc}.mc_prp_search_accounting_data_v10(_in_create_date character varying, OUT id bigint, OUT _id_tx bigint, OUT _type character varying, OUT _origin character varying, OUT _amount numeric, OUT _currency numeric, OUT _ammount_usd numeric, OUT _exchange_rate_dif numeric, OUT _fee numeric, OUT _fee_iva numeric, OUT _transaction_date timestamp without time zone, OUT _create_date timestamp without time zone, OUT _update_date timestamp without time zone)
RETURNS SETOF record
LANGUAGE plpgsql
AS $function$
BEGIN
RETURN QUERY
SELECT
  accounting.id,
  accounting.id_tx,
  accounting.type,
  accounting.origin,
  accounting.amount,
  accounting.currency,
  accounting.ammount_usd,
  accounting.exchange_rate_dif,
  accounting.fee,
  accounting.fee_iva,
  accounting.transaction_date,
  accounting.create_date,
  accounting.update_date
FROM
  ${schema.acc}.accounting
WHERE accounting.create_date::date >= _in_create_date::date
  AND accounting.create_date::date <= _in_create_date::date
ORDER BY
  accounting.create_date DESC;
RETURN;
END;
$function$

-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema.acc}.mc_prp_search_accounting_data_v10(VARCHAR);


