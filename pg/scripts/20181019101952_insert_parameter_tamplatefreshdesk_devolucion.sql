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

-- // insert_parameter_tamplatefreshdesk_colas
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
  'template_ticket_devolucion',
  'v1.0',
  '{"value":"<!DOCTYPE html><html><head></head><body><p>&nbsp;</p><h1>Error en el proceso de carga</h1><h2>Usuario: {nombres}</h2><h2>Rut: {rut}</h2><h2>ID Usuario: {IdUsuario}</h2><h2>Email Usuario: {email}</h2><h2>Telefono Usuario: {telefono}</h2><h2>N&deg; Autorizacion: {numaut}</h2><h2>Monto: {monto}</h2><h3>&nbsp;</h3><p>Contacte al usuario y devuelva el dinero. Una vez concretada la devolucion cierre este ticket.</p></body></html>"}',
   3600000,
   timezone('utc', now())
 );


-- //@UNDO
-- SQL to undo the change goes here.
DELETE FROM ${schema}.mc_parametro WHERE aplicacion = 'api-prepaid' AND nombre = 'template_ticket_devolucion' AND version = 'v1.0';

