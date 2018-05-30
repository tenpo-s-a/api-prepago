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

-- // create_sp_mc_actualiza_tarjeta
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.mc_prp_actualiza_tarjeta_v10
(
 IN _filter_id_tarjeta      BIGINT,
 IN _filter_id_usuario      BIGINT,
 IN _filter_estado          VARCHAR,
 IN _in_pan                 VARCHAR,
 IN _in_pan_encriptado      VARCHAR,
 IN _in_contrato            VARCHAR,
 IN _in_expiracion          INTEGER,
 IN _in_estado              VARCHAR,
 IN _in_nombre_tarjeta      VARCHAR,
 OUT _error_code            VARCHAR,
 OUT _error_msg             VARCHAR
) AS $$
 DECLARE
 BEGIN
    _error_code := '0';
    _error_msg := '';


      IF COALESCE(_filter_id_tarjeta, 0) = 0 THEN
        _error_code := 'MC001';
        _error_msg := 'El _filter_id_tarjeta es obligatorio';
        RETURN;
      END IF;

      IF COALESCE(_filter_id_usuario, 0) = 0 THEN
        _error_code := 'MC002';
        _error_msg := 'El _filtro_id_usuario es obligatorio';
        RETURN;
      END IF;

      IF TRIM(COALESCE(_filter_estado, '')) = '' THEN
        _error_code := 'MC003';
        _error_msg := 'El _filtro_estado es obligatorio';
        RETURN;
      END IF;


     UPDATE
        ${schema}.prp_tarjeta
     SET
       pan = (
               CASE WHEN _in_pan IS NOT NULL THEN
                   _in_pan
                ELSE
                   pan
                END
              ),

       pan_encriptado = (
                          CASE WHEN _in_pan_encriptado IS NOT NULL THEN
                            _in_pan_encriptado
                           ELSE
                             pan_encriptado
                           END
                        ),
      contrato = (
                   CASE WHEN _in_contrato IS NOT NULL THEN
                    _in_contrato
                   ELSE
                     contrato
                   END
                ),

       expiracion = (
                      CASE WHEN _in_expiracion IS NOT NULL THEN
                        _in_expiracion
                      ELSE
                        expiracion
                      END
                    ),
       estado = (
                  CASE WHEN _in_estado IS NOT NULL THEN
                    _in_estado
                  ELSE
                    estado
                  END
                ),
       nombre_tarjeta = (
                          CASE WHEN _in_nombre_tarjeta IS NOT NULL THEN
                            _in_nombre_tarjeta
                          ELSE
                           nombre_tarjeta
                          END
                        ),
       fecha_actualizacion=timezone('utc', now())
    WHERE
      id = _filter_id_tarjeta AND
      id_usuario = _filter_id_usuario AND
      estado = _filter_estado;

    IF NOT FOUND THEN
       _error_code := 500;
       _error_msg := '[mc_prp_actualiza_tarjeta_v10] Registro no encontrado';
    end if;

   EXCEPTION
     WHEN OTHERS THEN
         _error_code := SQLSTATE;
         _error_msg := '[mc_prp_actualiza_tarjeta_v10] Error al actualizar tarjeta. CAUSA ('|| SQLERRM ||')';
     RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_actualiza_tarjeta_v10(BIGINT,BIGINT,VARCHAR, VARCHAR, VARCHAR, VARCHAR, INTEGER, VARCHAR, VARCHAR);
