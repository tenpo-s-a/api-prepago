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

-- // Insert template pdf
-- Migration SQL that makes the change goes here.
INSERT INTO ${schema}.mc_parametro
(
   aplicacion,
   nombre,
   version,
   valor,
   expiracion,
   fecha_creacion
 )
VALUES
 ('api-prepaid',
  'card_pdf_template',
  'v1.0',
  '{"value":"<!DOCTYPE html><html><head> </head><body><h1>Los datos de su tarjeta prepago son: </h1><p>Numero Tarjea: ${numtar}</p><p>Vencimiento:  ${venc}</p><p>CVC:  ${cvc}</p></body></html>"}',
   3600000,
   timezone('utc', now())
 );

-- //@UNDO
-- SQL to undo the change goes here.

DELETE FROM ${schema}.mc_parametro WHERE aplicacion = 'api-prepaid' AND nombre = 'card_pdf_template' AND version = 'v1.0';
