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
CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_create_accounting_file_v10
(
  IN _name        VARCHAR,
  IN _file_id     VARCHAR,
  IN _type        VARCHAR,
  IN _format      VARCHAR,
  IN _url         VARCHAR,
  IN _status      VARCHAR,
  OUT _r_id       BIGINT,
  OUT _error_code VARCHAR,
  OUT _error_msg  VARCHAR
)AS $$
 DECLARE

  BEGIN
    _error_code := '0';
    _error_msg := '';

    IF COALESCE(_name, '') = '' THEN
      _error_code := 'MC001';
      _error_msg := '[mc_acc_create_accounting_file_v10] El nombre es obligatorio';
      RETURN;
    END IF;

    IF COALESCE(_type, '') = '' THEN
      _error_code := 'MC003';
      _error_msg := '[mc_acc_create_accounting_file_v10] El type es obligatorio';
      RETURN;
    END IF;

   IF COALESCE(_format, '') = '' THEN
      _error_code := 'MC004';
      _error_msg := '[mc_acc_create_accounting_file_v10] El format es obligatorio';
      RETURN;
   END IF;

   IF COALESCE(_status, '') = '' THEN
      _error_code := 'MC005';
      _error_msg := '[mc_acc_create_accounting_file_v10] El status es obligatorio';
      RETURN;
   END IF;

  INSERT INTO ${schema.acc}.accounting_files
  (
     name,
     file_id,
     type,
     format,
     url,
     status,
     created,
     updated
  )
  VALUES
  (
     _name,
     coalesce(_file_id,''),
     _type,
     _format,
     COALESCE(_url,''),
     _status,
     timezone('utc', now()),
     timezone('utc', now())
  ) RETURNING id INTO _r_id;

  EXCEPTION
  WHEN OTHERS THEN
    _error_code := SQLSTATE;
    _error_msg := '[mc_acc_create_accounting_file_v10] Error al insertar accounting file. CAUSA ('|| SQLERRM ||')';
    RETURN;
END;
$$
LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_create_accounting_file_v10(VARCHAR, VARCHAR, VARCHAR,VARCHAR,VARCHAR,VARCHAR);
