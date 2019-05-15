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
(id, id_usuario_mc, rut, estado, saldo_info, saldo_expiracion, intentos_validacion, fecha_creacion, fecha_actualizacion, nombre, apellido, numero_documento, tipo_documento, nivel, uuid, plan)
VALUES(default, 1, 11111111, 'ACTIVE', '', 0, 0, '2019-04-15 14:27:41.000', '2019-04-15 14:27:41.000', 'Mock', 'Uno', '11111111-1', 'RUT_CL', 'LEVEL_2', 'dee63b13-d3cc-44cf-a86b-f6ec37330a11', 'FREE');
INSERT INTO ${schema}.prp_usuario
(id, id_usuario_mc, rut, estado, saldo_info, saldo_expiracion, intentos_validacion, fecha_creacion, fecha_actualizacion, nombre, apellido, numero_documento, tipo_documento, nivel, uuid, plan)
VALUES(default, 2, 22222222, 'ACTIVE', '', 0, 0, '2019-04-15 14:27:41.000', '2019-04-15 14:27:41.000', 'Mock', 'Dos', '22222222-2', 'RUT_CL', 'LEVEL_2', 'e6295417-91fd-4c6f-8774-48c2c53cd52a', 'FREE');
INSERT INTO ${schema}.prp_usuario
(id, id_usuario_mc, rut, estado, saldo_info, saldo_expiracion, intentos_validacion, fecha_creacion, fecha_actualizacion, nombre, apellido, numero_documento, tipo_documento, nivel, uuid, plan)
VALUES(default, 3, 33333333, 'ACTIVE', '', 0, 0, '2019-04-15 14:27:41.000', '2019-04-15 14:27:41.000', 'Qatenpo', 'appUno', '33333333-3', 'RUT_CL', 'LEVEL_2', '872e5f7b-ff80-4847-ad02-04a1696d856c', 'FREE');
INSERT INTO ${schema}.prp_usuario
(id, id_usuario_mc, rut, estado, saldo_info, saldo_expiracion, intentos_validacion, fecha_creacion, fecha_actualizacion, nombre, apellido, numero_documento, tipo_documento, nivel, uuid, plan)
VALUES(default, 4, 44444444, 'ACTIVE', '', 0, 0, '2019-04-15 14:27:41.000', '2019-04-15 14:27:41.000', 'Qatenpo', 'appDos', '44444444-4', 'RUT_CL', 'LEVEL_2', 'ae5891f3-817a-42bb-80d7-49e8c6a22b2b', 'FREE');
INSERT INTO ${schema}.prp_usuario
(id, id_usuario_mc, rut, estado, saldo_info, saldo_expiracion, intentos_validacion, fecha_creacion, fecha_actualizacion, nombre, apellido, numero_documento, tipo_documento, nivel, uuid, plan)
VALUES(default, 5, 55555555, 'ACTIVE', '', 0, 0, '2019-04-15 14:27:41.000', '2019-04-15 14:27:41.000', 'Qatenpo', 'appTres', '55555555-5', 'RUT_CL', 'LEVEL_1', '4a70b069-545a-4bc1-af16-a109028aad02', 'FREE');
INSERT INTO ${schema}.prp_usuario
(id, id_usuario_mc, rut, estado, saldo_info, saldo_expiracion, intentos_validacion, fecha_creacion, fecha_actualizacion, nombre, apellido, numero_documento, tipo_documento, nivel, uuid, plan)
VALUES(default, 6, 66666666, 'ACTIVE', '', 0, 0, '2019-04-15 14:27:41.000', '2019-04-15 14:27:41.000', 'Qatenpo', 'appCuatro', '66666666-6', 'RUT_CL', 'LEVEL_1', 'ecc88087-688c-4534-8257-3f9a81eb9391', 'FREE');
INSERT INTO ${schema}.prp_usuario
(id, id_usuario_mc, rut, estado, saldo_info, saldo_expiracion, intentos_validacion, fecha_creacion, fecha_actualizacion, nombre, apellido, numero_documento, tipo_documento, nivel, uuid, plan)
VALUES(default, 7, 77777777, 'ACTIVE', '', 0, 0, '2019-04-15 14:27:41.000', '2019-04-15 14:27:41.000', 'Qatenpo', 'appCinco', '77777777-7', 'RUT_CL', 'LEVEL_1', 'd818f70c-fbdf-4b4e-8baf-e39087818c06', 'FREE');

