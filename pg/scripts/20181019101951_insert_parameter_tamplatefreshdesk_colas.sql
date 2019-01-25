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
  'template_ticket_cola_1',
  'v1.0',
  '{"value":"<!DOCTYPE html><html><head></head><body><p>&nbsp;</p><h1>Error en el proceso {process}</h1><h2>Usuario: {nombres}</h2><h2>Rut: {rut}</h2><h2>ID Usuario: {IdUsuario}</h2><h2>N&deg; Autorizacion: {numaut}</h2><h2>Monto: {monto}</h2><h3>&nbsp;</h3><h3>! Importante &iexcl;</h3><p><strong>Instrucciones para primera vez:</strong></p><p>1. Seleccionar el bot&oacute;n checkbox reintentar autom&aacute;ticamente que se encuentra en la zona superior de la columna del centro.</p><p>2. Al resolver el ticket, modificar el estado del ticket en status abierto a status cerrado.</p><p>3. Presionar el bot&oacute;n actualizar en la zona inferior de la columna del centro</p><p><strong>Instrucciones para segunda vez:</strong></p><p>1.Para realizar la carga, ingresar a la aplicaci&oacute;n de Sat y buscar el contrato del cliente con el rut, considerando la siguiente ruta: Gesti&oacute;n de Operaciones &gt; Posici&oacute;n econ&oacute;mica del contrato &gt; Posici&oacute;n global &gt; Consulta contrato &gt; Opciones &gt; Inclusi&oacute;n movimiento extracto.</p><p>2.Realizar una inclusi&oacute;n de movimiento en el extracto con los datos que se adjuntan, considerando los siguientes campos:</p><p>2.1. Pan: seleccionar el pan encriptado.</p><p>2.2. Transacci&oacute;n: se debe seleccionar normal.</p><p>2.3. Importe: monto indicado en el ticket.</p><p>2.4. Datos del comercio: siempre seleccionar ajeno.</p><p>2.5 C&oacute;digo del comercio: 01</p><p>2.6. Pa&iacute;s: escribir el c&oacute;digo 152 en el campo vac&iacute;o (Chile).</p><p>2.7. Tipo de Factura: Carga efectivo Comercio Multicaja o carga transferencia seg&uacute;n corresponda.</p><p>2.8 Moneda: seleccionar la moneda.</p><p>2.9. N&uacute;mero de autorizaci&oacute;n: escribir el n&uacute;mero de autorizaci&oacute;n que viene en el ticket.</p><p>2.10. Sector de actividad: ingresar c&oacute;digo de actividad 6012.</p><p>3. Al resolver el ticket, modificar el estado del ticket desde status abierto a status cerrado.</p><p>4. Presionar el bot&oacute;n actualizar en la zona inferior de la columna del centro.</p><p>5. De no poder resolver, escalar ticket y asignar a grupo Prepago. Datos: -Usuario -Rut -ID Usuario -N&deg; Autorizaci&oacute;n -Mont</p></body></html>"}',
   3600000,
   timezone('utc', now())
 );

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
  'template_ticket_cola_2',
  'v1.0',
  '{"value":"<!DOCTYPE html><html><head></head><body><p>&nbsp;</p><h1>Error en el proceso {process}</h1><h2>Usuario: {nombres}</h2><h2>Rut: {rut}</h2><h2>ID Usuario: {IdUsuario}</h2><h3>&nbsp;</h3><h3>! Importante &iexcl;</h3><p><strong>Instruccciones para primera vez</strong></p><p>1. Seleccionar el bot&oacute;n checkbox reintentar autom&aacute;ticamente. que se encuentra en la zona superior de la columna del centro.</p><p>2. Modificar el estado del ticket en status abierto a status cerrado. 3. Cerrar el ticket presionando el bot&oacute;n actualizar en la zona inferior de la columna del centro. Datos -Usuario -Rut -ID Usuario -N&deg; Autorizaci&oacute;n -Monto</p><p><strong>Instruccciones para segunda vez</strong></p><p>1. Para realizar la reversa, se debe ingresar a la aplicaci&oacute;n de Sat y buscar el contrato con el rut de cliente, considerando la siguiente ruta: Gesti&oacute;n de Operaciones &gt; Posici&oacute;n econ&oacute;mica del contrato &gt; Posici&oacute;n global &gt; consulta contrato &gt; Opciones&gt; Inclusi&oacute;n movimiento extracto.</p><p>2. Realizar una inclusi&oacute;n de movimiento en el extracto con los datos que se adjuntan, considerando los siguientes campos:</p><p>2.1. Pan: seleccionar el pan encriptado.</p><p>2.2. Transacci&oacute;n: se debe seleccionar correctora</p><p>2.3. Importe: monto indicado en el ticket.</p><p>2.4. Datos del comercio: siempre seleccionar ajeno.</p><p>2.5. C&oacute;digo del comercio: 01.</p><p>2.6. Pa&iacute;s: escribir el c&oacute;digo 152 en el campo vac&iacute;o (Chile).</p><p>2.7. Tipo de Factura: anula efectivo comercio multicaja o anula transferencia seg&uacute;n corresponda.</p><p>2.8. Moneda: seleccionar la moneda.</p><p>2.9. N&uacute;mero de autorizaci&oacute;n: escribir el n&uacute;mero de autorizaci&oacute;n que viene en el ticket.</p><p>2.10. Sector de actividad: ingresar c&oacute;digo de actividad 6012.</p><p>3. Al resolver el ticket, modificar el campo estadodesde status abierto a status cerrado.</p><p>4. Cerrar el ticket presionando el bot&oacute;n actualizar en la zona inferior de la columna del centro.</p><p>5. En el caso de no poder resolver el caso, escalar ticket y asignar a grupo Prepago. Datos -Usuario -Rut -ID Usuario -N&deg; Autorizaci&oacute;n -Monto</p></body></html>"}',
   3600000,
   timezone('utc', now())
 );



-- //@UNDO
-- SQL to undo the change goes here.
DELETE FROM ${schema}.mc_parametro WHERE aplicacion = 'api-prepaid' AND nombre = 'template_ticket_cola_1' AND version = 'v1.0';

DELETE FROM ${schema}.mc_parametro WHERE aplicacion = 'api-prepaid' AND nombre = 'template_ticket_cola_2' AND version = 'v1.0';
