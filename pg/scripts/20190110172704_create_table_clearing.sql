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

-- // create_table_clearing
-- Migration SQL that makes the change goes here.

  CREATE TABLE ${schema.acc}.clearing (
      id                    BIGSERIAL NOT NULL,
      accounting_id         BIGINT  REFERENCES ${schema.acc}.accounting(id),
      user_account_id       BIGINT NOT NULL,
      file_id               BIGINT NOT NULL,
      status                VARCHAR(20) NOT NULL,
      created               TIMESTAMP NOT NULL,
      updated               TIMESTAMP NOT NULL,
      CONSTRAINT clearing_pk PRIMARY KEY(id),
      CONSTRAINT clearing_u1 UNIQUE(accounting_id,user_account_id)
  );
  CREATE INDEX clearing_i1 ON ${schema.acc}.clearing (id);

-- //@UNDO
-- SQL to undo the change goes here.
  DROP TABLE IF EXISTS ${schema.acc}.clearing;
