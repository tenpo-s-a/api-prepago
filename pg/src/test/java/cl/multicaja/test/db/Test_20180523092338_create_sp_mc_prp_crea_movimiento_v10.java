package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard;

public class Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_crea_movimiento_v10";
  private static final String TABLE_NAME = SCHEMA + ".prp_movimiento";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s", TABLE_NAME));
  }

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s", TABLE_NAME));
  }

  public static InParam setInParam(Object o){
    return new InParam(o,Types.NUMERIC);
  }

  /**
   *
   * @param idMovimientoRef
   * @param idUsuario
   * @param idTxExterno
   * @param tipoMovimiento
   * @param estado
   * @param cuenta
   * @param clamon
   * @param indnorcor
   * @param tipofac
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> insertaMovimiento(Long idMovimientoRef, Long idUsuario, String idTxExterno, String tipoMovimiento,
                                                      String estado, String cuenta, Integer clamon, Integer indnorcor, Integer tipofac) throws SQLException {
    Object[] params = {
      setInParam(idMovimientoRef), //_id_mov_ref NUMERIC
      setInParam(idUsuario), //_id_usuario NUMERIC
      idTxExterno, //_id_tx_externo VARCHAR
      tipoMovimiento, //_tipo_movimiento VARCHAR
      setInParam(getUniqueLong()), //_monto NUMERIC
      estado, //_estado VARCHAR
      "AA",//_codent VARCHAR
      "CA",//_centalta VARCHAR
      cuenta,//_cuenta VARCHAR
      setInParam(clamon),//_clamon NUMERIC
      setInParam(indnorcor),//_indnorcor NUMERIC
      setInParam(tipofac),//_tipofac NUMERIC
      new Date(System.currentTimeMillis()),//_fecfac DATE
      "123",//_numreffac VARCHAR
      getRandomNumericString(16),// _pan VARCHAR,
      setInParam(90),//_clamondiv NUMERIC,
      setInParam(10),//_impdiv NUMERIC,
      setInParam(10),//_impfac NUMERIC,
      setInParam(10),//_cmbapli NUMERIC,
      "1231",//_numaut VARCHAR,
      "A",//_indproaje VARCHAR,
      "123",//_codcom VARCHAR,
      1234,//_codact NUMERIC,
      setInParam(1),//_impliq NUMERIC,
      setInParam(1), //_clamonliq NUMERIC,
      setInParam(1), //_codpais NUMERIC,
      "ASDA",//_nompob VARCHAR,
      setInParam(22),//_numextcta NUMERIC,
      setInParam(22),//_nummovext NUMERIC,
      setInParam(123),// _clamone NUMERIC,
      "V",//_tipolin VARCHAR,
      setInParam( 2),//_linref NUMERIC,
      setInParam(2),//_numbencta NUMERIC,
      setInParam(2),//_numplastico NUMERIC,
      new OutParam("_id", Types.NUMERIC),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);
    System.out.println(resp);
    return resp;
  }

  /**
   *
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> insertaMovimientoRandom() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA";
    String estado = "PRUEBA";
    String cuenta = getRandomNumericString(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;

    Map<String, Object> mapMovimiento = insertaMovimiento(idMovimientoRef, idUsuario, idTxExterno, tipoMovimiento, estado, cuenta, clamon, indnorcor, tipofac);
    return mapMovimiento;
  }

  @Test
  public void llamadaSpCreaMovimientoOk() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA";
    String estado = "PRUEBA";
    String cuenta = getRandomNumericString(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;

    Map<String, Object> resp = insertaMovimiento(idMovimientoRef, idUsuario, idTxExterno, tipoMovimiento, estado, cuenta, clamon, indnorcor, tipofac);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Debe retornar un id", true, numberUtils.toLong(resp.get("_id")) > 0);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
  }

  @Test
  public void llamadaSpCreaMovimientoNoOk() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idUsuario = (Long)mapCard.get("id_usuario");

    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA";
    String estado = "PRUEBA";
    String cuenta = getRandomNumericString(10);
    Integer clamon = 15200; //maximo largo 3
    Integer indnorcor = 0;
    Integer tipofac = 3001;

    Map<String, Object> resp = insertaMovimiento(idMovimientoRef, idUsuario, idTxExterno, tipoMovimiento, estado, cuenta, clamon, indnorcor, tipofac);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertNotEquals("Codigo de error debe ser != 0", "0", resp.get("_error_code"));
  }
}
