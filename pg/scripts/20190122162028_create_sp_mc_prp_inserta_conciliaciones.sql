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
--                    BIGSERIAL NOT NULL,

-- // create_sp_mc_prp_inserta_conciliaciones
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_inserta_conciliaciones_v10
(
  IN _id_movimiento     BIGINT,
  IN _tipo              VARCHAR,
  IN _status            VARCHAR,
  OUT _r_id             BIGINT,
  OUT _error_code       VARCHAR,
  OUT _error_msg        VARCHAR
) AS $$
 DECLARE

 BEGIN

  _error_code := '0';
  _error_msg := '';

  IF COALESCE(_id_movimiento, 0) = 0 THEN
    _error_code := 'MC001';
    _error_msg := 'El _id_movimiento es obligatorio';
    RETURN;
  END IF;

  IF COALESCE(_tipo, '') = '' THEN
    _error_code := 'MC002';
    _error_msg := 'El _tipo es obligatorio';
    RETURN;
  END IF;

  IF COALESCE(_status, '') = '' THEN
    _error_code := 'MC003';
    _error_msg := 'El _status es obligatorio';
    RETURN;
  END IF;

  INSERT INTO ${schema}.prp_conciliaciones (
   id_movimiento,
   tipo,
   status,
   created,
   updated
  )
  VALUES (
    _id_movimiento,
    _tipo,
    _status,
    timezone('utc', now()),
    timezone('utc', now())
  ) RETURNING id INTO _r_id;

  EXCEPTION
  WHEN OTHERS THEN
    _error_code := SQLSTATE;
    _error_msg := '[mc_prp_inserta_conciliaciones_v10] Error al guardar conciliaciones. CAUSA ('|| SQLERRM ||')';
  RETURN;

END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_inserta_conciliaciones_v10(BIGINT, VARCHAR, VARCHAR);

