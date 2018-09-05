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

-- // create_function_create_app_file_v10
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.mc_prp_create_app_file_v10(
  IN _in_name         VARCHAR,
  IN _in_version      VARCHAR,
  IN _in_description  VARCHAR,
  IN _in_mime_type    VARCHAR,
  IN _in_location     TEXT,
  OUT _id_app_file    BIGINT,
  OUT _error_code     VARCHAR,
  OUT _error_msg      VARCHAR
 ) AS $$
DECLARE
BEGIN
  _error_code := '0';
  _error_msg := '';
  _id_app_file = 0;

  IF COALESCE(_in_name, '') = '' THEN
     _error_code := '101000';
     _error_msg := 'El nombre del archivo es obligatorio';
     RETURN;
  END IF;

  IF COALESCE(_in_version, '') = '' THEN
     _error_code := '101000';
     _error_msg := 'La version del archivo es obligatoria';
     RETURN;
  END IF;

  IF COALESCE(_in_description, '') = '' THEN
     _error_code := '101000';
     _error_msg := 'La descripcion del archivo es obligatoria';
     RETURN;
  END IF;

  IF COALESCE(_in_mime_type, '') = '' THEN
     _error_code := '101000';
     _error_msg := 'El mime type del archivo es obligatorio';
     RETURN;
  END IF;

  IF COALESCE(_in_location, '') = '' THEN
     _error_code := '101000';
     _error_msg := 'El location del archivo es obligatorio';
     RETURN;
  END IF;

  INSERT INTO ${schema}.prp_app_file(
      status,
      name,
      version,
      description,
      mime_type,
      location,
      created_at,
      updated_at
  ) VALUES (
      'ACTIVE',
      _in_name,
      _in_version,
      _in_description,
      _in_mime_type,
      _in_location,
      timezone('utc', now()),
      timezone('utc', now())
  ) RETURNING id INTO _id_app_file;

EXCEPTION
     WHEN OTHERS THEN
         _error_code := SQLSTATE;
         _error_msg := '[mc_prp_create_app_file_v10] Error al insertar app file. CAUSA ('|| SQLERRM ||')';
     RETURN;
END;
$$ LANGUAGE plpgsql;


-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_create_app_file_v10(VARCHAR, VARCHAR, VARCHAR,VARCHAR, TEXT);

