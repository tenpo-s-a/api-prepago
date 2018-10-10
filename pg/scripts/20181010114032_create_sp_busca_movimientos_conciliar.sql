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

CREATE OR REPLACE FUNCTION ${schema}.mc_prp_busca_movimientos_conciliar_v10
(
    OUT _id                   BIGINT,
    OUT _estado               VARCHAR,
    OUT _estado_de_negocio    VARCHAR,
    OUT _estado_con_switch    VARCHAR,
    OUT _estado_con_tecnocom  VARCHAR
)RETURNS SETOF RECORD AS $$
BEGIN
  RETURN QUERY
    SELECT
      prp_movimiento.id,
      prp_movimiento.estado,
      prp_movimiento.estado_de_negocio,
      prp_movimiento.estado_con_switch,
      prp_movimiento.estado_con_tecnocom
    FROM
     ${schema}.prp_movimiento
    WHERE
      prp_movimiento.estado_con_switch != 'PENDING' and
      prp_movimiento.estado_con_tecnocom != 'PENDING' and
      prp_movimiento.id not in (select
                                  prp_movimiento_conciliado.id_mov_ref
                                from
                                  ${schema}.prp_movimiento_conciliado
                               );
  RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema}.mc_prp_busca_movimientos_conciliar_v10();