--Cuentas
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, 'e8a41457-7ca6-4405-8574-b1be69289415', 1, '41527848224522733755', 'TECNOCOM_CL', '', 1557271315501, 'ACTIVE', '2019-05-07 23:21:43.309', '2019-05-07 23:21:43.311');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, '1562beb6-2a8c-428f-932e-20e0886c1a41', 2, '23095870368653002904', 'TECNOCOM_CL', '', 1557271436416, 'ACTIVE', '2019-05-07 23:23:46.443', '2019-05-07 23:23:46.443');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, '932b0c61-ac1f-42d8-bd36-10315ce59119', 3, '46603156800778547156', 'TECNOCOM_CL', '', 1557839599602, 'ACTIVE', '2019-05-14 13:13:19.186', '2019-05-14 13:13:19.187');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, '19fa426f-d8db-4908-9126-84581895cda3', 4, '42533801789846068922', 'TECNOCOM_CL', '', 1557839651048, 'ACTIVE', '2019-05-14 13:14:10.751', '2019-05-14 13:14:10.751');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, 'ce37e803-17a4-4679-a561-427cf6c2f898', 5, '94354320601637160310', 'TECNOCOM_CL', '', 1557839693728, 'ACTIVE', '2019-05-14 13:14:53.440', '2019-05-14 13:14:53.441');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, 'b6519fa9-c4d2-440b-8fde-7aae75b1a843', 6, '64435038525650909722', 'TECNOCOM_CL', '', 1557839716745, 'ACTIVE', '2019-05-14 13:15:16.485', '2019-05-14 13:15:16.485');


--Tarjetas
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 1, '517608XXXXXX6871', 'L3XIDKMuscF6GeQNgbFfhj5ted4yP+2cx69BB7pII2I=', '41527848224522733755', 201812, 'ACTIVE', 'Mock Uno', '96', '19766417', '2019-05-07 23:21:43.362', '2019-05-07 23:21:43.575', '904757cb-0174-4fe2-87b6-461ccfea996b', '$2a$04$1LziVHngSaqtAv6WOiK9HuBplNj/cm1lv365dmVoxGlaVgiBHJ4qa', 1);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 2, '517608XXXXXX1572', 'X2Dg1drIkDPFjPbDQHi3H7oJTNTURnCmXfz75H0Ni1U=', '23095870368653002904', 201812, 'ACTIVE', 'Mock Dos', '02', '86121778', '2019-05-07 23:23:46.468', '2019-05-07 23:23:46.508', '4d995079-5b39-4b99-856d-563567fa964c', '$2a$04$uthO/LTeoKeODDXohrEk3OojZAsC79Wjw/69mNQkNvQYm8XUNOul2', 2);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 3, '517608XXXXXX2300', 'LoVkA0x8r8PwP5Qg/m57GuXbP/URxPRCiNypu7X1CBU=', '46603156800778547156', 201812, 'ACTIVE', 'Qatenpo appUno', '17', '63225029', '2019-05-14 13:13:19.241', '2019-05-14 13:13:19.342', '27fb27a7-72f6-47e6-9a2d-7d72d382f0fc', '$2a$04$jdyD7Q/149cLvpZwCKVH0.ZGuhQBpyBmXvlZbuCyOcogTx1hQ4Ana', 3);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 4, '517608XXXXXX3232', 'YYKGoO2/ddceUW3PamHSUs8NajJOkGsYeDEoipngC/A=', '42533801789846068922', 201812, 'ACTIVE', 'Qatenpo appDos', '64', '29762034', '2019-05-14 13:14:10.775', '2019-05-14 13:14:10.818', 'de933914-1757-4ee6-9dc8-a0a7e756bb29', '$2a$04$YRu4M/sRXeA6y9KXEw5jeOajZl550Vdy71MbSn6pnh1yx/OZYXGN6', 4);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 5, '517608XXXXXX6050', 'o4LeS3NmCiMhUPs3VkO9AOlorjYlUs7gqr6guQXnn50=', '94354320601637160310', 201812, 'ACTIVE', 'Qatenpo appTres', '67', '84484266', '2019-05-14 13:14:53.454', '2019-05-14 13:14:53.486', 'dc049e35-a550-47c0-a46f-513b6fbd48b2', '$2a$04$Jy94yqpncN9AtHVQ.SzTs.ejnznukdh3UnDOHQe1Qprw8qp7sGw7G', 5);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 6, '517608XXXXXX6046', 'btixjrS02VqBIF2cFtP8NYHmLkkZlP3jJjD95jOy/tA=', '64435038525650909722', 201812, 'ACTIVE', 'Qatenpo appCuatro', '95', '97036714', '2019-05-14 13:15:16.513', '2019-05-14 13:15:16.549', 'c75c1bf0-18b0-4378-ad56-83face8fa39e', '$2a$04$zQ/bgxzOeOhBviArl368jut3VyCOJAOCL2Q44Cvi97MvsENDimmhy', 6);


