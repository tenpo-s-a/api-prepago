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

-- // create_sp_mc_prp_actualizar_estado_tarjeta_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_actualizar_estado_tarjeta_v10
(
 IN _id             BIGINT,
 IN _estado         VARCHAR,
 OUT _error_code    VARCHAR,
 OUT _error_msg     VARCHAR
) AS $$
 DECLARE
 BEGIN
    _error_code := '0';
    _error_msg := '';

    IF COALESCE(_id, 0) = 0 THEN
      _error_code := 'MC001';
      _error_msg := 'El _id es obligatorio';
      RETURN;
    END IF;

    IF TRIM(COALESCE(_estado, '')) = '' THEN
      _error_code := 'MC002';
      _error_msg := 'El _estado es obligatorio';
      RETURN;
    END IF;

     UPDATE ${schema}.prp_tarjeta
     SET
        estado = _estado,
        fecha_actualizacion = timezone('utc', now())
     WHERE
        id = _id;

   EXCEPTION
     WHEN OTHERS THEN
         _error_code := SQLSTATE;
         _error_msg := '[mc_prp_actualizar_estado_tarjeta_v10] Error al actualizar estado de tarjeta. CAUSA ('|| SQLERRM ||')';
     RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_actualizar_estado_tarjeta_v10(BIGINT, VARCHAR, VARCHAR, VARCHAR);
