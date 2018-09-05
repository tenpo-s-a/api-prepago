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

-- // create_table_apps_file_v10
-- Migration SQL that makes the change goes here.
CREATE TABLE ${schema}.prp_app_file(
  id            BIGSERIAL,
  name          VARCHAR(25) not null,
  version       VARCHAR(10) NOT NULL,
  description   VARCHAR(100) not null,
  mime_type     VARCHAR(50) not null,
  location      TEXT not null,
  status        VARCHAR(20) NOT NULL,
  created_at    TIMESTAMP WITHOUT TIME ZONE DEFAULT transaction_timestamp() NOT NULL,
  updated_at    TIMESTAMP WITHOUT TIME ZONE DEFAULT transaction_timestamp() NOT NULL,
  CONSTRAINT    prp_app_file_pk PRIMARY KEY(id),
  CONSTRAINT    prp_app_file_u1 UNIQUE(name, version, status)
);


-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema}.prp_app_file;

