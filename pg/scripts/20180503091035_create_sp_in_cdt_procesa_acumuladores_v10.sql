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

-- // create_sp_mc_cdt_crea_cuenta_acumulacion
-- Migration SQL that makes the change goes here.

CREATE OR REPLACE FUNCTION ${schema.cdt}.in_cdt_procesa_acumuladores_v10
(
    IN _id_fase_movimiento      NUMERIC,
    IN _id_cuenta               NUMERIC,
    IN _monto                   NUMERIC,
    OUT _num_error              VARCHAR,
    OUT _msj_error               VARCHAR
)AS $$
        DECLARE
            _rec_regla_acum RECORD;
            _current_date DATE;
            _fecha_ini TIMESTAMP;
            _fecha_fin TIMESTAMP;
        BEGIN

            _num_error := '0';
            _msj_error := '';
            _current_date:= current_date;


            IF COALESCE(_id_cuenta, 0) = 0 THEN
                _num_error := 'MC001';
                _msj_error := '[in_cdt_procesa_acumuladores] El Id Cuenta no puede ser 0';
                RETURN;
            END IF;

            IF COALESCE(_id_fase_movimiento, 0) = 0 THEN
                _num_error := 'MC002';
                _msj_error := '[in_cdt_procesa_acumuladores] El Id Movimiento no puede ser 0';
                RETURN;
            END IF;

            IF COALESCE(_monto, 0) = 0 THEN
                _num_error := 'MC003';
                _msj_error := '[in_cdt_procesa_acumuladores] El Monto no puede ser 0';
                RETURN;
            END IF;


                FOR _rec_regla_acum IN
                  select
                    RAC.id as id,
                    FAC.signo as signo,
                    RAC.descripcion as descripcion,
                    RAC.periocidad as periocidad,
                    RAC.codigo_operacion as codigo_operacion
                  from
                    ${schema.cdt}.cdt_fase_acumulador	FAC
                  INNER JOIN ${schema.cdt}.cdt_regla_acumulacion  RAC ON RAC.id = FAC.id_regla_acumulacion
                  WHERE
                    FAC.id_fase_movimiento = _id_fase_movimiento AND
                    RAC.estado = 'ACTIVO'
                LOOP
                    BEGIN
                        IF (_rec_regla_acum.periocidad != 'VIDA') THEN
                            CASE
                                WHEN _rec_regla_acum.periocidad = 'DIA' THEN -- CALCULA FECHA INICIO Y FIN PARA EL DIA.
                                    _fecha_ini = _current_date;
                                    _fecha_fin = _current_date;
                                WHEN _rec_regla_acum.periocidad = 'SEM' THEN -- CALCULA LA FECHA DE INICIO Y FIN DE UNA SEMANA.
                                    _fecha_ini = date_trunc('week', _current_date)::date;
                                    _fecha_fin = (date_trunc('week', _current_date)+ '6 days'::interval)::date;
                                WHEN _rec_regla_acum.periocidad = 'MEN' THEN -- CALCULA LA FECHA DE INICIO Y FIN DE UN MES.
                                    _fecha_ini = cast(date_trunc('month', _current_date) as date);
                                    _fecha_fin = cast((date_trunc('MONTH', _current_date) + INTERVAL '1 MONTH - 1 day') as date);
                            END CASE;

                            UPDATE
                                ${schema.cdt}.cdt_cuenta_acumulador
                            SET
                                monto = CASE
                                            WHEN _rec_regla_acum.codigo_operacion = 'SUM' THEN
                                               (monto+(_monto*_rec_regla_acum.signo))
                                            WHEN _rec_regla_acum.codigo_operacion = 'COUNT' THEN
                                               (monto + (1*_rec_regla_acum.signo))
                                        END,
                                fecha_actualizacion = LOCALTIMESTAMP
                            WHERE
                                id_cuenta = _id_cuenta AND
                                fecha_inicio = _fecha_ini AND
                                fecha_fin = _fecha_fin AND
                                id_regla_acumulacion = _rec_regla_acum.id;
                            IF NOT FOUND THEN
                                INSERT INTO
                                    ${schema.cdt}.cdt_cuenta_acumulador
                                        (
                                            id_regla_acumulacion,
                                            id_cuenta,
                                            descripcion,
                                            codigo_operacion,
                                            monto,
                                            fecha_inicio,
                                            fecha_fin,
                                            fecha_creacion,
                                            fecha_actualizacion
                                        )
                                    VALUES
                                        (
                                            _rec_regla_acum.id,
                                            _id_cuenta,
                                            _rec_regla_acum.descripcion,
                                            _rec_regla_acum.codigo_operacion,
                                            CASE
                                                WHEN _rec_regla_acum.codigo_operacion = 'SUM' THEN
                                                    (_monto*_rec_regla_acum.signo)
                                                WHEN _rec_regla_acum.codigo_operacion = 'COUNT' THEN
                                                    (1*_rec_regla_acum.signo)
                                            END,
                                            _fecha_ini,
                                            _fecha_fin,
                                            LOCALTIMESTAMP,
                                            LOCALTIMESTAMP
                                        );
                            END IF;
                        ELSE
                            UPDATE
                                ${schema.cdt}.cdt_cuenta_acumulador
                            SET
                                monto = CASE
                                            WHEN _rec_regla_acum.codigo_operacion = 'SUM' THEN
                                               (monto+(_monto*_rec_regla_acum.signo))
                                            WHEN _rec_regla_acum.codigo_operacion = 'COUNT' THEN
                                               (monto + (1*_rec_regla_acum.signo))
                                        END,
                                fecha_actualizacion = LOCALTIMESTAMP
                            WHERE
                                id_cuenta = _id_cuenta AND
                                fecha_inicio = to_date('01-01-1900', 'dd-MM-YYYY') AND
                                fecha_fin = to_date('31-12-2100', 'dd-MM-YYYY') AND
                                id_regla_acumulacion = _rec_regla_acum.id;
                            IF NOT FOUND THEN
                                INSERT INTO
                                    ${schema.cdt}.cdt_cuenta_acumulador
                                    (
                                        id_regla_acumulacion,
                                        id_cuenta,
                                        descripcion,
                                        codigo_operacion,
                                        monto,
                                        fecha_inicio,
                                        fecha_fin,
                                        fecha_creacion,
                                        fecha_actualizacion
                                    )
                                VALUES
                                    (
                                        _rec_regla_acum.id,
                                        _id_cuenta,
                                         _rec_regla_acum.descripcion,
                                        _rec_regla_acum.codigo_operacion,
                                        CASE
                                            WHEN _rec_regla_acum.codigo_operacion = 'SUM' THEN
                                                (_monto*_rec_regla_acum.signo)
                                            WHEN _rec_regla_acum.codigo_operacion = 'COUNT' THEN
                                                (1*_rec_regla_acum.signo)
                                        END,
                                        to_date('01-01-1900', 'dd-MM-YYYY'),
                                        to_date('31-12-2100', 'dd-MM-YYYY'),
                                        LOCALTIMESTAMP,
                                        LOCALTIMESTAMP
                                    );
                            END IF;-- END IF NOT FOUND
                        END IF;-- EN IF PERIOCIDAD
                    EXCEPTION
                        WHEN OTHERS THEN
                            _num_error := SQLSTATE;
                            _msj_error := '[in_cdt_procesa_acumuladores] Error al insertar o actualizar acumulacion. CAUSA ('|| SQLERRM ||')';
                        RETURN;
                    END;
                END LOOP;-- END LOOP REGLA ACUMULACION

    EXCEPTION
        WHEN OTHERS THEN
            _num_error := SQLSTATE;
            _msj_error := '[in_cdt_procesa_acumuladores] Error desconocido en crea cuenta acumulacion. CAUSA ('|| SQLERRM ||')';
        RETURN;
    END;
$$
LANGUAGE 'plpgsql';

-- //@UNDO
-- SQL to undo the change goes here.
 DROP FUNCTION IF EXISTS  ${schema.cdt}.in_cdt_procesa_acumuladores_v10(NUMERIC,NUMERIC,NUMERIC);

