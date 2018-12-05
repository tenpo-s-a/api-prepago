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

-- // create_sp_mc_prp_buscar_movimientos_conciliados_para_contabilidad_v10
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE FUNCTION ${schema}.mc_prp_buscar_movimientos_conciliados_para_contabilidad_v10(
  IN _in_fecha              VARCHAR,
  IN _in_status             VARCHAR,
  OUT _id                   BIGINT,
  OUT _id_movimiento_ref    BIGINT,
  OUT _id_usuario           BIGINT,
  OUT _id_tx_externo        VARCHAR,
  OUT _tipo_movimiento      VARCHAR,
  OUT _monto                NUMERIC,
  OUT _estado               VARCHAR,
  OUT _estado_de_negocio    VARCHAR,
  OUT _estado_con_switch    VARCHAR,
  OUT _estado_con_tecnocom  VARCHAR,
  OUT _origen_movimiento    VARCHAR,
  OUT _fecha_creacion       TIMESTAMP,
  OUT _fecha_actualizacion  TIMESTAMP,
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
    m.id,
    m.id_movimiento_ref,
    m.id_usuario,
    m.id_tx_externo,
    m.tipo_movimiento,
    m.monto,
    m.estado,
    m.estado_de_negocio,
    m.estado_con_switch,
    m.estado_con_tecnocom,
    m.origen_movimiento,
    m.fecha_creacion,
    m.fecha_actualizacion,
    m.codent,
    m.centalta,
    m.cuenta,
    m.clamon,
    m.indnorcor,
    m.tipofac,
    m.fecfac,
    m.numreffac,
    m.pan,
    m.clamondiv,
    m.impdiv,
    m.impfac,
    m.cmbapli,
    m.numaut,
    m.indproaje,
    m.codcom,
    m.codact,
    m.impliq,
    m.clamonliq,
    m.codpais,
    m.nompob,
    m.numextcta,
    m.nummovext,
    m.clamone,
    m.tipolin,
    m.linref,
    m.numbencta,
    m.numplastico
  FROM
    ${schema}.prp_movimiento m
    INNER JOIN ${schema}.prp_movimiento_conciliado mc
      ON m.id = mc.id_mov_ref
    LEFT JOIN ${schema.acc}.accounting acc
      ON mc.id_mov_ref = acc.id_tx
  WHERE
    acc.id_tx IS NULL AND
    mc.estado = _in_status AND
    mc.fecha_registro <= TO_TIMESTAMP(_in_fecha, 'YYYY-MM-DD HH24:MI:SS')
  ORDER BY id DESC;
   RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_buscar_movimientos_conciliados_para_contabilidad_v10(VARCHAR, VARCHAR);

