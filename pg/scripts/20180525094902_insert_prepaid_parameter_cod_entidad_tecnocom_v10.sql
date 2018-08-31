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

-- // insert_prepaid_parameter_cod_entidad_tecnocom
-- Migration SQL that makes the change goes here.

INSERT INTO ${schema.parameters}.mc_parametro
 (
   aplicacion,
   nombre,
   version,
   valor,
   expiracion,
   fecha_creacion
 )
 VALUES
 (
    'api-prepaid',
    'cod_entidad',
    'v10',
    '{"value": "${params.tecnocom.cod_entidad}"}',
    86400000,
    timezone('utc', now())
 );

-- //@UNDO
-- SQL to undo the change goes here.

DELETE FROM ${schema.parameters}.mc_parametro WHERE aplicacion = 'api-prepaid' AND nombre = 'cod_entidad' AND version = 'v10';
