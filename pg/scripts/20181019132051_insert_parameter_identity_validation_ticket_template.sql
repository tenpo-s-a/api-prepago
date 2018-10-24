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

-- // insert_parameter_identity_validation_ticket_template
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
  'identity_validation_ticket_template',
  'v1.0',
  '{"value":"<div><ol><li><span><strong>INSTRUCCIONES</strong></span><br/>Debes validar identidad del cliente respondiendo preguntas del ticket, seleccionando opciones SI o NO.<br /> <ul><li>Pregunta 1: verificar si concuerda la foto de la persona de la c&eacute;dula con la foto de la selfie y seleccionar respuesta.</li><li>Pregunta 2: verificar si el RUT concuerda con el de la c&eacute;dula de identidad y seleccionar respuesta.</li><li>Pregunta 3: ingresar a url, verificar estado de documento y seleccionar respuesta (<a href=''https://bit.ly/2ANOfqT'' target=''_blank''>https://bit.ly/2ANOfqT</a>).</li><li>Pregunta 4: verificar si el nombre y primer apellido del cliente concuerda con los de la c&eacute;dula de identidad y seleccionar respuesta. En el caso de que no concuerde el nombre o 1er apellido,  sobrescribir en el campo indicado</li><li>Pregunta 5: verificar y seleccionar respuesta (lista negra).</li></ul></li><li><strong><span>Datos</span></strong><ul><li><span>Rut:</span>  ${rut}</li><li><span>N° de serie:</span> ${numSerie}</li><li><span>Nombre:</span> ${name}</li><li><span>Apellido:</span> ${lastname}</li></ul></li><li><strong><span>Fotos</span></strong><ul><li><img src=''${ciFront}''/></li><li><img src=''${ciBack}''></li><li><img src=''${ciSelfie}''></li></ul></li></ol></div>"}',
   3600000,
   timezone('utc', now())
 );

-- //@UNDO
-- SQL to undo the change goes here.

DELETE FROM ${schema}.mc_parametro WHERE aplicacion = 'api-prepaid' AND nombre = 'identity_validation_ticket_template' AND version = 'v1.0';
