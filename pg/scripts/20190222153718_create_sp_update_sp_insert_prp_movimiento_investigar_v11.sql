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

-- // create_sp_update_sp_insert_prp_movimiento_investigar_v11
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_crea_movimiento_investigar_v11(
	IN _id_archivo_origen	    VARCHAR,
	IN _origen			          VARCHAR,
	IN _nombre_archivo	      VARCHAR,
	IN _fecha_de_transaccion  TIMESTAMP,
	IN _responsable           VARCHAR,
	IN _descripcion           VARCHAR,
	IN _mov_ref               BIGINT,
	OUT _r_id                 BIGINT,
	OUT _error_code		        VARCHAR,
	OUT _error_msg		        VARCHAR
)AS $$

BEGIN
	_error_code := '0';
	_error_msg := '';

-- TODO: Esta validacion esta de mas
IF
COALESCE(_id_archivo_origen,'') = '' AND
COALESCE(_origen,'') = '' AND
COALESCE(_nombre_archivo,'') = '' AND
COALESCE(_fecha_de_transaccion,to_timestamp(0)) = to_timestamp(0) AND
COALESCE(_responsable,'') = '' AND
COALESCE(_descripcion,'') = '' AND
COALESCE(_mov_ref,0) = 0
THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v11] id_archivo_origen, origen, nombre_archivo, fecha_de_transaccion, responsable, descripcion y mov_ref son campos obligatorios';
RETURN;
END IF;

IF COALESCE(_id_archivo_origen,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v11] El id_archivo_origen es obligatorio';
RETURN;
END IF;

IF COALESCE(_origen,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v11] El origen es obligatorio';
RETURN;
END IF;

IF COALESCE(_nombre_archivo,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v11] El nombre_archivo es obligatorio';
RETURN;
END IF;

IF COALESCE(_fecha_de_transaccion,to_timestamp(0)) = to_timestamp(0) THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v11] La fecha_de_transaccion es obligatoria';
RETURN;
END IF;

IF COALESCE(_responsable,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v11] El responsable es obligatorio';
RETURN;
END IF;

IF COALESCE(_descripcion,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v11] La descripcion es obligatoria';
RETURN;
END IF;

INSERT INTO ${schema}.prp_movimiento_investigar
(
id_archivo_origen,
origen,
nombre_archivo,
fecha_registro,
fecha_de_transaccion,
responsable,
descripcion,
mov_ref
)
VALUES
(
_id_archivo_origen,
_origen,
coalesce(_nombre_archivo,''),
timezone('utc', now()),
_fecha_de_transaccion,
_responsable,
_descripcion,
_mov_ref
-- TODO: se puede verificar el id de movimiento ref se puede validar de esta forma
--,
--coalesce(_mov_ref,0)
) RETURNING id INTO _r_id;

EXCEPTION WHEN OTHERS THEN

-- TODO: esta validacion esta de mas, verificar todo anterior
IF COALESCE(_mov_ref,cast(_mov_ref as bigint)) = cast(_mov_ref as bigint) THEN
RETURN;
ELSE
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v11] El mov_ref es obligatorio';
RETURN;
END IF;

IF COALESCE(_mov_ref,NOT NULL) = NOT NULL THEN
_error_code := SQLSTATE;
_error_msg := '[mc_prp_crea_movimiento_investigar_v11] Error al insertar Movimiento conciliado. CAUSA ('|| SQLERRM ||')';
RETURN;
END IF;

END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_crea_movimiento_investigar_v11(VARCHAR,VARCHAR,VARCHAR,TIMESTAMP,VARCHAR,VARCHAR,BIGINT);



