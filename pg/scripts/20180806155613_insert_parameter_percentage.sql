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

-- // insert_parameter_percentage
-- Migration SQL that makes the change goes here.
INSERT INTO ${schema}.mc_parametro
(
   aplicacion,
   nombre,
   version,
   valor,
   expiracion,
   fecha_creacion
 )
VALUES
 ('api-prepaid',
  'calculator_percentage',
  'v10',
  '{"type":"Percentage10","value":"{\"topup_pos_fee_percentage\":0.5,\"topup_web_fee_percentage\":0,\"topup_web_fee_amount\":0,\"withdraw_pos_fee_percentage\":0.5,\"withdraw_web_fee_percentage\":0.5,\"withdraw_web_fee_amount\":81,\"calculator_topup_web_fee_amount\":0,\"calculator_topup_pos_fee_percentage\":0.5,\"calculator_withdraw_web_fee_amount\":81,\"calculator_withdraw_pos_fee_percentage\":0.5,\"opening_fee\":0,\"iva\":1.19,\"max_amount_by_user\":500000,\"subscription_purchase_fee_percentage\":1.01,\"clp_purchase_fee_amount\":35,\"clp_purchase_fee_percentage\":1.015,\"other_currency_purchase_fee_amount\":35,\"other_currency_purchase_exchange_rate_percentage\":1.015}"}',
   3600000,
   timezone('utc', now())
 );



-- //@UNDO
-- SQL to undo the change goes here.

DELETE FROM ${schema}.mc_parametro WHERE aplicacion = 'api-prepaid' AND nombre = 'calculator_percentage' AND version = 'v10';
