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

-- // mc_acc_create_clearing_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_create_clearing_data_v10
(
  IN _accounting_id     BIGINT,
  IN _user_account_id   BIGINT,
  IN _file_id           BIGINT,
  IN _status            VARCHAR,
  OUT _r_id             BIGINT,
  OUT _error_code       VARCHAR,
  OUT _error_msg        VARCHAR
)AS $$
 DECLARE

  BEGIN
    _error_code := '0';
    _error_msg := '';

    IF COALESCE(_accounting_id, 0) = 0 THEN
      _error_code := 'MC001';
      _error_msg := '[mc_acc_create_clearing_data_v10] El accounting_id es obligatorio';
      RETURN;
    END IF;

    IF COALESCE(_status, '') = '' THEN
      _error_code := 'MC002';
      _error_msg := '[mc_acc_create_clearing_data_v10] El status es obligatorio';
      RETURN;
    END IF;


    INSERT INTO ${schema.acc}.clearing
    (
      accounting_id,
      user_account_id,
      file_id,
      status,
      created,
      updated
    )
    VALUES
    (
      _accounting_id,
      COALESCE(_user_account_id,0),
      COALESCE(_file_id,0),
      _status,
      timezone('utc', now()),
      timezone('utc', now())
    ) RETURNING id INTO _r_id;

  EXCEPTION
  WHEN OTHERS THEN
    _error_code := SQLSTATE;
    _error_msg := '[mc_acc_create_clearing_data_v10] Error al insertar clearing data. CAUSA ('|| SQLERRM ||')';
    RETURN;
END;
$$
LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_create_clearing_data_v10(BIGINT, BIGINT, BIGINT, VARCHAR);