--Movimientos
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 1, 1, '1000000000001', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:21:42.675', '2019-05-07 23:21:43.635', '0730', '848', '24522733755', 152, 0, 3001, '2019-05-07', '', '517608XXXXXX6871', 0, 0, 50000, 0, '000001', 'A', '999999999999991', 6012, 0, 0, 152, '', 546, 7273831, 152, '', 0, 1, 0, 'Carnicería el mock', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 1, 1, '1000000000001', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:21:42.675', '2019-05-07 23:21:43.782', '0730', '848', '24522733755', 152, 0, 3000, '2019-05-07', '', '517608XXXXXX6871', 0, 0, 1, 0, '000002', 'A', '999999999999991', 6012, 0, 0, 152, '', 191, 1684113, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 3, 1, '1000000000002', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:21:49.104', '2019-05-07 23:21:49.104', '0730', '848', '24522733755', 152, 0, 3001, '2019-05-07', '', '517608XXXXXX6871', 0, 0, 50000, 0, '000003', 'A', '999999999999991', 6012, 0, 0, 152, '', 360, 2932223, 152, '', 0, 1, 0, 'Carnicería el mock', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 5, 1, '1000000000003', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:21:51.980', '2019-05-07 23:21:51.980', '0730', '848', '24522733755', 152, 0, 3001, '2019-05-07', '', '517608XXXXXX6871', 0, 0, 50000, 0, '000004', 'A', '999999999999991', 6012, 0, 0, 152, '', 985, 5386458, 152, '', 0, 1, 0, 'Carnicería el mock', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 7, 1, '1000000000004', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:21:55.427', '2019-05-07 23:21:55.427', '0730', '848', '24522733755', 152, 0, 3001, '2019-05-07', '', '517608XXXXXX6871', 0, 0, 50000, 0, '000005', 'A', '999999999999991', 6012, 0, 0, 152, '', 767, 2708737, 152, '', 0, 1, 0, 'Carnicería el mock', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 9, 2, '1000000000005', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:23:46.345', '2019-05-07 23:23:46.550', '0730', '870', '68653002904', 152, 0, 3001, '2019-05-07', '', '517608XXXXXX1572', 0, 0, 50000, 0, '000006', 'A', '999999999999991', 6012, 0, 0, 152, '', 724, 7249013, 152, '', 0, 1, 0, 'Carnicería el mock', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 9, 2, '1000000000005', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:23:46.345', '2019-05-07 23:23:46.644', '0730', '870', '68653002904', 152, 0, 3000, '2019-05-07', '', '517608XXXXXX1572', 0, 0, 1, 0, '000007', 'A', '999999999999991', 6012, 0, 0, 152, '', 901, 1286357, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 11, 2, '1000000000006', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:23:49.474', '2019-05-07 23:23:49.474', '0730', '870', '68653002904', 152, 0, 3001, '2019-05-07', '', '517608XXXXXX1572', 0, 0, 50000, 0, '000008', 'A', '999999999999991', 6012, 0, 0, 152, '', 346, 5355311, 152, '', 0, 1, 0, 'Carnicería el mock', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 13, 2, '1000000000007', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:23:53.075', '2019-05-07 23:23:53.075', '0730', '870', '68653002904', 152, 0, 3001, '2019-05-07', '', '517608XXXXXX1572', 0, 0, 50000, 0, '000009', 'A', '999999999999991', 6012, 0, 0, 152, '', 642, 9732361, 152, '', 0, 1, 0, 'Carnicería el mock', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 15, 2, '1000000000008', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-07 23:23:56.358', '2019-05-07 23:23:56.358', '0730', '870', '68653002904', 152, 0, 3001, '2019-05-07', '', '517608XXXXXX1572', 0, 0, 50000, 0, '000010', 'A', '999999999999991', 6012, 0, 0, 152, '', 816, 2701641, 152, '', 0, 1, 0, 'Carnicería el mock', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 17, 3, '1000000000009', 'TOPUP', 25000, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-14 13:13:18.455', '2019-05-14 13:13:19.407', '0730', '156', '00778547156', 152, 0, 3001, '2019-05-14', '', '517608XXXXXX2300', 0, 0, 25000, 0, '000011', 'A', '999999999999991', 6012, 0, 0, 152, '', 291, 9145001, 152, '', 0, 1, 0, 'Merchant Test', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 17, 3, '1000000000009', 'ISSUANCE_FEE', 25000, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-14 13:13:18.455', '2019-05-14 13:13:19.571', '0730', '156', '00778547156', 152, 0, 3000, '2019-05-14', '', '517608XXXXXX2300', 0, 0, 1, 0, '000012', 'A', '999999999999991', 6012, 0, 0, 152, '', 822, 5612611, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 19, 4, '1000000000010', 'TOPUP', 25000, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-14 13:14:10.615', '2019-05-14 13:14:10.858', '0730', '801', '89846068922', 152, 0, 3001, '2019-05-14', '', '517608XXXXXX3232', 0, 0, 25000, 0, '000013', 'A', '999999999999991', 6012, 0, 0, 152, '', 843, 3713540, 152, '', 0, 1, 0, 'Merchant Test', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 19, 4, '1000000000010', 'ISSUANCE_FEE', 25000, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-14 13:14:10.615', '2019-05-14 13:14:11.024', '0730', '801', '89846068922', 152, 0, 3000, '2019-05-14', '', '517608XXXXXX3232', 0, 0, 1, 0, '000014', 'A', '999999999999991', 6012, 0, 0, 152, '', 991, 5093703, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 21, 5, '1000000000011', 'TOPUP', 25000, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-14 13:14:53.333', '2019-05-14 13:14:53.525', '0730', '320', '01637160310', 152, 0, 3001, '2019-05-14', '', '517608XXXXXX6050', 0, 0, 25000, 0, '000015', 'A', '999999999999991', 6012, 0, 0, 152, '', 658, 1769062, 152, '', 0, 1, 0, 'Merchant Test', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 21, 5, '1000000000011', 'ISSUANCE_FEE', 25000, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-14 13:14:53.333', '2019-05-14 13:14:53.704', '0730', '320', '01637160310', 152, 0, 3000, '2019-05-14', '', '517608XXXXXX6050', 0, 0, 1, 0, '000016', 'A', '999999999999991', 6012, 0, 0, 152, '', 380, 6899798, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 23, 6, '1000000000012', 'TOPUP', 25000, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-14 13:15:16.361', '2019-05-14 13:15:16.603', '0730', '038', '25650909722', 152, 0, 3001, '2019-05-14', '', '517608XXXXXX6046', 0, 0, 25000, 0, '000017', 'A', '999999999999991', 6012, 0, 0, 152, '', 622, 7968352, 152, '', 0, 1, 0, 'Merchant Test', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 23, 6, '1000000000012', 'ISSUANCE_FEE', 25000, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-14 13:15:16.361', '2019-05-14 13:15:16.702', '0730', '038', '25650909722', 152, 0, 3000, '2019-05-14', '', '517608XXXXXX6046', 0, 0, 1, 0, '000018', 'A', '999999999999991', 6012, 0, 0, 152, '', 391, 1893945, 152, '', 0, 1, 0, 'null', 0);

