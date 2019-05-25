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
INSERT INTO prepago.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, 'e8a41457-7ca6-4405-8574-b1be69289415', 1, '31696650067718801932', 'TECNOCOM_CL', '', 1557956037587, 'ACTIVE', '2019-05-15 21:33:57.094', '2019-05-15 21:33:57.095');
INSERT INTO prepago.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, '1562beb6-2a8c-428f-932e-20e0886c1a41', 2, '12350955789273432406', 'TECNOCOM_CL', '', 1557956050270, 'ACTIVE', '2019-05-15 21:34:10.004', '2019-05-15 21:34:10.004');
INSERT INTO prepago.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, '932b0c61-ac1f-42d8-bd36-10315ce59119', 3, '38768438676179412173', 'TECNOCOM_CL', '', 1557956063078, 'ACTIVE', '2019-05-15 21:34:22.844', '2019-05-15 21:34:22.844');
INSERT INTO prepago.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, '19fa426f-d8db-4908-9126-84581895cda3', 4, '49036656233104205589', 'TECNOCOM_CL', '', 1557956071057, 'ACTIVE', '2019-05-15 21:34:30.850', '2019-05-15 21:34:30.850');
INSERT INTO prepago.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, 'ce37e803-17a4-4679-a561-427cf6c2f898', 5, '82245258508757851362', 'TECNOCOM_CL', '', 1557956080905, 'ACTIVE', '2019-05-15 21:34:40.677', '2019-05-15 21:34:40.677');
INSERT INTO prepago.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, 'b6519fa9-c4d2-440b-8fde-7aae75b1a843', 6, '12850163774058519283', 'TECNOCOM_CL', '', 1557956089026, 'ACTIVE', '2019-05-15 21:34:48.795', '2019-05-15 21:34:48.795');

--Tarjetas
INSERT INTO prepago.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 1, '517608XXXXXX1992', 'Frk2WoBGOmRLK7DuPIcfdZdIYEsJiAUs8raaiwBjr6I=', '31696650067718801932', 201812, 'ACTIVE', 'Mock Uno', '44', '24359176', '2019-05-15 21:33:57.169', '2019-05-15 21:33:57.296', '0914aa70-d922-42d3-8aab-55607907c49f', '$2a$04$mnSSaz4jw4cd6KGHL89ayuLOeEEmCKUnDQ.27Uy8d6l02bqjGTvQ6', 1);
INSERT INTO prepago.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 2, '517608XXXXXX4622', 'gvkrGgPQAaHi2QXxAfOKgNirraonMPwutDo2tx/k7VQ=', '12350955789273432406', 201812, 'ACTIVE', 'Mock Dos', '94', '42240729', '2019-05-15 21:34:10.031', '2019-05-15 21:34:10.079', 'b337b963-4829-4c05-a68a-ad70a2970834', '$2a$04$is4GczbpO1mpf4MoD8Fc4..c.571L2Djc9RIyIRpapqfAyfdkvFvW', 2);
INSERT INTO prepago.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 3, '517608XXXXXX3714', 'Ct5dwZKH+3uzAiPmp3F4ZK7nLWJX9oM1Ho8pusQw2K8=', '38768438676179412173', 201812, 'ACTIVE', 'Qatenpo appUno', '80', '87541370', '2019-05-15 21:34:22.872', '2019-05-15 21:34:22.910', '9905f054-9795-43d8-aa3b-c5ac06deaefb', '$2a$04$yw2dYzS1.5v3tg0TAa3wO.Iko3Edb6.7j9gJU0uJM7WJKEA9fdSWe', 3);
INSERT INTO prepago.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 4, '517608XXXXXX1604', '9RlCstNLDdRj/OB/4/NPyPD7nXAw153g4jTH2zZ7ess=', '49036656233104205589', 201812, 'ACTIVE', 'Qatenpo appDos', '39', '31242289', '2019-05-15 21:34:30.871', '2019-05-15 21:34:30.905', 'a599becd-9f58-4997-8ed7-c853e4c41807', '$2a$04$YbgDH43.ttxZNCHsF2eVhOnituI70Qt5Pgn9bhiOGMfYoVP7UloiC', 4);
INSERT INTO prepago.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 5, '517608XXXXXX4250', '+cqLdnd439YzzgdhIVhRU4m2J986Sf5VFmXuaUb5Woo=', '82245258508757851362', 201812, 'ACTIVE', 'Qatenpo appTres', '61', '16476385', '2019-05-15 21:34:40.705', '2019-05-15 21:34:40.735', '02427da5-97ea-4e92-a4ed-88486f5a68fc', '$2a$04$JQKaRQ/YyD8gH6MS9j6IB.NpW4D6nZWU0NO0WJG2PiuEdhFMjpBOG', 5);
INSERT INTO prepago.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 6, '517608XXXXXX8877', '1Msh0rc6X6TXrsHnHWVy0XTcQa4AifNMnkiCwz7w2ZI=', '12850163774058519283', 201812, 'ACTIVE', 'Qatenpo appCuatro', '64', '97238458', '2019-05-15 21:34:48.814', '2019-05-15 21:34:48.845', '8b633475-0a7a-4d4c-b50d-75f853e0c184', '$2a$04$LVXVBbkS4il.KyhIp7stlOpt0jz.xczJGq7e5mvXhF.aBrs6XG/mG', 6);

