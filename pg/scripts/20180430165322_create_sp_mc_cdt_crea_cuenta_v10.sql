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

-- // create_sp_mc_cdt_crea_cuenta
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.cdt}.mc_cdt_crea_cuenta_v10
(
    IN _id_externo         VARCHAR,
    IN _descripcion		     VARCHAR,
    OUT _id_cuenta         NUMERIC,
    OUT _num_error          VARCHAR,
    OUT _msj_error          VARCHAR
)AS $$
    DECLARE

    BEGIN

        _num_error := '0';
        _msj_error := '';
        _id_cuenta:= 0;

      IF TRIM(COALESCE(_id_externo, '')) = '' THEN
          _num_error := 'MC001';
          _msj_error := '[mc_cdt_crea_cuenta] El Id Externo de la cuenta no puede ser vacio';
          RETURN;
        END IF;

        INSERT INTO ${schema.cdt}.cdt_cuenta (
          id_externo,
          descripcion,
          estado,
          fecha_estado,
          fecha_creacion
        )
        VALUES (
           _id_externo,
           COALESCE(_descripcion,''),
           'ACTIVO',
           timezone('utc', now()),
           timezone('utc', now())
        );
      SELECT currval('${schema.cdt}.cdt_cuenta_id_seq') INTO _id_cuenta;
      EXCEPTION
          WHEN OTHERS THEN
              _num_error := SQLSTATE;
              _msj_error := '[mc_cdt_crea_cuenta] Error al Insertar Nueva Cuenta. CAUSA ('|| SQLERRM ||')';
          RETURN;
    END;
$$
LANGUAGE 'plpgsql';

-- //@UNDO
-- SQL to undo the change goes here.
  DROP FUNCTION IF EXISTS ${schema.cdt}.mc_cdt_crea_cuenta_v10(VARCHAR, VARCHAR);

