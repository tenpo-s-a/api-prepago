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

-- // create_table_acc_accounting
-- Migration SQL that makes the change goes here.
  CREATE SCHEMA IF NOT EXISTS ${schema.acc};

  CREATE TABLE ${schema.acc}.accounting (
      id                    BIGSERIAL NOT NULL,
      id_tx                 BIGINT NOT NULL,
      type                  VARCHAR(20) NOT NULL,
      accounting_mov        VARCHAR(20) NOT NULL,
      origin                VARCHAR(20) NOT NULL,
      amount                NUMERIC(15,2) NOT NULL,
      currency              NUMERIC(3,0) NOT NULL,
      amount_usd            NUMERIC(15,2) NOT NULL,
      amount_mcar           NUMERIC(15,2) NOT NULL,
      exchange_rate_dif     NUMERIC(15,2) NOT NULL,
      fee                   NUMERIC(15,2) NOT NULL,
      fee_iva               NUMERIC(15,2) NOT NULL,
      collector_fee        NUMERIC(15,2) NOT NULL,
      collector_fee_iva    NUMERIC(15,2) NOT NULL,
      amount_balance        NUMERIC(15,2) NOT NULL,
      status                VARCHAR(20) NOT NULL,
      file_id               BIGINT NOT NULL, -- Puede ser null
      accounting_status     VARCHAR(20) NOT NULL,
      transaction_date      TIMESTAMP NOT NULL,
      conciliation_date     TIMESTAMP NOT NULL,
      create_date           TIMESTAMP NOT NULL,
      update_date           TIMESTAMP NOT NULL,
      CONSTRAINT accounting_pk PRIMARY KEY(id)
  );
  CREATE INDEX accounting_i1 ON ${schema.acc}.accounting (id);

-- //@UNDO
-- SQL to undo the change goes here.
  DROP TABLE IF EXISTS ${schema.acc}.accounting;

  DROP SCHEMA IF EXISTS ${schema.acc} CASCADE;