--Movimientos
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 1, 1, '20190515173362', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:33:56.056', '2019-05-15 21:33:57.373', '0730', '650', '67718801932', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX1992', 0, 0, 50000, 0, '000001', 'A', '000000000000021', 6012, 0, 0, 152, '', 974, 1612428, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 1, 1, '20190515173362', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:33:56.056', '2019-05-15 21:33:57.543', '0730', '650', '67718801932', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX1992', 0, 0, 1, 0, '000002', 'A', '000000000000021', 6012, 0, 0, 152, '', 328, 6649684, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 3, 2, '20190515173476', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:09.797', '2019-05-15 21:34:10.113', '0730', '955', '89273432406', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX4622', 0, 0, 50000, 0, '000003', 'A', '000000000000021', 6012, 0, 0, 152, '', 471, 4483571, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 3, 2, '20190515173476', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:09.797', '2019-05-15 21:34:10.252', '0730', '955', '89273432406', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX4622', 0, 0, 1, 0, '000004', 'A', '000000000000021', 6012, 0, 0, 152, '', 413, 8412029, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 5, 3, '20190515173469', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:22.713', '2019-05-15 21:34:22.944', '0730', '438', '76179412173', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX3714', 0, 0, 50000, 0, '000005', 'A', '000000000000021', 6012, 0, 0, 152, '', 966, 3538943, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 5, 3, '20190515173469', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:22.713', '2019-05-15 21:34:23.057', '0730', '438', '76179412173', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX3714', 0, 0, 1, 0, '000006', 'A', '000000000000021', 6012, 0, 0, 152, '', 500, 9509758, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 7, 4, '20190515173470', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:30.727', '2019-05-15 21:34:30.934', '0730', '656', '33104205589', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX1604', 0, 0, 50000, 0, '000007', 'A', '000000000000021', 6012, 0, 0, 152, '', 180, 6316943, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 7, 4, '20190515173470', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:30.727', '2019-05-15 21:34:31.035', '0730', '656', '33104205589', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX1604', 0, 0, 1, 0, '000008', 'A', '000000000000021', 6012, 0, 0, 152, '', 164, 8481863, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 9, 5, '20190515173455', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:40.572', '2019-05-15 21:34:40.786', '0730', '258', '08757851362', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX4250', 0, 0, 50000, 0, '000009', 'A', '000000000000021', 6012, 0, 0, 152, '', 693, 9716497, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 9, 5, '20190515173455', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:40.572', '2019-05-15 21:34:40.878', '0730', '258', '08757851362', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX4250', 0, 0, 1, 0, '000010', 'A', '000000000000021', 6012, 0, 0, 152, '', 193, 1852358, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 11, 6, '20190515173463', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:48.660', '2019-05-15 21:34:48.912', '0730', '163', '74058519283', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX8877', 0, 0, 50000, 0, '000011', 'A', '000000000000021', 6012, 0, 0, 152, '', 683, 2440884, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO prepago.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 11, 6, '20190515173463', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 21:34:48.660', '2019-05-15 21:34:49.010', '0730', '163', '74058519283', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX8877', 0, 0, 1, 0, '000012', 'A', '000000000000021', 6012, 0, 0, 152, '', 378, 8056432, 152, '', 0, 1, 0, 'null', 0);

