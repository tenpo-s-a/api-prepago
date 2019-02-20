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

-- // mc_prp_update_accounting
-- Migration SQL that makes the change goes here.



CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_update_accounting_full_data_v10
(
  IN _id_tx              BIGINT,
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
  IN _accounting_status  VARCHAR,
  OUT _error_code        VARCHAR,
  OUT _error_msg         VARCHAR
)AS $$
 DECLARE

  BEGIN
    _error_code := '0';
    _error_msg := '';

    IF COALESCE(_id_tx, 0) = 0 THEN
      _error_code := 'MC001';
      _error_msg := '[mc_acc_update_accounting_full_data_v10] El _id_tx es obligatorio';
      RETURN;
    END IF;


    UPDATE
      ${schema.acc}.accounting
    SET

       amount= ( CASE WHEN _amount IS NOT NULL THEN
                    _amount
                  ELSE
                    amount
                  END
                ),
       currency= ( CASE WHEN _currency IS NOT NULL THEN
                    _currency
                  ELSE
                    currency
                  END
                ),
       amount_usd=( CASE WHEN _amount_usd IS NOT NULL THEN
                    _amount_usd
                  ELSE
                    amount_usd
                  END
                ),
       amount_mcar=( CASE WHEN _amount_mcar IS NOT NULL THEN
                    _amount_mcar
                  ELSE
                    amount_mcar
                  END
                ),
       exchange_rate_dif=( CASE WHEN _exchange_rate_dif IS NOT NULL THEN
                    _exchange_rate_dif
                  ELSE
                    exchange_rate_dif
                  END
                ),
       fee=( CASE WHEN _fee IS NOT NULL THEN
                    _fee
                  ELSE
                    fee
                  END
                ),
       fee_iva=( CASE WHEN _fee_iva IS NOT NULL THEN
                    _fee_iva
                  ELSE
                    fee_iva
                  END
                ),
       collector_fee=( CASE WHEN _collector_fee IS NOT NULL THEN
                    _collector_fee
                  ELSE
                    collector_fee
                  END
                ),
       collector_fee_iva=( CASE WHEN _collector_fee_iva IS NOT NULL THEN
                    _collector_fee_iva
                  ELSE
                    collector_fee_iva
                  END
                ),
       amount_balance=( CASE WHEN _amount_balance IS NOT NULL THEN
                    _amount_balance
                  ELSE
                    amount_balance
                  END
                ),
       status=( CASE WHEN _status IS NOT NULL THEN
                    _status
                  ELSE
                    status
                  END
                ),
       accounting_status=(CASE WHEN _accounting_status IS NOT NULL THEN
                  _accounting_status
                ELSE
                  accounting_status
                END
              ),
       update_date= timezone('utc', now())
    WHERE
      id_tx = _id_tx;

    IF NOT FOUND THEN
      _error_code := '404';
      _error_msg := '[mc_acc_update_accounting_full_data_v10] Data not found';
      RETURN;
    END IF;

  EXCEPTION
  WHEN OTHERS THEN
    _error_code := SQLSTATE;
    _error_msg := '[mc_acc_update_accounting_full_data_v10] Error al actualizar clearing data. CAUSA ('|| SQLERRM ||')';
    RETURN;
END;
$$
LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_update_accounting_full_data_v10(BIGINT, NUMERIC, NUMERIC, NUMERIC, NUMERIC,NUMERIC,NUMERIC,NUMERIC,NUMERIC,NUMERIC,NUMERIC,VARCHAR,VARCHAR,VARCHAR,VARCHAR);
