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

-- // create_sp_mc_prp_crea_movimiento
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.mc_prp_crea_movimiento_v10(
  _id_movimiento_ref    NUMERIC,
  _id_usuario           NUMERIC,
  _id_tx_externo        VARCHAR,
  _tipo_movimiento      VARCHAR,
  _monto                NUMERIC,
  _estado               VARCHAR,
  _estado_de_negocio    VARCHAR,
  _estado_con_switch    VARCHAR,
  _estado_con_tecnocom  VARCHAR,
  _origen_movimiento    VARCHAR,
  _codent               VARCHAR,
  _centalta             VARCHAR,
  _cuenta               VARCHAR,
  _clamon               NUMERIC,
  _indnorcor            NUMERIC,
  _tipofac              NUMERIC,
  _fecfac               DATE,
  _numreffac            VARCHAR,
  _pan                  VARCHAR,
  _clamondiv            NUMERIC,
  _impdiv               NUMERIC,
  _impfac               NUMERIC,
  _cmbapli              NUMERIC,
  _numaut               VARCHAR,
  _indproaje            VARCHAR,
  _codcom               VARCHAR,
  _codact               NUMERIC,
  _impliq               NUMERIC,
  _clamonliq            NUMERIC,
  _codpais              NUMERIC,
  _nompob               VARCHAR,
  _numextcta            NUMERIC,
  _nummovext            NUMERIC,
  _clamone              NUMERIC,
  _tipolin              VARCHAR,
  _linref               NUMERIC,
  _numbencta            NUMERIC,
  _numplastico          NUMERIC,
  OUT _r_id             NUMERIC,
  OUT _error_code       VARCHAR,
  OUT _error_msg        VARCHAR
)AS $$
  DECLARE

  BEGIN
    _error_code := '0';
    _error_msg := '';

    INSERT INTO
        ${schema}.prp_movimiento(
          id_movimiento_ref,
          id_usuario,
          id_tx_externo,
          tipo_movimiento,
          monto,
          estado,
          estado_de_negocio,
          estado_con_switch,
          estado_con_tecnocom,
          origen_movimiento,
          fecha_creacion,
          fecha_actualizacion,
          codent,
          centalta,
          cuenta,
          clamon,
          indnorcor,
          tipofac,
          fecfac,
          numreffac,
          pan,
          clamondiv,
          impdiv,
          impfac,
          cmbapli,
          numaut,
          indproaje,
          codcom,
          codact,
          impliq,
          clamonliq,
          codpais,
          nompob,
          numextcta,
          nummovext,
          clamone,
          tipolin,
          linref,
          numbencta,
          numplastico
        )
        VALUES(
          _id_movimiento_ref,
          _id_usuario,
          _id_tx_externo,
          _tipo_movimiento,
          _monto,
          _estado,
          _estado_de_negocio,
          _estado_con_switch,
          _estado_con_tecnocom,
          _origen_movimiento,
          timezone('utc', now()),
          timezone('utc', now()),
          _codent,
          _centalta,
          _cuenta,
          _clamon,
          _indnorcor,
          _tipofac,
          _fecfac,
          _numreffac,
          _pan,
          _clamondiv,
          _impdiv,
          _impfac,
          _cmbapli,
          _numaut,
          _indproaje,
          _codcom,
          _codact,
          _impliq,
          _clamonliq,
          _codpais,
          _nompob,
          _numextcta,
          _nummovext,
          _clamone,
          _tipolin,
          _linref,
          _numbencta,
          _numplastico
        ) RETURNING id INTO _r_id;

        UPDATE ${schema}.prp_movimiento
        SET  numaut = (
                   CASE WHEN (TRIM(COALESCE(_numaut,'')) = '' OR _numaut = '') THEN
                       lpad(_r_id::text, 6, '0')
                    ELSE
                       _numaut
                    END
                  )
        WHERE
          id = _r_id;

  EXCEPTION
   WHEN OTHERS THEN
       _error_code := SQLSTATE;
       _error_msg := '[mc_prp_crea_movimiento_v10] Error al insertar movimiento. CAUSA ('|| SQLERRM ||')';
   RETURN;
  END;
$$ LANGUAGE plpgsql;
-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_crea_movimiento_v10(NUMERIC, NUMERIC,VARCHAR, VARCHAR, NUMERIC, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC, NUMERIC, DATE, VARCHAR, VARCHAR, NUMERIC, NUMERIC, NUMERIC, NUMERIC, VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC, NUMERIC, NUMERIC, VARCHAR, NUMERIC, NUMERIC, NUMERIC, VARCHAR, NUMERIC, NUMERIC, NUMERIC);
