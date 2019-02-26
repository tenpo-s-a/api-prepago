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

-- // create_sp_prp_crea_movimiento_tecnocom_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_crea_movimiento_tecnocom_v10
(
  IN	_idArchivo           BIGINT,
  IN	_cuenta              VARCHAR,
  IN	_pan                 VARCHAR,
  IN	_codent              VARCHAR,
  IN	_centalta            VARCHAR,
  IN	_clamon              NUMERIC,
  IN	_indnorcor           NUMERIC,
  IN	_tipofac             NUMERIC,
  IN	_fecfac              DATE,
  IN	_numreffac           VARCHAR,
  IN	_clamondiv           NUMERIC,
  IN	_impdiv              NUMERIC,
  IN	_impfac              NUMERIC,
  IN	_cmbapli             NUMERIC,
  IN	_numaut              VARCHAR,
  IN	_indproaje           VARCHAR,
  IN	_codcom              VARCHAR,
  IN	_codact              NUMERIC,
  IN	_impliq              NUMERIC,
  IN	_clamonliq           NUMERIC,
  IN	_codpais             NUMERIC,
  IN	_nompob              VARCHAR,
  IN	_numextcta           NUMERIC,
  IN	_nummovext           NUMERIC,
  IN	_clamone             NUMERIC,
  IN	_tipolin             VARCHAR,
  IN	_linref              NUMERIC,
  IN _fectrn               TIMESTAMP,
  IN _impautcon            NUMERIC,
  IN _originope            VARCHAR,
  IN _contrato             VARCHAR,
  OUT _r_id                BIGINT,
  OUT _error_code          VARCHAR,
  OUT _error_msg           VARCHAR
) AS $$
 DECLARE

 BEGIN

  _error_code := '0';
  _error_msg := '';

  IF COALESCE(_idArchivo, 0) = 0 THEN
    _error_code := 'MC001';
    _error_msg := 'El _idArchivo es obligatorio';
    RETURN;
  END IF;

  IF COALESCE(_cuenta, '') = '' THEN
    _error_code := 'MC002';
    _error_msg := 'El _cuenta es obligatorio';
    RETURN;
  END IF;

  IF COALESCE(_pan, '') = '' THEN
    _error_code := 'MC003';
    _error_msg := 'El _pan es obligatorio';
    RETURN;
  END IF;

 IF COALESCE(_tipofac, 0) = 0 THEN
    _error_code := 'MC004';
    _error_msg := 'El _tipofac es obligatorio';
    RETURN;
 END IF;

 IF COALESCE(_impfac, 0) = 0 THEN
    _error_code := 'MC005';
    _error_msg := 'El _impfac es obligatorio';
    RETURN;
 END IF;


 IF COALESCE(_numaut, '') = '' THEN
    _error_code := 'MC006';
    _error_msg := 'El _numaut es obligatorio';
    RETURN;
 END IF;


  INSERT INTO ${schema}.prp_movimientos_tecnocom
  (
    idArchivo,
    cuenta,
    pan,
    codent,
    centalta,
    clamon,
    indnorcor,
    tipofac,
    fecfac,
    numreffac,
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
    fectrn,
    impautcon,
    originope,
    fecha_creacion,
    fecha_actualizacion,
    contrato
  )
  VALUES
  (
    _idArchivo,
    _cuenta,
    _pan,
    _codent,
    _centalta,
    _clamon,
    _indnorcor,
    _tipofac,
    _fecfac,
    _numreffac,
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
    _fectrn,
    _impautcon,
    _originope,
    timezone('utc', now()),
    timezone('utc', now()),
    _contrato
  ) RETURNING id INTO _r_id;

 INSERT INTO ${schema}.prp_movimientos_tecnocom_hist
  (
    idArchivo,
    cuenta,
    pan,
    codent,
    centalta,
    clamon,
    indnorcor,
    tipofac,
    fecfac,
    numreffac,
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
    fectrn,
    impautcon,
    originope,
    fecha_creacion,
    fecha_actualizacion,
    contrato
  )
  VALUES
  (
    _idArchivo,
    _cuenta,
    _pan,
    _codent,
    _centalta,
    _clamon,
    _indnorcor,
    _tipofac,
    _fecfac,
    _numreffac,
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
    _fectrn,
    _impautcon,
    _originope,
    timezone('utc', now()),
    timezone('utc', now()),
    _contrato
  );

  EXCEPTION
  WHEN OTHERS THEN
    _error_code := SQLSTATE;
    _error_msg := '[mc_prp_crea_movimiento_tecnocom_v10] Error al guardar movimientos tecnocom. CAUSA ('|| SQLERRM ||')';
  RETURN;

END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_crea_movimiento_tecnocom_v10(BIGINT, VARCHAR, VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC, NUMERIC, DATE, VARCHAR, NUMERIC, NUMERIC, NUMERIC, NUMERIC, VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC, NUMERIC, NUMERIC, VARCHAR, NUMERIC, NUMERIC, NUMERIC, VARCHAR, NUMERIC,TIMESTAMP,NUMERIC,VARCHAR);
