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

-- // create_sp_mc_prp_buscar_movimientos_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_buscar_movimientos_v10(
  IN _in_id                 BIGINT,
  IN _in_id_movimiento_ref  BIGINT,
  IN _in_id_usuario         BIGINT,
  IN _in_id_tx_externo      VARCHAR,
  IN _in_tipo_movimiento    VARCHAR,
  IN _in_estado             VARCHAR,
  IN _in_cuenta             VARCHAR,
  IN _in_clamon             NUMERIC,
  IN _in_indnorcor          NUMERIC,
  IN _in_tipofac            NUMERIC,
  OUT _id                 BIGINT,
  OUT _id_movimiento_ref  BIGINT,
  OUT _id_usuario         BIGINT,
  OUT _id_tx_externo      VARCHAR,
  OUT _tipo_movimiento    VARCHAR,
  OUT _monto              NUMERIC,
  OUT _estado             VARCHAR,
  OUT _fecha_creacion     TIMESTAMP,
  OUT _fecha_actualizacion TIMESTAMP,
  OUT _codent             VARCHAR,
  OUT _centalta           VARCHAR,
  OUT _cuenta             VARCHAR,
  OUT _clamon             NUMERIC,
  OUT _indnorcor          NUMERIC,
  OUT _tipofac            NUMERIC,
  OUT _fecfac             DATE,
  OUT _numreffac          VARCHAR,
  OUT _pan                VARCHAR,
  OUT _clamondiv          NUMERIC,
  OUT _impdiv             NUMERIC,
  OUT _impfac             NUMERIC,
  OUT _cmbapli            NUMERIC,
  OUT _numaut             VARCHAR,
  OUT _indproaje          VARCHAR,
  OUT _codcom             VARCHAR,
  OUT _codact             NUMERIC,
  OUT _impliq             NUMERIC,
  OUT _clamonliq          NUMERIC,
  OUT _codpais            NUMERIC,
  OUT _nompob             VARCHAR,
  OUT _numextcta          NUMERIC,
  OUT _nummovext          NUMERIC,
  OUT _clamone            NUMERIC,
  OUT _tipolin            VARCHAR,
  OUT _linref             NUMERIC,
  OUT _numbencta          NUMERIC,
  OUT _numplastico        NUMERIC
)
RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
  SELECT
    id,
    id_movimiento_ref,
    id_usuario,
    id_tx_externo,
    tipo_movimiento,
    monto,
    estado,
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
    (TRIM(COALESCE(_in_tipo_movimiento,'')) = '' OR tipo_movimiento = _in_tipo_movimiento) AND
    (TRIM(COALESCE(_in_estado,'')) = '' OR estado = _in_estado) AND
    (TRIM(COALESCE(_in_cuenta,'')) = '' OR cuenta = _in_cuenta) AND
    (COALESCE(_in_clamon, 0) = 0 OR clamon = _in_clamon) AND
    (COALESCE(_in_indnorcor, -1) = -1 OR indnorcor = _in_indnorcor) AND
    (COALESCE(_in_tipofac, 0) = 0 OR tipofac = _in_tipofac)
    ORDER BY id DESC;
   RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_movimientos_v10(BIGINT, BIGINT, BIGINT, VARCHAR, VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC,  NUMERIC);
