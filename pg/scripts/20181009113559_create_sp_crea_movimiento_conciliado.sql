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

-- // create_sp_crea_movimiento_conciliado
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_crea_movimiento_conciliado_v10(
	IN _id_mov_ref		BIGINT,
	IN _accion			VARCHAR,
	IN _estado			VARCHAR,
	OUT _error_code		VARCHAR,
	OUT _error_msg		VARCHAR
)AS $$
DECLARE
BEGIN
	_error_code := '0';
	_error_msg := '';

	IF COALESCE(_id_mov_ref,0) = 0 THEN
	    _error_code := '101000';
      _error_msg := 'El _id es obligatorio';
      RETURN;
    END IF;

	IF COALESCE(_accion,'') = '' THEN
	    _error_code := '101000';
      _error_msg := 'El _accion es obligatorio';
      RETURN;
    END IF;

    IF COALESCE(_estado,'') = '' THEN
	    _error_code := '101000';
      _error_msg := 'El _estado es obligatorio';
      RETURN;
    END IF;

    INSERT INTO ${schema}.prp_movimiento_conciliado
    (
      id_mov_ref,
      fecha_registro,
      accion,
      estado
    )
    VALUES
    (
      _id_mov_ref,
      timezone('utc', now()),
      _accion,
      _estado
    );

EXCEPTION
	WHEN OTHERS THEN
     _error_code := SQLSTATE;
     _error_msg := '[mc_prp_crea_movimiento_conciliado_v10] Error al insertar Movimiento conciliado. CAUSA ('|| SQLERRM ||')';
 RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.


DROP FUNCTION IF EXISTS ${schema}.mc_prp_crea_movimiento_conciliado_v10(BIGINT,VARCHAR,VARCHAR);

