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

-- // sp_busca_movimiento_v11
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION prepago.mc_prp_buscar_movimientos_v11(
  IN _in_id                   BIGINT,
  IN _in_id_movimiento_ref    BIGINT,
  IN _in_id_usuario           BIGINT,
  IN _in_id_tx_externo        VARCHAR,
  IN _in_tipo_movimiento      VARCHAR,
  IN _in_estado               VARCHAR,
  IN _in_estado_con_switch    VARCHAR,
  IN _in_estado_con_tecnocom  VARCHAR,
  IN _in_origen_movimiento    VARCHAR,
  IN _in_cuenta               VARCHAR,
  IN _in_clamon               NUMERIC,
  IN _in_indnorcor            NUMERIC,
  IN _in_tipofac              NUMERIC,
  IN _in_fecfac               DATE,
  IN _in_numaut               VARCHAR,
  OUT cur_movimiento          refcursor,
  OUT numerror$               VARCHAR,
  OUT msjerror$               VARCHAR
) RETURNS record AS
$BODY$

BEGIN
  numerror$ := '0';
  msjerror$ := '';

  BEGIN
    OPEN cur_movimiento FOR
      SELECT
        id,
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
      FROM
        ${schema}.prp_movimiento
      WHERE
        (COALESCE(_in_id, 0) = 0 OR id = _in_id) AND
        (COALESCE(_in_id_movimiento_ref, 0) = 0 OR id_movimiento_ref = _in_id_movimiento_ref) AND
        (COALESCE(_in_id_usuario, 0) = 0 OR id_usuario = _in_id_usuario) AND
        (TRIM(COALESCE(_in_id_tx_externo,'')) = '' OR id_tx_externo = _in_id_tx_externo) AND
        (COALESCE(trim(_in_fecfac::text), '') = '' OR fecfac = _in_fecfac) AND
        (TRIM(COALESCE(_in_numaut,'')) = '' OR numaut = _in_numaut) AND
        (TRIM(COALESCE(_in_tipo_movimiento,'')) = '' OR tipo_movimiento = _in_tipo_movimiento) AND
        (TRIM(COALESCE(_in_estado,'')) = '' OR estado = _in_estado) AND
        (TRIM(COALESCE(_in_estado_con_switch,'')) = '' OR estado_con_switch = _in_estado_con_switch) AND
        (TRIM(COALESCE(_in_estado_con_tecnocom,'')) = '' OR estado_con_tecnocom = _in_estado_con_tecnocom) AND
        (TRIM(COALESCE(_in_origen_movimiento,'')) = '' OR origen_movimiento = _in_origen_movimiento) AND
        (TRIM(COALESCE(_in_cuenta,'')) = '' OR cuenta = _in_cuenta) AND
        (COALESCE(_in_clamon, 0) = 0 OR clamon = _in_clamon) AND
        (COALESCE(_in_indnorcor, -1) = -1 OR indnorcor = _in_indnorcor) AND
        (COALESCE(_in_tipofac, 0) = 0 OR tipofac = _in_tipofac)
        ORDER BY id DESC;
   EXCEPTION
      WHEN NO_DATA_FOUND THEN
        numerror$  := SQLSTATE;
        msjerror$ := '[mc_prp_buscar_movimientos_v10] Movimiento no encontrado (SQL): ' || SQLERRM;
        RETURN;
      WHEN OTHERS THEN
        numerror$ := SQLSTATE;
        msjerror$ := '[mc_prp_buscar_movimientos_v10] Error al buscar movimientos (SQL): ' || SQLERRM;
        RETURN;
  END;

EXCEPTION
  WHEN OTHERS THEN
    numerror$ := SQLSTATE;
    msjerror$ := '[mc_prp_buscar_movimientos_v10] Error al obtener movimientos (SQL): ' || SQLERRM;
    RETURN;
END;
$BODY$
LANGUAGE 'plpgsql';

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_movimientos_v10(BIGINT, BIGINT, BIGINT, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC,  NUMERIC, DATE, VARCHAR);
