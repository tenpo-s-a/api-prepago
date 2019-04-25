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

-- // Bootstrap.sql

-- This is the only SQL script file that is NOT
-- a valid migration and will not be run or tracked
-- in the changelog.  There is no @UNDO section.

-- // Do I need this file?

-- New projects likely won't need this file.
-- Existing projects will likely need this file.
-- It's unlikely that this bootstrap should be run
-- in the production environment.

-- // Purpose

-- The purpose of this file is to provide a facility
-- to initialize the database to a state before MyBatis
-- SQL migrations were applied.  If you already have
-- a database in production, then you probably have
-- a script that you run on your developer machine
-- to initialize the database.  That script can now
-- be put in this bootstrap file (but does not have
-- to be if you are comfortable with your current process.

-- // Running

-- The bootstrap SQL is run with the "migrate bootstrap"
-- command.  It must be run manually, it's never run as
-- part of the regular migration process and will never
-- be undone. Variables (e.g. ${variable}) are still
-- parsed in the bootstrap SQL.

-- After the boostrap SQL has been run, you can then
-- use the migrations and the changelog for all future
-- database change management.

-- Valor usd
INSERT INTO ${schema}.prp_valor_usd
(id, nombre_archivo, fecha_creacion, fecha_termino, fecha_expiracion_usd, precio_venta, precio_compra, precio_medio, exponente, precio_dia)
VALUES(default, 'TEST.AR.T058.OK', '2018-11-06 15:27:55.969', '3000-12-31 00:00:00.000', '2018-11-07 15:27:55.969', 595.8102080, 595.8697920, 595.8400000, 0, 0.0000000);

-- Usuarios
INSERT INTO ${schema}.prp_usuario
(id, id_usuario_mc, rut, estado, saldo_info, saldo_expiracion, intentos_validacion, fecha_creacion, fecha_actualizacion, nombre, apellido, numero_documento, tipo_documento, nivel, uuid)
VALUES(default, 1, 11111111, 'ACTIVE', '', 0, 0, '2019-04-15 14:27:41.000', '2019-04-15 14:27:41.000', 'Mock', 'Uno', '11111111', 'RUT_CL', 'LEVEL_1', 'dee63b13-d3cc-44cf-a86b-f6ec37330a11');

--Cuentas
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, 'e8a41457-7ca6-4405-8574-b1be69289415', 1, '91772217819374871805', 'TECNOCOM_CL', '', 1556050877948, 'ACTIVE', '2019-04-23 20:21:17.014', '2019-04-23 20:21:17.014');

--Tarjetas
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 1, '517608XXXXXX6041', 'IYBj9oFho0PV9JvCvzrxpwtZu/zxrgRIpWpq0NRabKs=', '91772217819374871805', 201812, 'ACTIVE', 'Mock Uno', '59', '66009547', '2019-04-23 20:21:17.135', '2019-04-23 20:21:17.464', 'afd10397-d89e-4ac5-94fa-b1a9a602a23b', '$2a$04$ZslLV1Jy3C/PNbi6Nmmg1uYFXOxcIHTSbDwe1YhWjwxbJm2J60zfy', 1);

--Movimientos
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 1, 1, '1000000000001', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-04-23 20:21:15.956', '2019-04-23 20:21:17.608', '0730', '217', '19374871805', 152, 0, 3001, '2019-04-23', '', '517608XXXXXX6041', 0, 0, 50000, 0, '000001', 'A', '999999999999991', 6012, 0, 0, 152, '', 632, 7150178, 152, '', 0, 1, 0, 'Carnicería el tajo', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 1, 1, '1000000000001', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-04-23 20:21:15.956', '2019-04-23 20:21:17.916', '0730', '217', '19374871805', 152, 0, 3000, '2019-04-23', '', '517608XXXXXX6041', 0, 0, 1, 0, '000002', 'A', '999999999999991', 6012, 0, 0, 152, '', 603, 7084284, 152, '', 0, 1, 0, 'null', 0);

--CDT
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_11111111', 'PREPAGO_11111111', 'ACTIVO', '2019-04-23 20:21:15.956', '2019-04-23 20:21:15.956');

INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 1, 0, '1000000000001', 'Solicitud Primera Carga 50000.00', 50000.00, '2019-04-23 20:21:15.956', 'PEND', '2019-04-23 20:21:15.956', '2019-04-23');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 2, 1, '1000000000001', 'Confirmación Primera Carga 50000.00', 50000.00, '2019-04-23 20:21:17.618', 'PEND', '2019-04-23 20:21:17.618', '2019-04-23');

INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 1, 'Acumulador Mensual Cargas', 'SUM', 50000.00, '2019-04-01 00:00:00.000', '2019-04-30 00:00:00.000', '2019-04-23 20:21:15.956', '2019-04-23 20:21:15.956');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 1, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-04-01 00:00:00.000', '2019-04-30 00:00:00.000', '2019-04-23 20:21:15.956', '2019-04-23 20:21:15.956');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 1, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-04-23 20:21:15.956', '2019-04-23 20:21:17.618');
