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

-- // create_sp_mc_prp_crear_usuario
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_crear_usuario_v10
(
 IN _id_usuario_mc   BIGINT,
 IN _rut             INTEGER,
 IN _estado          VARCHAR,
 OUT _r_id            BIGINT,
 OUT _error_code     VARCHAR,
 OUT _error_msg      VARCHAR
) AS $$
 DECLARE
 BEGIN
    _error_code := '0';
    _error_msg := '';

    IF COALESCE(_id_usuario_mc, 0) = 0 THEN
      _error_code := 'MC001';
      _error_msg := 'El _id_usuario_mc es obligatorio';
      RETURN;
    END IF;

    IF COALESCE(_rut, 0) = 0 THEN
      _error_code := 'MC002';
      _error_msg := 'El _rut es obligatorio';
      RETURN;
    END IF;

    IF TRIM(COALESCE(_estado, '')) = '' THEN
      _error_code := 'MC003';
      _error_msg := 'El _estado es obligatorio';
      RETURN;
    END IF;

     INSERT INTO ${schema}.prp_usuario
     (
       id_usuario_mc,
       rut,
       estado,
       fecha_creacion,
       fecha_actualizacion
     )
     VALUES
     (
        _id_usuario_mc,
        _rut,
        _estado,
        timezone('utc', now()),
        timezone('utc', now())
     )
     RETURNING id INTO _r_id;

   EXCEPTION
     WHEN OTHERS THEN
         _error_code := SQLSTATE;
         _error_msg := '[mc_prp_crear_usuario_v10] Error al insertar usuario. CAUSA ('|| SQLERRM ||')';
     RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_crear_usuario_v10(BIGINT, INTEGER, VARCHAR, BIGINT, VARCHAR, VARCHAR)

