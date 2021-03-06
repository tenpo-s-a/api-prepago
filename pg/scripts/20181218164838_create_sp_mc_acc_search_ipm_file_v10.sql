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

-- // craate_sp_mc_acc_search_ipm_file_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_search_ipm_file_v10
(
    IN  _in_id                BIGINT,
    IN  _in_file_name         VARCHAR,
    IN  _in_file_id           VARCHAR,
    IN  _in_status            VARCHAR,
    OUT _id                   BIGINT,
    OUT _file_name            VARCHAR,
    OUT _file_id              VARCHAR,
    OUT _message_count        NUMERIC,
    OUT _status               VARCHAR,
    OUT _create_date          TIMESTAMP,
    OUT _update_date          TIMESTAMP
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
    SELECT
      id,
      file_name,
      file_id,
      message_count,
      status,
      create_date,
      update_date
    FROM
     ${schema.acc}.ipm_file
    WHERE
      (COALESCE(_in_id, 0) = 0 OR id = _in_id) AND
      (TRIM(COALESCE(_in_file_name,'')) = '' OR file_name = _in_file_name) AND
      (TRIM(COALESCE(_in_file_id,'')) = '' OR file_id = _in_file_id) AND
      (TRIM(COALESCE(_in_status,'')) = '' OR status = _in_status);

  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_search_ipm_file_v10(BIGINT, VARCHAR, VARCHAR, VARCHAR);
