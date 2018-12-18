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

-- // craate_sp_mc_acc_create_ipm_file_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_create_ipm_file_v10
(
  IN _file_name            VARCHAR,
  IN _file_id              VARCHAR,
  IN _message_count        NUMERIC,
  IN _status               VARCHAR,
  OUT _r_id                BIGINT,
  OUT _error_code          VARCHAR,
  OUT _error_msg           VARCHAR
) AS $$
 DECLARE
 BEGIN
    _error_code := '0';
    _error_msg := '';

IF COALESCE(_file_name, '') = '' THEN
_error_code := 'MC001';
_error_msg := 'El _file_name es obligatorio';
RETURN;
END IF;

IF COALESCE(_file_id, '') = '' THEN
_error_code := 'MC002';
_error_msg := 'El _file_id es obligatorio';
RETURN;
END IF;

IF COALESCE(_message_count, 0) = 0 THEN
_error_code := 'MC003';
_error_msg := 'El _message_count es obligatorio';
RETURN;
END IF;

IF COALESCE(_status, '') = '' THEN
_error_code := 'MC004';
_error_msg := 'El _status es obligatorio';
RETURN;
END IF;

INSERT INTO ${schema.acc}.ipm_file (
  file_name,
  file_id,
  message_count,
  status,
  create_date,
  update_date
) VALUES (
  _file_name,
  _file_id,
  _message_count,
  _status,
  timezone('utc', now()),
  timezone('utc', now())
) RETURNING id INTO _r_id;

EXCEPTION
WHEN OTHERS THEN
_error_code := SQLSTATE;
_error_msg := '[mc_acc_create_ipm_file_v10] Error guardar archivo IPM. CAUSA ('|| SQLERRM ||')';
RETURN;

END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_create_ipm_file_v10(VARCHAR, VARCHAR, NUMERIC, VARCHAR);
