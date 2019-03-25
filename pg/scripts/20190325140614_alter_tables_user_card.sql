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

-- // alter_tables_user_card
-- Migration SQL that makes the change goes here.

ALTER TABLE ${schema}.prp_usuario
  ADD COLUMN uuid             UUID DEFAULT uuid_generate_v4(),
  ADD COLUMN nombre           VARCHAR(30) NOT NULL DEFAULT '',
  ADD COLUMN apellido         VARCHAR(30) NOT NULL DEFAULT '',
  ADD COLUMN numero_documento VARCHAR(30) NOT NULL DEFAULT '',
  ADD COLUMN tipo_documento   VARCHAR(20) NOT NULL DEFAULT '',
  ADD COLUMN nivel            NUMERIC(1) NOT NULL DEFAULT 0;

ALTER TABLE ${schema}.prp_tarjeta
  ADD COLUMN uuid       UUID NOT NULL DEFAULT uuid_generate_v4(),
  ADD COLUMN pan_hash   VARCHAR(200) NOT NULL DEFAULT '',
  ADD COLUMN id_cuenta  BIGINT NOT NULL DEFAULT 0;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE ${schema}.prp_usuario
  DROP COLUMN uuid,
  DROP COLUMN nombre,
  DROP COLUMN apellido,
  DROP COLUMN numero_documento,
  DROP COLUMN tipo_documento,
  DROP COLUMN nivel;

ALTER TABLE ${schema}.prp_tarjeta
  DROP COLUMN uuid,
  DROP COLUMN pan_hash,
  DROP COLUMN id_cuenta;
