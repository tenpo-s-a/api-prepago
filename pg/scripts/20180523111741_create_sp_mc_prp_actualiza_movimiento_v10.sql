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

-- // create_sp_mc_prp_actualiza_movimiento
-- Migration SQL that makes the change goes here.


CREATE OR REPLACE FUNCTION ${schema}.mc_prp_actualiza_movimiento_v10(
  IN _id               NUMERIC,
  IN _num_extracto     NUMERIC,
  IN _num_mov_extracto NUMERIC,
  IN _clave_moneda     NUMERIC,
  IN _estado           VARCHAR,
  OUT _error_code      VARCHAR,
  OUT _error_msg       VARCHAR
)AS $$
 DECLARE

 BEGIN
    _error_code := '0';
    _error_msg := '';

    IF COALESCE(_id, 0) = 0 THEN
      _error_code := 'MC001';
      _error_msg := '[mc_prp_actualiza_movimiento_v10] El Id es obligatorio';
      RETURN;
    END IF;

    IF TRIM(COALESCE(_estado, '')) = '' THEN
      _error_code := 'MC002';
      _error_msg := '[mc_prp_actualiza_movimiento_v10] El Estado es obligatorio';
      RETURN;
    END IF;

    UPDATE
      ${schema}.prp_movimiento
    SET
       estado = _estado,
       num_extracto =   (
                           CASE WHEN _num_extracto IS NOT NULL  THEN
                            _num_extracto
                           ELSE
                              num_extracto
                           END
                       ),
       num_mov_extracto = (
                            CASE WHEN _num_mov_extracto IS NOT NULL THEN
                              _num_mov_extracto
                             ELSE
                                num_mov_extracto
                             END
                          ),
       clave_moneda = (
                            CASE WHEN _clave_moneda IS NOT NULL THEN
                              _clave_moneda
                             ELSE
                                clave_moneda
                             END
                          ),
        fecha_actualizacion = timezone('utc', now())
     WHERE
        id = _id;

 EXCEPTION
   WHEN OTHERS THEN
       _error_code := SQLSTATE;
       _error_msg := '[mc_prp_actualiza_movimiento_v10] Error al actualizar estado de tarjeta. CAUSA ('|| SQLERRM ||')';
   RETURN;
END;
$$
LANGUAGE plpgsql;
-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_actualiza_movimiento_v10(NUMERIC,NUMERIC,NUMERIC,NUMERIC,VARCHAR);
