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

CREATE TABLE ${schema}.prp_archivos_reconciliacion (
  id                  BIGSERIAL NOT NULL,
  nombre_de_archivo   VARCHAR(255) NOT NULL,
  proceso   		      VARCHAR(50) NOT NULL,
  tipo 				        VARCHAR(50) NOT NULL,
  status			        VARCHAR(50) NOT NULL,
  created_at          TIMESTAMP NOT NULL,
  updated_at          TIMESTAMP NOT NULL,
  CONSTRAINT prp_archivos_reconciliacion_pk PRIMARY KEY(id)
);

CREATE INDEX prp_archivos_reconciliacion_i1 ON ${schema}.prp_archivos_reconciliacion (id);
CREATE INDEX prp_archivos_reconciliacion_i2 ON ${schema}.prp_archivos_reconciliacion (nombre_de_archivo);
CREATE INDEX prp_archivos_reconciliacion_i3 ON ${schema}.prp_archivos_reconciliacion (proceso);
CREATE INDEX prp_archivos_reconciliacion_i4 ON ${schema}.prp_archivos_reconciliacion (status);
CREATE INDEX prp_archivos_reconciliacion_i5 ON ${schema}.prp_archivos_reconciliacion (created_at);
CREATE INDEX prp_archivos_reconciliacion_i6 ON ${schema}.prp_archivos_reconciliacion (updated_at);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema}.prp_archivos_reconciliacion;

