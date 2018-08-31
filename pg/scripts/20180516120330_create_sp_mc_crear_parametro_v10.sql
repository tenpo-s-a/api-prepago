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

-- // create_sp_mc_crear_parametro_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.parameters}.mc_crear_parametro_v10
(
 IN _aplicacion      VARCHAR,
 IN _nombre          VARCHAR,
 IN _version         VARCHAR,
 IN _valor           TEXT,
 IN _expiracion      BIGINT,
 OUT _r_id           BIGINT,
 OUT _error_code     VARCHAR,
 OUT _error_msg      VARCHAR
) AS $$
 DECLARE
 BEGIN
    _error_code := '0';
    _error_msg := '';

    IF COALESCE(_aplicacion, '') = '' THEN
      _error_code := '101000';
      _error_msg := 'La _aplicacion es obligatorio';
      RETURN;
    END IF;

    IF COALESCE(_nombre, '') = '' THEN
      _error_code := '101000';
      _error_msg := 'El _nombre es obligatorio';
      RETURN;
    END IF;

    IF COALESCE(_version, '') = '' THEN
      _error_code := '101000';
      _error_msg := 'La _version es obligatorio';
      RETURN;
    END IF;

    IF TRIM(COALESCE(_valor, '')) = '' THEN
      _error_code := '101000';
      _error_msg := 'El _valor es obligatorio';
      RETURN;
    END IF;

    IF COALESCE(_expiracion, 0) = 0 THEN
      _error_code := '101000';
      _error_msg := 'la _expiracion es obligatorio';
      RETURN;
    END IF;

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
        _aplicacion,
        _nombre,
        _version,
        to_json(_valor::json),
        _expiracion,
        timezone('utc', now())
     )
     RETURNING id INTO _r_id;

   EXCEPTION
     WHEN OTHERS THEN
         _error_code := SQLSTATE;
         _error_msg := '[mc_crear_parametro_v10] Error al insertar parametro. CAUSA ('|| SQLERRM ||')';
     RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.parameters}.mc_crear_parametro_v10(VARCHAR, VARCHAR, VARCHAR, TEXT, BIGINT);
