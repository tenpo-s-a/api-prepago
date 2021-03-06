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

-- // alter_table_clearing
-- Migration SQL that makes the change goes here.

ALTER TABLE ${schema.acc}.clearing
  ADD COLUMN bank_id          NUMERIC(10) NOT NULL DEFAULT 0,
  ADD COLUMN account_number   NUMERIC(15) NOT NULL DEFAULT 0,
  ADD COLUMN account_type     VARCHAR(20) NOT NULL DEFAULT '',
  ADD COLUMN account_rut      NUMERIC(15) NOT NULL DEFAULT 0;
-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE ${schema.acc}.clearing
  DROP COLUMN bank_id,
  DROP COLUMN account_number,
  DROP COLUMN account_type,
  DROP COLUMN account_rut;
