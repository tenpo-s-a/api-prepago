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
      id              BIGSERIAL NOT NULL,
      id_tx           INT8 NOT NULL,
      type            VARCHAR NOT NULL,
      origin          VARCHAR NOT NULL,
      amount          NUMERIC NOT NULL,
      currency        NUMERIC NOT NULL,
      ammount_usd     NUMERIC NOT NULL,
      exchange_rate_dif NUMERIC NOT NULL,
      fee               NUMERIC NOT NULL,
      fee_iva           NUMERIC NOT NULL,
      transaction_date  TIMESTAMP NOT NULL,
      create_date  TIMESTAMP NULL,
      update_date  TIMESTAMP NULL,
      CONSTRAINT accounting_pk PRIMARY KEY(id)
  );
  CREATE INDEX accounting_i1 ON ${schema.acc}.accounting (id);


-- //@UNDO
-- SQL to undo the change goes here.
  DROP TABLE IF EXISTS ${schema.acc}.accounting;

  DROP SCHEMA IF EXISTS ${schema.acc};