--CDT cuentas
INSERT INTO ctatraspaso.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_11111111-1', 'PREPAGO_11111111-1', 'ACTIVO', '2019-05-15 21:33:56.056', '2019-05-15 21:33:56.056');
INSERT INTO ctatraspaso.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_22222222-2', 'PREPAGO_22222222-2', 'ACTIVO', '2019-05-15 21:34:09.797', '2019-05-15 21:34:09.797');
INSERT INTO ctatraspaso.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_33333333-3', 'PREPAGO_33333333-3', 'ACTIVO', '2019-05-15 21:34:22.713', '2019-05-15 21:34:22.713');
INSERT INTO ctatraspaso.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_44444444-4', 'PREPAGO_44444444-4', 'ACTIVO', '2019-05-15 21:34:30.727', '2019-05-15 21:34:30.727');
INSERT INTO ctatraspaso.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_55555555-5', 'PREPAGO_55555555-5', 'ACTIVO', '2019-05-15 21:34:40.572', '2019-05-15 21:34:40.572');
INSERT INTO ctatraspaso.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_66666666-6', 'PREPAGO_66666666-6', 'ACTIVO', '2019-05-15 21:34:48.660', '2019-05-15 21:34:48.660');

--CDT movimientos
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 1, 0, '20190515173362', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 21:33:56.056', 'PEND', '2019-05-15 21:33:56.056', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 2, 1, '20190515173362', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 21:33:57.390', 'PEND', '2019-05-15 21:33:57.390', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 1, 0, '20190515173476', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 21:34:09.797', 'PEND', '2019-05-15 21:34:09.797', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 2, 3, '20190515173476', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 21:34:10.117', 'PEND', '2019-05-15 21:34:10.117', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 3, 1, 0, '20190515173469', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 21:34:22.713', 'PEND', '2019-05-15 21:34:22.713', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 3, 2, 5, '20190515173469', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 21:34:22.949', 'PEND', '2019-05-15 21:34:22.949', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 4, 1, 0, '20190515173470', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 21:34:30.727', 'PEND', '2019-05-15 21:34:30.727', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 4, 2, 7, '20190515173470', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 21:34:30.938', 'PEND', '2019-05-15 21:34:30.938', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 5, 1, 0, '20190515173455', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 21:34:40.572', 'PEND', '2019-05-15 21:34:40.572', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 5, 2, 9, '20190515173455', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 21:34:40.790', 'PEND', '2019-05-15 21:34:40.790', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 6, 1, 0, '20190515173463', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 21:34:48.660', 'PEND', '2019-05-15 21:34:48.660', '2019-05-15');
INSERT INTO ctatraspaso.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 6, 2, 11, '20190515173463', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 21:34:48.928', 'PEND', '2019-05-15 21:34:48.928', '2019-05-15');

--CDT acumuladores
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 1, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:33:56.056', '2019-05-15 21:33:56.056');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 1, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:33:56.056', '2019-05-15 21:33:56.056');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 1, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 21:33:56.056', '2019-05-15 21:33:57.390');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 2, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:09.797', '2019-05-15 21:34:09.797');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 2, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:09.797', '2019-05-15 21:34:09.797');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 2, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 21:34:09.797', '2019-05-15 21:34:10.117');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 3, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:22.713', '2019-05-15 21:34:22.713');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 3, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:22.713', '2019-05-15 21:34:22.713');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 3, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 21:34:22.713', '2019-05-15 21:34:22.949');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 4, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:30.727', '2019-05-15 21:34:30.727');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 4, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:30.727', '2019-05-15 21:34:30.727');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 4, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 21:34:30.727', '2019-05-15 21:34:30.938');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 5, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:40.572', '2019-05-15 21:34:40.572');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 5, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:40.572', '2019-05-15 21:34:40.572');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 5, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 21:34:40.572', '2019-05-15 21:34:40.790');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 6, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:48.660', '2019-05-15 21:34:48.660');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 6, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 21:34:48.660', '2019-05-15 21:34:48.660');
INSERT INTO ctatraspaso.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 6, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 21:34:48.660', '2019-05-15 21:34:48.928');
