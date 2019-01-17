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

-- // create_sp_mc_acc_update_accounting_files_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_update_accounting_file_v10
(
  IN _id                BIGINT,
  IN _file_id           VARCHAR,
  IN _url               VARCHAR,
  IN _status            VARCHAR,
  OUT _error_code       VARCHAR,
  OUT _error_msg        VARCHAR
)AS $$
 DECLARE

  BEGIN
    _error_code := '0';
    _error_msg := '';

    IF COALESCE(_id, 0) = 0 THEN
      _error_code := 'MC001';
      _error_msg := '[mc_acc_update_accounting_file_v10] El _id es obligatorio';
      RETURN;
    END IF;

    UPDATE
      ${schema.acc}.accounting_files
    SET
      file_id = ( CASE WHEN _file_id IS NOT NULL THEN
                    _file_id
                  ELSE
                    file_id
                  END
                ),
      url = ( CASE WHEN _url IS NOT NULL THEN
                    _url
                  ELSE
                    url
                  END
                ),
      status = ( CASE WHEN _status IS NOT NULL THEN
                    _status
                  ELSE
                    status
                  END
                )
    WHERE
      id = _id;
    IF NOT FOUND THEN
      _error_code := '404';
      _error_msg := '[mc_acc_update_accounting_file_v10] Data not found';
      RETURN;
    END IF;

  EXCEPTION
  WHEN OTHERS THEN
    _error_code := SQLSTATE;
    _error_msg := '[mc_acc_update_accounting_file_v10] Error al actualizar accounting file. CAUSA ('|| SQLERRM ||')';
    RETURN;
END;
$$
LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_update_accounting_file_v10(BIGINT, VARCHAR, VARCHAR,VARCHAR);
