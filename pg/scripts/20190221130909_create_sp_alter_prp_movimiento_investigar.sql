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

-- // create_sp_alter_prp_movimiento_investigar
-- Migration SQL that makes the change goes here.

ALTER TABLE ${schema}.prp_movimiento_investigar RENAME mov_ref TO id_archivo_origen;

ALTER TABLE ${schema}.prp_movimiento_investigar ALTER COLUMN id_archivo_origen TYPE character varying (100) COLLATE pg_catalog."default";

ALTER TABLE ${schema}.prp_movimiento_investigar ADD COLUMN fecha_de_transaccion timestamp without time zone NOT NULL;

ALTER TABLE ${schema}.prp_movimiento_investigar ADD COLUMN responsable character varying(50) NOT NULL;

ALTER TABLE ${schema}.prp_movimiento_investigar ADD COLUMN descripcion character varying(100) NOT NULL;

ALTER TABLE ${schema}.prp_movimiento_investigar ADD COLUMN mov_ref bigint NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE ${schema}.prp_movimiento_investigar DROP COLUMN mov_ref;

ALTER TABLE ${schema}.prp_movimiento_investigar DROP COLUMN descripcion;

ALTER TABLE ${schema}.prp_movimiento_investigar DROP COLUMN responsable;

ALTER TABLE ${schema}.prp_movimiento_investigar DROP COLUMN fecha_de_transaccion;

ALTER TABLE ${schema}.prp_movimiento_investigar RENAME id_archivo_origen TO mov_ref;

ALTER TABLE ${schema}.prp_movimiento_investigar ALTER COLUMN mov_ref TYPE character varying (100) COLLATE pg_catalog."default";
