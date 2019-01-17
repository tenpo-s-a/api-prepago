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

-- // create_sp_search_clearing_data_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.mc_acc_search_clearing_data_v10
(
    IN  _in_id                BIGINT,
    IN  _in_status            VARCHAR,
    OUT _id                   BIGINT,
    OUT _accounting_id        BIGINT,
    OUT _user_account_id      BIGINT,
    OUT _file_id              BIGINT,
    OUT _status               VARCHAR,
    OUT _created              TIMESTAMP,
    OUT _updated              TIMESTAMP
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
    SELECT
      id,
      accounting_id,
      user_account_id,
      file_id,
      status,
      created,
      updated
    FROM
     ${schema.acc}.clearing
    WHERE
      (COALESCE(_in_id, 0) = 0 OR id = _in_id) AND
      (TRIM(COALESCE(_in_status,'')) = '' OR status = _in_status);

  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.mc_acc_search_clearing_data_v10(BIGINT, VARCHAR);
