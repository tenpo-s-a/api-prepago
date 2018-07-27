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

-- // create_sp_prp_actualiza_valor_usd_v10
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.mc_prp_actualiza_valor_usd_v10
(
  IN _nombre_archivo       VARCHAR,
  IN _fecha_creacion       TIMESTAMP,
  IN _fecha_termino        TIMESTAMP,
  IN _fecha_expiracion_usd TIMESTAMP,
  IN _precio_venta         NUMERIC,
  IN _precio_compra        NUMERIC,
  IN _precio_medio         NUMERIC,
  IN _exponente            NUMERIC,
  OUT _r_id                BIGINT,
  OUT _error_code          VARCHAR,
  OUT _error_msg           VARCHAR
) AS $$
 DECLARE
 BEGIN
    _error_code := '0';
    _error_msg := '';

    UPDATE
      ${schema}.prp_valor_usd
    SET
      fecha_termino = now()
    WHERE
      now() BETWEEN fecha_creacion AND fecha_termino;

    INSERT
      INTO ${schema}.prp_valor_usd (
        nombre_archivo,
        fecha_creacion,
        fecha_termino,
        fecha_expiracion_usd,
        precio_venta,
        precio_compra,
        precio_medio,
        exponente
      ) VALUES (
        _nombre_archivo,
        _fecha_creacion,
        _fecha_termino,
        _fecha_expiracion_usd,
        _precio_venta,
        _precio_compra,
        _precio_medio,
        _exponente
    ) RETURNING id INTO _r_id;

    IF NOT FOUND THEN
       _error_code := 500;
       _error_msg := '[mc_prp_actualiza_valor_usd_v10] Registro no encontrado';
    END if;

   EXCEPTION
   WHEN OTHERS THEN
       _error_code := SQLSTATE;
       _error_msg := '[mc_prp_actualiza_valor_usd_v10] Error al actualizar valor USD. CAUSA ('|| SQLERRM ||')';
   RETURN;

END;
$$ LANGUAGE plpgsql;


-- //@UNDO
-- SQL to undo the change goes here.
DROP FUNCTION IF EXISTS ${schema}.mc_prp_actualiza_valor_usd_v10(VARCHAR, TIMESTAMP, TIMESTAMP, TIMESTAMP, NUMERIC, NUMERIC, NUMERIC, NUMERIC);

