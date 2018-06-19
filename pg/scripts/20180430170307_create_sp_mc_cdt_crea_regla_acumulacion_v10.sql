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

-- // create_sp_mc_cdt_crea_regla_acumulacion
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.cdt}.mc_cdt_crea_regla_acumulacion_v10
(
    IN _periocidad               VARCHAR,
    IN _codigo_operacion         VARCHAR,
    IN _descripcion              VARCHAR,
    OUT _num_error               VARCHAR,
    OUT _msj_error               VARCHAR
)AS $$

    	DECLARE

    	BEGIN
	        _num_error := '0';
	        _msj_error := '';


        IF TRIM(COALESCE(_periocidad, '')) = '' THEN
          _num_error := 'MC001';
          _msj_error := '[mc_cdt_crea_regla_acumulacion] La Periocidad no puede ser vacia';
          RETURN;
        END IF;

         IF TRIM(COALESCE(_codigo_operacion, '')) = '' THEN
            _num_error := 'MC002';
            _msj_error := '[mc_cdt_crea_regla_acumulacion] El Codigo Operacion no puede ser vacio';
            RETURN;
         END IF;

        	INSERT INTO ${schema.cdt}.cdt_regla_acumulacion
	    		(
            periocidad,
	    			codigo_operacion,
	    			descripcion,
            estado,
	    			fecha_estado,
	    			fecha_creacion
	    		)
        	VALUES
        		(
              _periocidad,
              _codigo_operacion,
              _descripcion,
              'ACTIVO',
        			timezone('utc', now()),
        			timezone('utc', now())
        		);
        EXCEPTION
            WHEN OTHERS THEN
                _num_error := SQLSTATE;
                _msj_error := '[mc_cdt_crea_regla_acumulacion] Error al crear Regla acumulacion. CAUSA ('|| SQLERRM ||')';
            RETURN;
    	END;
$$
LANGUAGE 'plpgsql';

-- //@UNDO
-- SQL to undo the change goes here.
 DROP FUNCTION IF EXISTS ${schema.cdt}.mc_cdt_crea_regla_acumulacion_v10(VARCHAR,VARCHAR,VARCHAR);
