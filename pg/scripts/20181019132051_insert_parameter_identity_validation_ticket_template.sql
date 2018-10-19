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
  '{"value":"<div><ol><li><span><strong>INSTRUCCIONES</strong></span><br/>DEBES VALIDAR IDENTIDAD DEL CLIENTE RESPONDIENDO PREGUNTAS DELTICKET, SELECCIONANDO OPCIONES SI O NO.<br /> <ul><li>PREGUNTA1: VERIFICAR SI CONCUERDA LA FOTO DE LA PERSONA DE LA C&Eacute;DULA CON LA FOTO DE LA SELFIE.</li><li>PREGUNTA 2: VERIFICAR Y SELECCIONAR RESPUESTA.</li><li>PREGUNTA 3: INGRESAR A URL , VERIFICAR ESTADO DE DOCUMENTO Y SELECCIONAR RESPUESTA (<a href=''HTTPS://BIT.LY/2ANOFT''>HTTPS://BIT.LY/2ANOFT</a>).</li><li>PREGUNTA4: VERIFICAR Y SELECCIONAR RESPUESTA. DE LO CONTRARIO, EN CASO DE QUE NO CONCUERDE EL NOMBRE Y 1 APELLIDO , SOBRESCRIBIR EN EL CAMPO INDICADO.</li><li>PREGUNTA5: VERIFICAR Y SELECCIONAR RESPUESTA (LISTA NEGRA).</li></ul></li><li><strong><span>DATOS</span></strong><ul><li><span>RUT:</span>  ${rut}</li><li><span>N DE SERIE:</span> ${numSerie}</li><li><span>NOMBRE:</span> ${name}</li><li><span>APELLIDO:</span> ${lastname}</li></ul></li><li><strong><span>FOTOS</span></strong><ul><li><img src=''${ciFront}''/></li><li><img src=''${ciBack}''></li><li><img src=''${ciSelfie}''></li></ul></li></ol></div>"}',
   3600000,
   timezone('utc', now())
 );

-- //@UNDO
-- SQL to undo the change goes here.

DELETE FROM ${schema}.mc_parametro WHERE aplicacion = 'api-prepaid' AND nombre = 'identity_validation_ticket_template' AND version = 'v1.0';
