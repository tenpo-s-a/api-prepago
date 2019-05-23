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

-- // alter_table_card_drop_constrain
-- Migration SQL that makes the change goes here.

ALTER TABLE  ${schema}.prp_tarjeta
  DROP CONSTRAINT prp_tarjeta_id_usuario_fkey;

-- //@UNDO
-- SQL to undo the change goes here.

--ALTER TABLE prepago.prp_tarjeta
  --ADD CONSTRAINT prp_tarjeta_id_usuario_fkey FOREIGN KEY (id_usuario)
    --  REFERENCES prepago.prp_usuario (id) MATCH SIMPLE
      --ON UPDATE NO ACTION ON DELETE NO ACTION;
