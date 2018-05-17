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

-- // create_sp_mc_cdt_crea_limite
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.cdt}.mc_cdt_crea_limite_v10
(
    IN _id_fase_movimiento     NUMERIC,
    IN _id_regla_acumulacion   NUMERIC,
    IN _descripcion            VARCHAR,
    IN _valor    		           DECIMAL,
    IN _cod_operacion          VARCHAR,
    OUT _num_error             VARCHAR,
    OUT _msj_error             VARCHAR
) AS $$

    	DECLARE

    	BEGIN
	        _num_error := '0';
	        _msj_error := '';

		    IF COALESCE(_id_fase_movimiento, 0) = 0 THEN
	          _num_error := 'MC001';
	        	_msj_error := '[mc_cdt_crea_limite] El Id Fase Movimiento no puede ser 0';
	        	RETURN;
	      END IF;

        IF COALESCE(_id_regla_acumulacion, 0) = 0 THEN
	          _num_error := 'MC002';
	        	_msj_error := '[mc_cdt_crea_limite] El Id Regla Acumulacion no puede ser 0';
	        	RETURN;
	      END IF;

        IF COALESCE(_descripcion, '') = '' THEN
            _num_error := 'MC003';
            _msj_error := '[mc_cdt_crea_limite] La descripcion no puede estar vacia';
            RETURN;
        END IF;


        IF COALESCE(_cod_operacion, '') = '' THEN
            _num_error := '1004';
            _msj_error := '[mc_cdt_crea_limite] El Codigo de operacion no puede estar vacio';
            RETURN;
        END IF;

        INSERT INTO ${schema.cdt}.cdt_limite
        (
          id_fase_movimiento,
          id_regla_acumulacion,
          descripcion,
          valor,
          cod_operacion,
          estado,
          fecha_estado,
          fecha_creacion
        )
        VALUES
          (
            _id_fase_movimiento,
            _id_regla_acumulacion,
            _descripcion,
            _valor,
            _cod_operacion,
            'ACTIVO',
            timezone('utc', now()),
            timezone('utc', now())
          );
      EXCEPTION
          WHEN OTHERS THEN
              _num_error := SQLSTATE;
              _msj_error := '[mc_cdt_crea_limite] Error al crear Limite. CAUSA ('|| SQLERRM ||')';
          RETURN;
    END;
$$
LANGUAGE 'plpgsql';

-- //@UNDO
-- SQL to undo the change goes here.
  DROP FUNCTION IF EXISTS ${schema.cdt}.mc_cdt_crea_limite_v10(NUMERIC,NUMERIC,VARCHAR,NUMERIC,VARCHAR);
