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

-- // alter_table_movimientos_tecnocom
-- Migration SQL that makes the change goes here.

ALTER TABLE ${schema}.prp_movimientos_tecnocom
  ADD COLUMN tiporeg VARCHAR(5) NOT NULL DEFAULT '';
  
ALTER TABLE ${schema}.prp_movimientos_tecnocom
  ADD COLUMN nomcomred VARCHAR(5) NOT NULL DEFAULT '';

ALTER TABLE ${schema}.prp_movimientos_tecnocom_hist
  ADD COLUMN tiporeg VARCHAR(5) NOT NULL DEFAULT '';
    
ALTER TABLE ${schema}.prp_movimientos_tecnocom_hist
  ADD COLUMN nomcomred VARCHAR(5) NOT NULL DEFAULT '';

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE ${schema}.prp_movimientos_tecnocom_hist
  DROP COLUMN tiporeg;
  
ALTER TABLE ${schema}.prp_movimientos_tecnocom_hist
  DROP COLUMN nomcomred;

ALTER TABLE ${schema}.prp_movimientos_tecnocom
  DROP COLUMN tiporeg;
  
ALTER TABLE ${schema}.prp_movimientos_tecnocom
  DROP COLUMN nomcomred;

