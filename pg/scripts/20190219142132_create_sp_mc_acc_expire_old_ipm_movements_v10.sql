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

-- // create_sp_mc_acc_expire_old_ipm_movements_v10
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.acc}.create_sp_mc_acc_expire_old_ipm_movements_v10()
RETURNS VOID
AS $$
BEGIN

WITH updatedMovements AS (
  UPDATE
    ${schema}.prp_movimiento mov
  SET
    estado = 'EXPIRED'
  WHERE
    mov.estado = 'PENDING' AND
    (mov.tipo_movimiento = 'SUSCRIPTION' OR mov.tipo_movimiento = 'PURCHASE') AND
    (SELECT COUNT(f.id)
     FROM ${schema.acc}.ipm_file f
     WHERE f.create_date >= mov.fecha_creacion AND f.status = 'PROCESSED') >= 7
  RETURNING
    mov.id
),
updatedAccountings AS (
  UPDATE
    ${schema.acc}.accounting a
  SET
    accounting_status = 'NOT_OK',
    status = CASE WHEN status = 'PENDING' THEN 'NOT_SEND' ELSE status END,
    conciliation_date = timezone('utc', now())
  FROM
    updatedMovements
  WHERE
    a.id_tx = updatedMovements.id
  RETURNING
    a.id
)
UPDATE
  ${schema.acc}.clearing cle
SET
  status = 'NO_CONFIRMADA'
FROM
  updatedAccountings
WHERE
  cle.accounting_id = updatedAccountings.id;

RETURN;
END;
$$ LANGUAGE plpgsql;

-- //@UNDO
-- SQL to undo the change goes here.

DROP FUNCTION IF EXISTS ${schema.acc}.create_sp_mc_acc_expire_old_ipm_movements_v10();