--CDT cuentas
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_11111111', 'PREPAGO_11111111', 'ACTIVO', '2019-05-07 23:21:42.675', '2019-05-07 23:21:42.675');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_22222222', 'PREPAGO_22222222', 'ACTIVO', '2019-05-07 23:23:46.345', '2019-05-07 23:23:46.345');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_33333333-3', 'PREPAGO_33333333-3', 'ACTIVO', '2019-05-14 13:13:18.455', '2019-05-14 13:13:18.455');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_44444444-4', 'PREPAGO_44444444-4', 'ACTIVO', '2019-05-14 13:14:10.615', '2019-05-14 13:14:10.615');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_55555555-5', 'PREPAGO_55555555-5', 'ACTIVO', '2019-05-14 13:14:53.333', '2019-05-14 13:14:53.333');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_66666666-6', 'PREPAGO_66666666-6', 'ACTIVO', '2019-05-14 13:15:16.361', '2019-05-14 13:15:16.361');

--CDT movimientos
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 1, 0, '1000000000001', 'Solicitud Primera Carga 50000.00', 50000.00, '2019-05-07 23:21:42.675', 'PEND', '2019-05-07 23:21:42.675', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 2, 1, '1000000000001', 'Confirmación Primera Carga 50000.00', 50000.00, '2019-05-07 23:21:43.644', 'PEND', '2019-05-07 23:21:43.644', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 5, 0, '1000000000002', 'Solicitud Carga Web 50000.00', 50000.00, '2019-05-07 23:21:49.104', 'PEND', '2019-05-07 23:21:49.104', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 6, 3, '1000000000002', 'Confirmación Carga Web 50000.00', 50000.00, '2019-05-07 23:21:49.104', 'PEND', '2019-05-07 23:21:49.104', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 5, 0, '1000000000003', 'Solicitud Carga Web 50000.00', 50000.00, '2019-05-07 23:21:51.980', 'PEND', '2019-05-07 23:21:51.980', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 6, 5, '1000000000003', 'Confirmación Carga Web 50000.00', 50000.00, '2019-05-07 23:21:51.980', 'PEND', '2019-05-07 23:21:51.980', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 5, 0, '1000000000004', 'Solicitud Carga Web 50000.00', 50000.00, '2019-05-07 23:21:55.427', 'PEND', '2019-05-07 23:21:55.427', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 6, 7, '1000000000004', 'Confirmación Carga Web 50000.00', 50000.00, '2019-05-07 23:21:55.427', 'PEND', '2019-05-07 23:21:55.427', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 1, 0, '1000000000005', 'Solicitud Primera Carga 50000.00', 50000.00, '2019-05-07 23:23:46.345', 'PEND', '2019-05-07 23:23:46.345', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 2, 9, '1000000000005', 'Confirmación Primera Carga 50000.00', 50000.00, '2019-05-07 23:23:46.555', 'PEND', '2019-05-07 23:23:46.555', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 5, 0, '1000000000006', 'Solicitud Carga Web 50000.00', 50000.00, '2019-05-07 23:23:49.474', 'PEND', '2019-05-07 23:23:49.474', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 6, 11, '1000000000006', 'Confirmación Carga Web 50000.00', 50000.00, '2019-05-07 23:23:49.474', 'PEND', '2019-05-07 23:23:49.474', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 5, 0, '1000000000007', 'Solicitud Carga Web 50000.00', 50000.00, '2019-05-07 23:23:53.075', 'PEND', '2019-05-07 23:23:53.075', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 6, 13, '1000000000007', 'Confirmación Carga Web 50000.00', 50000.00, '2019-05-07 23:23:53.075', 'PEND', '2019-05-07 23:23:53.075', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 5, 0, '1000000000008', 'Solicitud Carga Web 50000.00', 50000.00, '2019-05-07 23:23:56.358', 'PEND', '2019-05-07 23:23:56.358', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 6, 15, '1000000000008', 'Confirmación Carga Web 50000.00', 50000.00, '2019-05-07 23:23:56.358', 'PEND', '2019-05-07 23:23:56.358', '2019-05-07');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 3, 1, 0, '1000000000009', 'Solicitud Primera Carga 25000', 25000, '2019-05-14 13:13:18.455', 'PEND', '2019-05-14 13:13:18.455', '2019-05-14');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 3, 2, 17, '1000000000009', 'Confirmación Primera Carga 25000', 25000, '2019-05-14 13:13:19.419', 'PEND', '2019-05-14 13:13:19.419', '2019-05-14');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 4, 1, 0, '1000000000010', 'Solicitud Primera Carga 25000', 25000, '2019-05-14 13:14:10.615', 'PEND', '2019-05-14 13:14:10.615', '2019-05-14');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 4, 2, 19, '1000000000010', 'Confirmación Primera Carga 25000', 25000, '2019-05-14 13:14:10.863', 'PEND', '2019-05-14 13:14:10.863', '2019-05-14');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 5, 1, 0, '1000000000011', 'Solicitud Primera Carga 25000', 25000, '2019-05-14 13:14:53.333', 'PEND', '2019-05-14 13:14:53.333', '2019-05-14');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 5, 2, 21, '1000000000011', 'Confirmación Primera Carga 25000', 25000, '2019-05-14 13:14:53.530', 'PEND', '2019-05-14 13:14:53.530', '2019-05-14');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 6, 1, 0, '1000000000012', 'Solicitud Primera Carga 25000', 25000, '2019-05-14 13:15:16.361', 'PEND', '2019-05-14 13:15:16.361', '2019-05-14');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 6, 2, 23, '1000000000012', 'Confirmación Primera Carga 25000', 25000, '2019-05-14 13:15:16.607', 'PEND', '2019-05-14 13:15:16.607', '2019-05-14');

