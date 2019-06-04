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

-- // alter_card_delete_old_colums
-- Migration SQL that makes the change goes here.

  ALTER TABLE  ${schema}.prp_tarjeta
    DROP COLUMN id_usuario;

  ALTER TABLE  ${schema}.prp_tarjeta
    DROP COLUMN contrato;


-- //@UNDO
-- SQL to undo the change goes here.

  ALTER TABLE ${schema}.prp_tarjeta
    ADD COLUMN id_usuario BIGINT NOT NULL DEFAULT 0;

  ALTER TABLE ${schema}.prp_tarjeta
    ADD COLUMN contrato VARCHAR(20) NOT NULL DEFAULT 0
