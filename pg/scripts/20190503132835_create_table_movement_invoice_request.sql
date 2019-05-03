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

-- // create table movement invoice request 
-- Migration SQL that makes the change goes here.

CREATE TABLE ${schema.acc}.movement_invoice_request (
    id BIGSERIAL NOT NULL,
    uuid VARCHAR(36) NOT NULL,
    movement_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT movement_invoice_request_pk PRIMARY KEY(id),
    CONSTRAINT movement_invoice_request_u1 UNIQUE(uuid),
    CONSTRAINT movement_invoice_request_u2 UNIQUE(movement_id)
);

CREATE INDEX movement_invoice_request_i1 
ON ${schema.acc}.movement_invoice_request(uuid);

CREATE INDEX movement_invoice_request_i2
ON ${schema.acc}.movement_invoice_request(movement_id);

-- //@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS ${schema.acc}.movement_invoice_request;

