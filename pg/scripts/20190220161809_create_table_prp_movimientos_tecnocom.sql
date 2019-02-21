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

-- // create_table_prp_movimientos_tecnocom
-- Migration SQL that makes the change goes here.

 CREATE TABLE ${schema}.prp_movimientos_tecnocom (
      id                  BIGSERIAL NOT NULL,
      idArchivo           BIGINT  REFERENCES ${schema}.prp_archivos_conciliacion(id),
      cuenta              VARCHAR(20) NOT NULL,
      pan                 VARCHAR(100) NOT NULL,
      codent              VARCHAR(4) NOT NULL,
      centalta            VARCHAR(4) NOT NULL,
      clamon              NUMERIC(3) NOT NULL,
      indnorcor           NUMERIC(1) NOT NULL,
      tipofac             NUMERIC(4) NOT NULL,
      fecfac              DATE NOT NULL,
      numreffac           VARCHAR(23) NOT NULL,
      clamondiv           NUMERIC(3) NOT NULL,
      impdiv              NUMERIC(17,2) NOT NULL,
      impfac              NUMERIC(17,2) NOT NULL,
      cmbapli             NUMERIC(9,2) NOT NULL,
      numaut              VARCHAR(6) NOT NULL,
      indproaje           VARCHAR(1) NOT NULL,
      codcom              VARCHAR(15) NOT NULL,
      codact              NUMERIC(4) NOT NULL,
      impliq              NUMERIC(17,2) NOT NULL,
      clamonliq           NUMERIC(3) NOT NULL,
      codpais             NUMERIC(3) NOT NULL,
      nompob              VARCHAR(26) NOT NULL,
      numextcta           NUMERIC(3) NOT NULL,
      nummovext           NUMERIC(7) NOT NULL,
      clamone             NUMERIC(3) NOT NULL,
      tipolin             VARCHAR(4) NOT NULL,
      linref              NUMERIC(8) NOT NULL,
      fecha_creacion      TIMESTAMP NOT NULL,
      fecha_actualizacion TIMESTAMP NOT NULL,
      CONSTRAINT prp_movimientos_tecnocom_pk PRIMARY KEY(id),
      CONSTRAINT prp_movimientos_tecnocom_u1 UNIQUE(cuenta,pan,fecfac,numaut)
  );
  CREATE INDEX prp_movimientos_tecnocom_i1 ON ${schema}.prp_movimientos_tecnocom (idArchivo);

 CREATE TABLE ${schema}.prp_movimientos_tecnocom_hist (
      id                  BIGSERIAL NOT NULL,
      idArchivo           BIGINT  REFERENCES ${schema}.prp_archivos_conciliacion(id),
      cuenta              VARCHAR(20) NOT NULL,
      pan                 VARCHAR(100) NOT NULL,
      codent              VARCHAR(4) NOT NULL,
      centalta            VARCHAR(4) NOT NULL,
      clamon              NUMERIC(3) NOT NULL,
      indnorcor           NUMERIC(1) NOT NULL,
      tipofac             NUMERIC(4) NOT NULL,
      fecfac              DATE NOT NULL,
      numreffac           VARCHAR(23) NOT NULL,
      clamondiv           NUMERIC(3) NOT NULL,
      impdiv              NUMERIC(17,2) NOT NULL,
      impfac              NUMERIC(17,2) NOT NULL,
      cmbapli             NUMERIC(9,2) NOT NULL,
      numaut              VARCHAR(6) NOT NULL,
      indproaje           VARCHAR(1) NOT NULL,
      codcom              VARCHAR(15) NOT NULL,
      codact              NUMERIC(4) NOT NULL,
      impliq              NUMERIC(17,2) NOT NULL,
      clamonliq           NUMERIC(3) NOT NULL,
      codpais             NUMERIC(3) NOT NULL,
      nompob              VARCHAR(26) NOT NULL,
      numextcta           NUMERIC(3) NOT NULL,
      nummovext           NUMERIC(7) NOT NULL,
      clamone             NUMERIC(3) NOT NULL,
      tipolin             VARCHAR(4) NOT NULL,
      linref              NUMERIC(8) NOT NULL,
      fecha_creacion      TIMESTAMP NOT NULL,
      fecha_actualizacion TIMESTAMP NOT NULL,
      CONSTRAINT prp_movimientos_tecnocom_hist_pk PRIMARY KEY(id),
      CONSTRAINT prp_movimientos_tecnocom_hist_u1 UNIQUE(cuenta,pan,fecfac,numaut)
  );
  CREATE INDEX prp_movimientos_tecnocom_hist_i1 ON ${schema}.prp_movimientos_tecnocom_hist (idArchivo);


-- //@UNDO
-- SQL to undo the change goes here.
  DROP TABLE IF EXISTS ${schema}.prp_movimientos_tecnocom_hist;

  DROP TABLE IF EXISTS ${schema}.prp_movimientos_tecnocom;
