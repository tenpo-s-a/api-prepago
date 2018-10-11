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

-- // create_sp_crea_movimiento_investigar
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_crea_movimiento_investigar_v10(
	IN _mov_ref			    VARCHAR,
	IN _origen			    VARCHAR,
	IN _nombre_archivo	VARCHAR,
	OUT _error_code		  VARCHAR,
	OUT _error_msg		  VARCHAR
)AS $$
DECLARE
BEGIN
	_error_code := '0';
	_error_msg := '';

	IF COALESCE(_mov_ref,'') = '' THEN
	  _error_code := '101000';
      _error_msg := 'El _mov_ref es obligatorio';
      RETURN;
    END IF;

	IF COALESCE(_origen,'') = '' THEN
	  _error_code := '101000';
      _error_msg := 'El _origen es obligatorio';
      RETURN;
    END IF;

    IF COALESCE(_nombre_archivo,'') = '' THEN
	  _error_code := '101000';
      _error_msg := 'El _nombre_archivo es obligatorio';
      RETURN;
    END IF;

    INSERT INTO ${schema}.prp_movimiento_investigar
    (
      mov_ref,
      origen,
      nombre_archivo,
      fecha_registro
    )
    VALUES
    (
      _mov_ref,
      _origen,
      _nombre_archivo,
      timezone('utc', now())
    );

EXCEPTION
	WHEN OTHERS THEN
     _error_code := SQLSTATE;
     _error_msg := '[mc_prp_crea_movimiento_investigar_v10] Error al insertar Movimiento conciliado. CAUSA ('|| SQLERRM ||')';
 RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_crea_movimiento_investigar_v10(VARCHAR,VARCHAR,VARCHAR);