--CDT acumuladores
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 1, 'Acumulador Mensual Cargas', 'SUM', 200000.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-07 23:21:42.675', '2019-05-07 23:21:55.427');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 1, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-07 23:21:42.675', '2019-05-07 23:21:42.675');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 1, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-07 23:21:42.675', '2019-05-07 23:21:55.427');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 4, 1, 'Contador transacciones Cargas', 'COUNT', 3, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-07 23:21:49.104', '2019-05-07 23:21:55.427');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 2, 'Acumulador Mensual Cargas', 'SUM', 200000.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-07 23:23:46.345', '2019-05-07 23:23:56.358');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 2, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-07 23:23:46.345', '2019-05-07 23:23:46.345');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 2, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-07 23:23:46.345', '2019-05-07 23:23:56.358');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 4, 2, 'Contador transacciones Cargas', 'COUNT', 3, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-07 23:23:49.474', '2019-05-07 23:23:56.358');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 3, 'Acumulador Mensual Cargas', 'SUM', 25000, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-14 13:13:18.455', '2019-05-14 13:13:18.455');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 3, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-14 13:13:18.455', '2019-05-14 13:13:18.455');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 3, 'Saldo cuenta en CDT', 'SUM', 0, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-14 13:13:18.455', '2019-05-14 13:13:19.419');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 4, 'Acumulador Mensual Cargas', 'SUM', 25000, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-14 13:14:10.615', '2019-05-14 13:14:10.615');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 4, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-14 13:14:10.615', '2019-05-14 13:14:10.615');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 4, 'Saldo cuenta en CDT', 'SUM', 0, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-14 13:14:10.615', '2019-05-14 13:14:10.863');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 5, 'Acumulador Mensual Cargas', 'SUM', 25000, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-14 13:14:53.333', '2019-05-14 13:14:53.333');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 5, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-14 13:14:53.333', '2019-05-14 13:14:53.333');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 5, 'Saldo cuenta en CDT', 'SUM', 0, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-14 13:14:53.333', '2019-05-14 13:14:53.530');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 6, 'Acumulador Mensual Cargas', 'SUM', 25000, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-14 13:15:16.361', '2019-05-14 13:15:16.361');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 6, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-14 13:15:16.361', '2019-05-14 13:15:16.361');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 6, 'Saldo cuenta en CDT', 'SUM', 0, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-14 13:15:16.361', '2019-05-14 13:15:16.607');

