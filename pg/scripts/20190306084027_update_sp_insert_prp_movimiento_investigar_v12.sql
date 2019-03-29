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

-- // update_sp_insert_prp_movimiento_investigar_v12
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_crea_movimiento_investigar_v12(
IN _in_informacion_archivos	TEXT,
IN _in_origen			          VARCHAR,
IN _in_fecha_de_transaccion  TIMESTAMP,
IN _in_responsable           VARCHAR,
IN _in_descripcion           VARCHAR,
IN _in_mov_ref               NUMERIC,
IN _in_tipo_movimiento       VARCHAR,
IN _in_sent_status           VARCHAR,
OUT _r_id                 BIGINT,
OUT _error_code		        VARCHAR,
OUT _error_msg		        VARCHAR
)AS $$

BEGIN
_error_code := '0';
_error_msg := '';

IF TRIM(COALESCE(_in_informacion_archivos, '')) = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v12] La informacion_archivos es obligatoria';
RETURN;
END IF;

IF COALESCE(_in_origen,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v12] El origen es obligatorio';
RETURN;
END IF;

IF COALESCE(_in_fecha_de_transaccion,null) = to_timestamp(0) THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v12] La fecha_de_transaccion es obligatoria';
RETURN;
END IF;

IF COALESCE(_in_responsable,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v12] El responsable es obligatorio';
RETURN;
END IF;

IF COALESCE(_in_descripcion,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v12] La descripcion es obligatoria';
RETURN;
END IF;

IF COALESCE(_in_mov_ref,-1) = -1 THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v12] El mov_ref es obligatorio y debe ser superior a 0';
RETURN;
END IF;

IF COALESCE(_in_tipo_movimiento,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v12] El tipo_movimiento es obligatorio';
RETURN;
END IF;

IF COALESCE(_in_sent_status,'') = '' THEN
_error_code := '101000';
_error_msg := '[mc_prp_crea_movimiento_investigar_v12] El sent_status es obligatorio';
RETURN;
END IF;

INSERT INTO ${schema}.prp_movimiento_investigar
(
  informacion_archivos,
  origen,
  fecha_registro,
  fecha_de_transaccion,
  responsable,
  descripcion,
  mov_ref,
  tipo_movimiento,
  sent_status
)
VALUES
(
  _in_informacion_archivos::json,
  COALESCE(_in_origen,''),
  timezone('utc', now()),
  COALESCE(_in_fecha_de_transaccion,to_timestamp(0)),
  COALESCE(_in_responsable,''),
  COALESCE(_in_descripcion,''),
  COALESCE(_in_mov_ref,0),
  COALESCE(_in_tipo_movimiento,''),
  COALESCE(_in_sent_status,'')
) RETURNING id INTO _r_id;

EXCEPTION WHEN OTHERS THEN

_error_code := SQLSTATE;
_error_msg := '[mc_prp_crea_movimiento_investigar_v12] Error al insertar movimiento a investigar. CAUSA ('|| SQLERRM ||')';
RETURN;

END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_crea_movimiento_investigar_v12(VARCHAR,VARCHAR,TIMESTAMP,VARCHAR,VARCHAR,NUMERIC,VARCHAR,VARCHAR);
