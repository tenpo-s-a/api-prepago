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

-- // create_function_get_app_file_v10
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.mc_prp_get_app_file_v10(
  IN _in_id         BIGINT,
  IN _in_name       VARCHAR,
  IN _in_version    VARCHAR,
  IN _in_status     VARCHAR,
  OUT _id           BIGINT,
  OUT _name         VARCHAR,
  OUT _version      VARCHAR,
  OUT _description  VARCHAR,
  OUT _mime_type    VARCHAR,
  OUT _location     TEXT,
  OUT _status       VARCHAR,
  OUT _created_at   TIMESTAMP,
  OUT _updated_at   TIMESTAMP
)
RETURNS SETOF RECORD AS $$
DECLARE
BEGIN
     RETURN QUERY
	     SELECT
					af.id,
          af.name,
					af.version,
          af.description,
          af.mime_type,
          af.location,
          af.status,
          af.created_at,
          af.updated_at
			  FROM
			    ${schema}.prp_app_file as af
			  WHERE
			    (COALESCE(_in_id,0) = 0 OR _in_id = af.id) AND
          (COALESCE(TRIM(_in_name),'') = '' OR _in_name = af.name) AND
			    (COALESCE(TRIM(_in_version),'') = '' OR _in_version = af.version) AND
			    (COALESCE(_in_status,'') = '' OR _in_status = af.status)
        ORDER BY af.id DESC;
     RETURN;
END;
$$ LANGUAGE plpgsql;


-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_get_app_file_v10(BIGINT, VARCHAR, VARCHAR, INTEGER);

