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

-- // create_function_getNumAut
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.getNumAut() RETURNS text AS
$$
 DECLARE
    xLastNumAut  NUMERIC;
    xNumAutOut   VARCHAR;
 BEGIN

    SELECT
      nextval('${schema}."numaut"')
    INTO
      xLastNumAut;

    IF(xLastNumAut = 999999) THEN
     ALTER SEQUENCE ${schema}.numaut RESTART WITH 1;
    END IF;

    SELECT
      lpad(xLastNumAut::varchar, 6, '0')
    INTO
      xNumAutOut;
  RETURN xNumAutOut;
 END;
$$
LANGUAGE 'plpgsql';

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.getNumAut();

