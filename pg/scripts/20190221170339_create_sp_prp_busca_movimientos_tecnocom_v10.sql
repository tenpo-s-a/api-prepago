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

-- // create_sp_prp_busca_movimientos_tecnocom_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.prp_busca_movimientos_tecnocom_v10
(
  IN  _in_fileId    BIGINT,
  IN  _in_originope VARCHAR,
  OUT _id           BIGINT,
  OUT _idarchivo    BIGINT,
  OUT _cuenta       VARCHAR,
  OUT _pan          VARCHAR,
  OUT _codent       VARCHAR,
  OUT _centalta     VARCHAR,
  OUT _clamon       NUMERIC,
  OUT _indnorcor    NUMERIC,
  OUT _tipofac      NUMERIC,
  OUT _fecfac       DATE,
  OUT _numreffac    VARCHAR,
  OUT _clamondiv    NUMERIC,
  OUT _impdiv       NUMERIC,
  OUT _impfac       NUMERIC,
  OUT _cmbapli      NUMERIC,
  OUT _numaut       VARCHAR,
  OUT _indproaje    VARCHAR,
  OUT _codcom       VARCHAR,
  OUT _codact       NUMERIC,
  OUT _impliq       NUMERIC,
  OUT _clamonliq    NUMERIC,
  OUT _codpais      NUMERIC,
  OUT _nompob       VARCHAR,
  OUT _numextcta    NUMERIC,
  OUT _nummovext    NUMERIC,
  OUT _clamone      NUMERIC,
  OUT _tipolin      VARCHAR,
  OUT _linref       NUMERIC,
  OUT _fectrn       TIMESTAMP,
  OUT _impautcon    NUMERIC,
  OUT _originope    VARCHAR,
  OUT _fecha_creacion TIMESTAMP,
  OUT _fecha_actualizacion TIMESTAMP,
  OUT _contrato     VARCHAR
)
RETURNS SETOF record
LANGUAGE plpgsql
AS $function$
BEGIN

  RETURN QUERY
    SELECT
      id,
      idarchivo,
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
    FROM
      ${schema}.prp_movimientos_tecnocom
    WHERE
      idarchivo = COALESCE(_in_fileId,0) AND
      originope = COALESCE(_in_originope,'')
    ORDER BY id ASC;

RETURN;
END;
$function$

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.prp_busca_movimientos_tecnocom_v10(BIGINT,_in_originope);

