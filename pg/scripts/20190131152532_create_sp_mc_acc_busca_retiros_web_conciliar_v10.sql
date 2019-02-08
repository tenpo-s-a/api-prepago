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

-- // create_sp_mc_acc_busca_retiros_web_conciliar_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_busca_retiros_web_conciliar_v10
(
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
  OUT _user_account_id BIGINT,
  OUT _accounting_id BIGINT
)
RETURNS SETOF record
LANGUAGE plpgsql
AS $function$
BEGIN

RETURN QUERY
  SELECT
    cle.id,
	  acc.id_tx,
	  acc.type,
	  acc.accounting_mov,
	  acc.origin,
	  acc.amount,
	  acc.currency,
	  acc.amount_usd,
    acc.amount_mcar,
    acc.exchange_rate_dif,
    acc.fee,
    acc.fee_iva,
    acc.collector_fee,
    acc.collector_fee_iva,
    acc.amount_balance,
    cle.status,
    cle.file_id,
    acc.transaction_date,
    acc.conciliation_date,
    cle.created,
    cle.updated,
    cle.user_account_id,
    cle.accounting_id
  FROM
    ${schema.acc}.clearing cle
    INNER JOIN ${schema.acc}.accounting acc ON acc.id = cle.accounting_id
    INNER JOIN ${schema}.prp_movimiento pm ON acc.id_tx = pm.id
    LEFT JOIN ${schema}.prp_movimiento_conciliado pmc on acc.id_tx = pmc.id_mov_ref
  where
  	pmc.id_mov_ref is null and
  	acc."type" = 'RETIRO_WEB' and
    (cle.status != 'PENDING' and cle.status != 'SENT') and
    pm.estado_con_tecnocom != 'PENDING'
  ORDER BY
    cle.id ASC;

RETURN;
END;
$function$

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_busca_retiros_web_conciliar_v10();
