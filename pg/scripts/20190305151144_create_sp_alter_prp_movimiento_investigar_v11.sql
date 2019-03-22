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

-- // create_sp_alter_prp_movimiento_investigar_v11.sql
-- Migration SQL that makes the change goes here.

ALTER TABLE ${schema}.prp_movimiento_investigar DROP COLUMN nombre_archivo;

ALTER TABLE ${schema}.prp_movimiento_investigar
  ALTER COLUMN mov_ref TYPE numeric USING mov_ref::numeric(100,0);

ALTER TABLE ${schema}.prp_movimiento_investigar
  RENAME id_archivo_origen TO informacion_archivos;

ALTER TABLE ${schema}.prp_movimiento_investigar
ALTER COLUMN informacion_archivos TYPE json USING informacion_archivos::json;

ALTER TABLE ${schema}.prp_movimiento_investigar
ALTER COLUMN origen TYPE character varying (100) COLLATE pg_catalog."default";

ALTER TABLE ${schema}.prp_movimiento_investigar
ALTER COLUMN responsable TYPE character varying (100) COLLATE pg_catalog."default";

ALTER TABLE ${schema}.prp_movimiento_investigar
  ADD COLUMN tipo_movimiento character varying(100) NOT NULL;

ALTER TABLE ${schema}.prp_movimiento_investigar
  ADD COLUMN sent_status character varying(100) NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE ${schema}.prp_movimiento_investigar DROP COLUMN sent_status;

ALTER TABLE ${schema}.prp_movimiento_investigar DROP COLUMN tipo_movimiento;

ALTER TABLE ${schema}.prp_movimiento_investigar
  RENAME informacion_archivos TO id_archivo_origen;

ALTER TABLE ${schema}.prp_movimiento_investigar
  ALTER COLUMN id_archivo_origen TYPE character varying (100) COLLATE pg_catalog."default";

ALTER TABLE ${schema}.prp_movimiento_investigar
  ALTER COLUMN origen TYPE character varying (50) COLLATE pg_catalog."default";

ALTER TABLE ${schema}.prp_movimiento_investigar
  ALTER COLUMN responsable TYPE character varying (50) COLLATE pg_catalog."default";

ALTER TABLE ${schema}.prp_movimiento_investigar
  ALTER COLUMN mov_ref TYPE bigint ;

ALTER TABLE ${schema}.prp_movimiento_investigar
  ADD COLUMN nombre_archivo character varying (100) NOT NULL COLLATE pg_catalog."default";




