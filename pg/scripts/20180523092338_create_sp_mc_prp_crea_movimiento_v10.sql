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
  _id_movimiento_ref   NUMERIC,
  _id_usuario          NUMERIC,
  _tipo_movimiento     VARCHAR,
  _monto               NUMERIC,
  _moneda              VARCHAR,
  _estado              VARCHAR,
  _cod_entidad         VARCHAR,
  _cen_alta            VARCHAR,
  _cuenta              VARCHAR,
  _cod_moneda          NUMERIC,
  _ind_norcor          NUMERIC,
  _tipo_factura        NUMERIC,
  _fecha_factura       TIMESTAMP,
  _num_factura_ref     VARCHAR,
  _pan                 VARCHAR,
  _cod_mondiv          NUMERIC,
  _imp_div             NUMERIC,
  _imp_fac             NUMERIC,
  _cmp_apli            NUMERIC,
  _num_autorizacion    VARCHAR,
  _ind_proaje          VARCHAR,
  _cod_comercio        VARCHAR,
  _cod_actividad       VARCHAR,
  _imp_liq             NUMERIC,
  _cod_monliq          NUMERIC,
  _cod_pais            NUMERIC,
  _nom_poblacion       VARCHAR,
  _num_extracto        NUMERIC,
  _num_mov_extracto    NUMERIC,
  _clave_moneda        NUMERIC,
  _tipo_linea          VARCHAR,
  _referencia_linea    NUMERIC,
  _num_benef_cta       NUMERIC,
  _numero_plastico     NUMERIC,
  OUT _r_id            NUMERIC,
  OUT _error_code      VARCHAR,
  OUT _error_msg       VARCHAR
)AS $$
  DECLARE

  BEGIN
    _error_code := '0';
    _error_msg := '';

    INSERT INTO
        ${schema}.prp_movimiento(
          id_movimiento_ref,
          id_usuario,
          tipo_movimiento,
          monto,
          moneda,
          estado,
          fecha_creacion,
          fecha_actualizacion,
          cod_entidad,
          cen_alta,
          cuenta,
          cod_moneda,
          ind_norcor,
          tipo_factura,
          fecha_factura,
          num_factura_ref,
          pan,
          cod_mondiv,
          imp_div,
          imp_fac,
          cmp_apli,
          num_autorizacion,
          ind_proaje,
          cod_comercio,
          cod_actividad,
          imp_liq,
          cod_monliq,
          cod_pais,
          nom_poblacion,
          num_extracto,
          num_mov_extracto,
          clave_moneda,
          tipo_linea,
          referencia_linea,
          num_benef_cta,
          numero_plastico
        )
        VALUES(
          _id_movimiento_ref,
          _id_usuario,
          _tipo_movimiento,
          _monto,
          _moneda,
          _estado,
          timezone('utc', now()),
          timezone('utc', now()),
          _cod_entidad,
          _cen_alta,
          _cuenta,
          _cod_moneda,
          _ind_norcor,
          _tipo_factura,
          _fecha_factura,
          _num_factura_ref,
          _pan,
          _cod_mondiv,
          _imp_div,
          _imp_fac,
          _cmp_apli,
          _num_autorizacion,
          _ind_proaje,
          _cod_comercio,
          _cod_actividad,
          _imp_liq,
          _cod_monliq,
          _cod_pais,
          _nom_poblacion,
          _num_extracto,
          _num_mov_extracto,
          _clave_moneda,
          _tipo_linea,
          _referencia_linea,
          _num_benef_cta,
          _numero_plastico
        ) RETURNING id INTO _r_id;

  EXCEPTION
   WHEN OTHERS THEN
       _error_code := SQLSTATE;
       _error_msg := '[mc_prp_crea_movimiento_v10] Error al insertar movimiento. CAUSA ('|| SQLERRM ||')';
   RETURN;
  END;
$$ LANGUAGE plpgsql;
-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_crea_movimiento_v10(NUMERIC, NUMERIC, VARCHAR, NUMERIC, VARCHAR, VARCHAR, TIMESTAMP, VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC, NUMERIC, TIMESTAMP, VARCHAR, VARCHAR, NUMERIC, NUMERIC, NUMERIC, NUMERIC, VARCHAR, VARCHAR, VARCHAR, VARCHAR, NUMERIC, NUMERIC, NUMERIC, VARCHAR, NUMERIC, NUMERIC, NUMERIC, VARCHAR, NUMERIC, NUMERIC, NUMERIC);
