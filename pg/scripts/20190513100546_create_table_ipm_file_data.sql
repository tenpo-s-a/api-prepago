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

-- // create_table_ipm_file_data
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema.acc}.ipm_file_data (
    id                                  BIGSERIAL NOT NULL,
    file_id                             BIGINT NOT NULL,
    message_type                        INTEGER NOT NULL DEFAULT 0,
    function_code                       INTEGER NOT NULL DEFAULT 0,
    message_reason                      INTEGER NOT NULL DEFAULT 0,
    message_number                      INTEGER NOT NULL DEFAULT 0,
    pan                                 VARCHAR(19) NOT NULL DEFAULT 0,
    transaction_amount                  NUMERIC NOT NULL DEFAULT 0,
    reconciliation_amount               NUMERIC NOT NULL DEFAULT 0,
    cardholder_billing_amount           NUMERIC NOT NULL DEFAULT 0,
    reconciliation_conversion_rate      NUMERIC NOT NULL DEFAULT 0,
    cardholder_billing_conversion_rate  NUMERIC NOT NULL DEFAULT 0,
    transaction_local_date              TIMESTAMP NOT NULL,
    approval_code                       VARCHAR(6) NOT NULL DEFAULT '',
    transaction_currency_code           INTEGER NOT NULL DEFAULT 0,
    reconciliation_currency_code        INTEGER NOT NULL DEFAULT 0,
    cardholder_billing_currency_code    INTEGER NOT NULL DEFAULT 0,
    merchant_code                       VARCHAR(15) NOT NULL DEFAULT '',
    merchant_name                       VARCHAR(22) NOT NULL DEFAULT '',
    merchant_state                      VARCHAR(13) NOT NULL DEFAULT '',
    merchant_country                    VARCHAR(3) NOT NULL DEFAULT '',
    transaction_life_cycle_id           VARCHAR(16) NOT NULL DEFAULT '',
    reconciled                          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                          TIMESTAMP NOT NULL,
    updated_at                          TIMESTAMP NOT NULL,
    CONSTRAINT ipm_file_data_pk PRIMARY KEY(id)
);

comment on column ${schema.acc}.ipm_file_data.message_type is 'MTI Message Type Identifier';
comment on column ${schema.acc}.ipm_file_data.function_code is 'DE24 Function Code';
comment on column ${schema.acc}.ipm_file_data.message_reason is 'DE25 Message reason code';
comment on column ${schema.acc}.ipm_file_data.message_number is 'DE71 Message number';
comment on column ${schema.acc}.ipm_file_data.pan is 'DE2 Pan (masked)';
comment on column ${schema.acc}.ipm_file_data.transaction_amount is 'DE4 Transaction amount';
comment on column ${schema.acc}.ipm_file_data.reconciliation_amount is 'DE5 Reconciliation amount';
comment on column ${schema.acc}.ipm_file_data.cardholder_billing_amount is 'DE6 Cardholder billing amount';
comment on column ${schema.acc}.ipm_file_data.reconciliation_conversion_rate is 'DE9 Reconciliation conversion rate';
comment on column ${schema.acc}.ipm_file_data.cardholder_billing_conversion_rate is 'DE10 Cardholder billing conversion rate';
comment on column ${schema.acc}.ipm_file_data.transaction_local_date is 'DE12 Transaction local date time';
comment on column ${schema.acc}.ipm_file_data.approval_code is 'DE38 Approval code';
comment on column ${schema.acc}.ipm_file_data.transaction_currency_code is 'DE49 Transaction currency code';
comment on column ${schema.acc}.ipm_file_data.reconciliation_currency_code is 'DE50 Reconciliation currency code';
comment on column ${schema.acc}.ipm_file_data.cardholder_billing_currency_code is 'DE51 Cardholder billing currency code';
comment on column ${schema.acc}.ipm_file_data.merchant_code is 'DE42 Card acceptor id';
comment on column ${schema.acc}.ipm_file_data.merchant_name is 'DE43 - Merchant name';
comment on column ${schema.acc}.ipm_file_data.merchant_state is 'DE43 Merchant state';
comment on column ${schema.acc}.ipm_file_data.merchant_country is 'DE43 - Merchant country';
comment on column ${schema.acc}.ipm_file_data.transaction_life_cycle_id is 'DE63 TransactionLifeCycleId';

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE IF EXISTS ${schema.acc}.ipm_file_data CASCADE;
