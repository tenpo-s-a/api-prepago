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
VALUES(default, 'e8a41457-7ca6-4405-8574-b1be69289415', 1, '15036100805968524456', 'TECNOCOM_CL', '', 1557949725960, 'ACTIVE', '2019-05-15 19:48:45.448', '2019-05-15 19:48:45.450');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, '1562beb6-2a8c-428f-932e-20e0886c1a41', 2, '25587089980422180813', 'TECNOCOM_CL', '', 1557949743413, 'ACTIVE', '2019-05-15 19:49:03.019', '2019-05-15 19:49:03.019');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, '932b0c61-ac1f-42d8-bd36-10315ce59119', 3, '58634662941970259442', 'TECNOCOM_CL', '', 1557949752925, 'ACTIVE', '2019-05-15 19:49:12.563', '2019-05-15 19:49:12.563');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, '19fa426f-d8db-4908-9126-84581895cda3', 4, '92396607024888997168', 'TECNOCOM_CL', '', 1557949763480, 'ACTIVE', '2019-05-15 19:49:22.970', '2019-05-15 19:49:22.970');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, 'ce37e803-17a4-4679-a561-427cf6c2f898', 5, '38099090728604444861', 'TECNOCOM_CL', '', 1557949774390, 'ACTIVE', '2019-05-15 19:49:34.146', '2019-05-15 19:49:34.146');
INSERT INTO ${schema}.prp_cuenta
(id, uuid, id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion)
VALUES(default, 'b6519fa9-c4d2-440b-8fde-7aae75b1a843', 6, '45242394804312516876', 'TECNOCOM_CL', '', 1557949784756, 'ACTIVE', '2019-05-15 19:49:44.559', '2019-05-15 19:49:44.559');

--Tarjetas
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 1, '517608XXXXXX6488', 'qpAFaPVSG5Dxnmh1RJ4Vmsin6IcVSQOfk/DFI99st5I=', '15036100805968524456', 201812, 'ACTIVE', 'Mock Uno', '42', '94925578', '2019-05-15 19:48:45.483', '2019-05-15 19:48:45.610', '904757cb-0174-4fe2-87b6-461ccfea996b', '$2a$04$LVndZO/cMT5bv3Met0bWg.qhKhP4UlTGv2RLj1Vl/QZfKmfJUvKuO', 1);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 2, '517608XXXXXX9249', 'yb2rPssRTz0JAMWZLoQKlIleMOtLVgvJKjscOx8MsjI=', '25587089980422180813', 201812, 'ACTIVE', 'Mock Dos', '48', '51431969', '2019-05-15 19:49:03.051', '2019-05-15 19:49:03.104', '4d995079-5b39-4b99-856d-563567fa964c', '$2a$04$OjJv0VFeAqjDkvN2egeVPeFXxFhR.nFpBf8K0iSzd4aii9VLfXuJK', 2);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 3, '517608XXXXXX5697', 'Ne1YLTzeatY4GN7mfg4EAouh4BQf/u93Fl6cEa3Efq4=', '58634662941970259442', 201812, 'ACTIVE', 'Qatenpo appUno', '28', '28480387', '2019-05-15 19:49:12.584', '2019-05-15 19:49:12.635', '27fb27a7-72f6-47e6-9a2d-7d72d382f0fc', '$2a$04$g7Ay1FkKLJquHquTz6E6ZOteEtb/U6BT1fEMV..nh3JrvkNUUvN/W', 3);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 4, '517608XXXXXX6465', 'rVB5KEBWUO/TeOjdSqcdyDlzEIzjdV54d9q+CU1gHug=', '92396607024888997168', 201812, 'ACTIVE', 'Qatenpo appDos', '68', '10431401', '2019-05-15 19:49:22.994', '2019-05-15 19:49:23.100', 'de933914-1757-4ee6-9dc8-a0a7e756bb29', '$2a$04$uQ8I3evZHwN.5pNTHHoQoeFc661b00zTPsIGCiSQIoHbGvi/.1wPm', 4);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 5, '517608XXXXXX5428', '4bED2cqioGHp1ltVE8DopjvViBG5ItPp+ov+rlubaQ0=', '38099090728604444861', 201812, 'ACTIVE', 'Qatenpo appTres', '68', '69919639', '2019-05-15 19:49:34.175', '2019-05-15 19:49:34.210', 'dc049e35-a550-47c0-a46f-513b6fbd48b2', '$2a$04$XAmU5CBova3uv1KPv3VpxuQ0ULwVlxdaa6CxHifIznjE5fI8LyE76', 5);
INSERT INTO ${schema}.prp_tarjeta
(id, id_usuario, pan, pan_encriptado, contrato, expiracion, estado, nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, uuid, pan_hash, id_cuenta)
VALUES(default, 6, '517608XXXXXX5936', 'z9d/Pzx5YXJiXUBry+VMiBVF+Si2kkfLmmQ0q+ZC83A=', '45242394804312516876', 201812, 'ACTIVE', 'Qatenpo appCuatro', '15', '77010440', '2019-05-15 19:49:44.568', '2019-05-15 19:49:44.609', 'c75c1bf0-18b0-4378-ad56-83face8fa39e', '$2a$04$944KmIcrBoWNe5O95EHLGePLVMkZ9.vpHL9aqnQJSaw0OmsoOcaMK', 6);

