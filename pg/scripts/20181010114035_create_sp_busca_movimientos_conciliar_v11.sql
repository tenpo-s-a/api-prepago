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

-- // create_sp_busca_movimientos_conciliar
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_busca_movimientos_conciliar_v11
(
    OUT _id                   BIGINT,
    OUT _estado               VARCHAR,
    OUT _estado_de_negocio    VARCHAR,
    OUT _estado_con_switch    VARCHAR,
    OUT _estado_con_tecnocom  VARCHAR,
    OUT _tipo_movimiento      VARCHAR,
    OUT _indnorcor            NUMERIC,
    OUT _fecha_creacion       TIMESTAMP,
    OUT _tipofac              NUMERIC
)RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
    SELECT
      pm.id,
      pm.estado,
      pm.estado_de_negocio,
      pm.estado_con_switch,
      pm.estado_con_tecnocom,
      pm.tipo_movimiento,
      pm.indnorcor,
      pm.fecha_creacion,
      pm.tipofac
    FROM
     ${schema}.prp_movimiento pm
      LEFT JOIN ${schema}.prp_movimiento_conciliado pmc on pm.id = pmc.id_mov_ref
    WHERE
      pmc.id_mov_ref is null and -- que no este conciliado
      pm.estado_con_switch != 'PENDING' and
      pm.estado_con_tecnocom != 'PENDING' and
      pm.tipofac != 3003;  -- Se buscan todos menos los retiros web
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_busca_movimientos_conciliar_v10();


