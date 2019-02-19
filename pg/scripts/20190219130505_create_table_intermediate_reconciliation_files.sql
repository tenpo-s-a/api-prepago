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

-- // create_table_intermediate_reconciliation_files
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema}.prp_reconciliation_files (
  id                  BIGSERIAL NOT NULL,
  nombre_de_archivo   VARCHAR(255) NOT NULL,
  proceso   		      VARCHAR(50) NOT NULL,
  fecha_recepcion     TIMESTAMP NOT NULL,
  tipo 				        VARCHAR(50) NOT NULL,
  status			        VARCHAR(50) NOT NULL,
  CONSTRAINT prp_reconciliation_files_pk PRIMARY KEY(id)
);

CREATE INDEX prp_reconciliation_files_i1 ON ${schema}.prp_reconciliation_files (id);
CREATE INDEX prp_reconciliation_files_i2 ON ${schema}.prp_reconciliation_files (nombre_de_archivo);
CREATE INDEX prp_reconciliation_files_i3 ON ${schema}.prp_reconciliation_files (proceso);
CREATE INDEX prp_reconciliation_files_i4 ON ${schema}.prp_reconciliation_files (fecha_recepcion);
CREATE INDEX prp_reconciliation_files_i5 ON ${schema}.prp_reconciliation_files (status);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema}.prp_reconciliation_files;