--Movimientos
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 1, 1, '20190515154806', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:48:45.112', '2019-05-15 19:48:45.707', '0730', '100', '05968524456', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX6488', 0, 0, 50000, 0, '000001', 'A', '000000000000021', 6012, 0, 0, 152, '', 242, 3920115, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 1, 1, '20190515154806', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:48:45.112', '2019-05-15 19:48:45.916', '0730', '100', '05968524456', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX6488', 0, 0, 1, 0, '000002', 'A', '000000000000021', 6012, 0, 0, 152, '', 566, 7475302, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 3, 2, '20190515154984', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:02.863', '2019-05-15 19:49:03.160', '0730', '089', '80422180813', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX9249', 0, 0, 50000, 0, '000003', 'A', '000000000000021', 6012, 0, 0, 152, '', 217, 6538778, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 3, 2, '20190515154984', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:02.863', '2019-05-15 19:49:03.387', '0730', '089', '80422180813', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX9249', 0, 0, 1, 0, '000004', 'A', '000000000000021', 6012, 0, 0, 152, '', 594, 3398165, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 5, 3, '20190515154936', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:12.395', '2019-05-15 19:49:12.698', '0730', '662', '41970259442', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX5697', 0, 0, 50000, 0, '000005', 'A', '000000000000021', 6012, 0, 0, 152, '', 778, 6161131, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 5, 3, '20190515154936', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:12.395', '2019-05-15 19:49:12.883', '0730', '662', '41970259442', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX5697', 0, 0, 1, 0, '000006', 'A', '000000000000021', 6012, 0, 0, 152, '', 266, 1519783, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 7, 4, '20190515154978', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:22.810', '2019-05-15 19:49:23.171', '0730', '607', '24888997168', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX6465', 0, 0, 50000, 0, '000007', 'A', '000000000000021', 6012, 0, 0, 152, '', 873, 4713756, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 7, 4, '20190515154978', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:22.810', '2019-05-15 19:49:23.436', '0730', '607', '24888997168', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX6465', 0, 0, 1, 0, '000008', 'A', '000000000000021', 6012, 0, 0, 152, '', 146, 7129230, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 9, 5, '20190515154996', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:33.990', '2019-05-15 19:49:34.252', '0730', '090', '28604444861', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX5428', 0, 0, 50000, 0, '000009', 'A', '000000000000021', 6012, 0, 0, 152, '', 102, 3968515, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 9, 5, '20190515154996', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:33.990', '2019-05-15 19:49:34.368', '0730', '090', '28604444861', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX5428', 0, 0, 1, 0, '000010', 'A', '000000000000021', 6012, 0, 0, 152, '', 170, 6117802, 152, '', 0, 1, 0, 'null', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 11, 6, '20190515154943', 'TOPUP', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:44.466', '2019-05-15 19:49:44.657', '0730', '394', '04312516876', 152, 0, 3002, '2019-05-15', '', '517608XXXXXX5936', 0, 0, 50000, 0, '000011', 'A', '000000000000021', 6012, 0, 0, 152, '', 699, 3013030, 152, '', 0, 1, 0, 'Merchant Staging', 0);
INSERT INTO ${schema}.prp_movimiento
(id, id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, estado, estado_de_negocio, estado_con_switch, estado_con_tecnocom, origen_movimiento, fecha_creacion, fecha_actualizacion, codent, centalta, cuenta, clamon, indnorcor, tipofac, fecfac, numreffac, pan, clamondiv, impdiv, impfac, cmbapli, numaut, indproaje, codcom, codact, impliq, clamonliq, codpais, nompob, numextcta, nummovext, clamone, tipolin, linref, numbencta, numplastico, nomcomred, id_tarjeta)
VALUES(default, 11, 6, '20190515154943', 'ISSUANCE_FEE', 50000.00, 'PROCESS_OK', 'CONFIRMED', 'PENDING', 'PENDING', 'API', '2019-05-15 19:49:44.466', '2019-05-15 19:49:44.739', '0730', '394', '04312516876', 152, 0, 3000, '2019-05-15', '', '517608XXXXXX5936', 0, 0, 1, 0, '000012', 'A', '000000000000021', 6012, 0, 0, 152, '', 462, 8706434, 152, '', 0, 1, 0, 'null', 0);

--CDT cuentas
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_11111111-1', 'PREPAGO_11111111-1', 'ACTIVO', '2019-05-15 19:48:45.112', '2019-05-15 19:48:45.112');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_22222222-2', 'PREPAGO_22222222-2', 'ACTIVO', '2019-05-15 19:49:02.863', '2019-05-15 19:49:02.863');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_33333333-3', 'PREPAGO_33333333-3', 'ACTIVO', '2019-05-15 19:49:12.395', '2019-05-15 19:49:12.395');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_44444444-4', 'PREPAGO_44444444-4', 'ACTIVO', '2019-05-15 19:49:22.810', '2019-05-15 19:49:22.810');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_55555555-5', 'PREPAGO_55555555-5', 'ACTIVO', '2019-05-15 19:49:33.990', '2019-05-15 19:49:33.990');
INSERT INTO ${schema.cdt}.cdt_cuenta
(id, id_externo, descripcion, estado, fecha_estado, fecha_creacion)
VALUES(default, 'PREPAGO_66666666-6', 'PREPAGO_66666666-6', 'ACTIVO', '2019-05-15 19:49:44.466', '2019-05-15 19:49:44.466');

--CDT movimientos
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 1, 0, '20190515154806', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 19:48:45.112', 'PEND', '2019-05-15 19:48:45.112', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 1, 2, 1, '20190515154806', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 19:48:45.717', 'PEND', '2019-05-15 19:48:45.717', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 1, 0, '20190515154984', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 19:49:02.863', 'PEND', '2019-05-15 19:49:02.863', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 2, 2, 3, '20190515154984', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 19:49:03.170', 'PEND', '2019-05-15 19:49:03.170', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 3, 1, 0, '20190515154936', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 19:49:12.395', 'PEND', '2019-05-15 19:49:12.395', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 3, 2, 5, '20190515154936', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 19:49:12.707', 'PEND', '2019-05-15 19:49:12.707', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 4, 1, 0, '20190515154978', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 19:49:22.810', 'PEND', '2019-05-15 19:49:22.810', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 4, 2, 7, '20190515154978', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 19:49:23.178', 'PEND', '2019-05-15 19:49:23.178', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 5, 1, 0, '20190515154996', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 19:49:33.990', 'PEND', '2019-05-15 19:49:33.990', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 5, 2, 9, '20190515154996', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 19:49:34.257', 'PEND', '2019-05-15 19:49:34.257', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 6, 1, 0, '20190515154943', 'Solicitud Primera Carga 50000.00', 49762.00, '2019-05-15 19:49:44.466', 'PEND', '2019-05-15 19:49:44.466', '2019-05-15');
INSERT INTO ${schema.cdt}.cdt_movimiento_cuenta
(id, id_cuenta, id_fase_movimiento, id_mov_referencia, id_tx_externo, glosa, monto, fecha_registro, estado, fecha_estado, fecha_tx)
VALUES(default, 6, 2, 11, '20190515154943', 'Confirmación Primera Carga 49762.00', 49762.00, '2019-05-15 19:49:44.660', 'PEND', '2019-05-15 19:49:44.660', '2019-05-15');

--CDT acumuladores
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 1, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:48:45.112', '2019-05-15 19:48:45.112');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 1, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:48:45.112', '2019-05-15 19:48:45.112');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 1, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 19:48:45.112', '2019-05-15 19:48:45.717');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 2, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:02.863', '2019-05-15 19:49:02.863');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 2, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:02.863', '2019-05-15 19:49:02.863');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 2, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 19:49:02.863', '2019-05-15 19:49:03.170');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 3, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:12.395', '2019-05-15 19:49:12.395');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 3, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:12.395', '2019-05-15 19:49:12.395');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 3, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 19:49:12.395', '2019-05-15 19:49:12.707');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 4, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:22.810', '2019-05-15 19:49:22.810');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 4, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:22.810', '2019-05-15 19:49:22.810');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 4, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 19:49:22.810', '2019-05-15 19:49:23.178');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 5, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:33.990', '2019-05-15 19:49:33.990');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 5, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:33.990', '2019-05-15 19:49:33.990');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 5, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 19:49:33.990', '2019-05-15 19:49:34.257');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 3, 6, 'Acumulador Mensual Cargas', 'SUM', 49762.00, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:44.466', '2019-05-15 19:49:44.466');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 2, 6, 'Contador trasacciones primera carga', 'COUNT', 1, '2019-05-01 00:00:00.000', '2019-05-31 00:00:00.000', '2019-05-15 19:49:44.466', '2019-05-15 19:49:44.466');
INSERT INTO ${schema.cdt}.cdt_cuenta_acumulador
(id, id_regla_acumulacion, id_cuenta, descripcion, codigo_operacion, monto, fecha_inicio, fecha_fin, fecha_creacion, fecha_actualizacion)
VALUES(default, 9, 6, 'Saldo cuenta en CDT', 'SUM', 0.00, '1900-01-01 00:00:00.000', '2100-12-31 00:00:00.000', '2019-05-15 19:49:44.466', '2019-05-15 19:49:44.660');
