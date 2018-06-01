package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard;
import static cl.multicaja.test.db.Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertaMovimiento;

/**
 * @autor vutreras
 */
public class Test_20180601080757_create_sp_mc_prp_buscar_movimientos_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_buscar_movimientos_v10";
  private static final String TABLE_NAME = SCHEMA + ".prp_movimiento";

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s", TABLE_NAME));
  }

  @AfterClass
  public static void afterClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s", TABLE_NAME));
  }

  /**
   *
   * @param id
   * @param idMovimientoRef
   * @param idPrepaidUser
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
  public static Map<String, Object> searchMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, String tipoMovimiento,
                                                    String estado, String cuenta, Integer clamon, Integer indnorcor, Integer tipofac) throws SQLException {
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      idMovimientoRef != null ? idMovimientoRef : new NullParam(Types.BIGINT),
      idPrepaidUser != null ? idPrepaidUser : new NullParam(Types.BIGINT),
      idTxExterno != null ? idTxExterno : new NullParam(Types.VARCHAR),
      tipoMovimiento != null ? tipoMovimiento : new NullParam(Types.VARCHAR),
      estado != null ? estado : new NullParam(Types.VARCHAR),
      cuenta != null ? cuenta : new NullParam(Types.VARCHAR),
      clamon != null ? clamon : new NullParam(Types.NUMERIC),
      indnorcor != null ? indnorcor : new NullParam(Types.NUMERIC),
      tipofac != null ? tipofac : new NullParam(Types.NUMERIC)
    };

    return dbUtils.execute(SP_NAME, params);
  }

  /**
   *
   * @param mapMov
   */
  private void checkColumns(Map<String, Object> mapMov) {

    String[] columns = {
      "_id",
      "_id_movimiento_ref",
      "_id_usuario",
      "_id_tx_externo",
      "_tipo_movimiento",
      "_monto",
      "_estado",
      "_fecha_creacion",
      "_fecha_actualizacion",
      "_codent",
      "_centalta",
      "_cuenta",
      "_clamon",
      "_indnorcor",
      "_tipofac",
      "_fecfac",
      "_numreffac",
      "_pan",
      "_clamondiv",
      "_impdiv",
      "_impfac",
      "_cmbapli",
      "_numaut",
      "_indproaje",
      "_codcom",
      "_codact",
      "_impliq",
      "_clamonliq",
      "_codpais",
      "_nompob",
      "_numextcta",
      "_nummovext",
      "_clamone",
      "_tipolin",
      "_linref",
      "_numbencta",
      "_numplastico"
    };

    for (String column : columns) {
      Assert.assertTrue("Debe contener la columna " + column, mapMov.containsKey(column));
    }
  }

  @Test
  public void findMovements() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idPrepaidUser = (Long)mapCard.get("id_usuario");
    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA1";
    String estado = "PRUEBA1";
    String cuenta = RandomStringUtils.randomNumeric(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;

    Map<String, Object> mapMov1 = insertaMovimiento(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, cuenta, clamon, indnorcor, tipofac);

    {
      Long id = numberUtils.toLong(mapMov1.get("_id"));

      Map<String, Object> resp = searchMovements(id, null, null, null, null, null, null, null, null, null);

      List result = (List)resp.get("result");

      System.out.println(result);

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1, result.size());

      Map<String, Object> mapMov = (Map)result.get(0);

      Assert.assertEquals("debe ser el mismo registro", idMovimientoRef, mapMov.get("_id_movimiento_ref"));
      Assert.assertEquals("debe ser el mismo registro", idPrepaidUser, mapMov.get("_id_usuario"));
      Assert.assertEquals("debe ser el mismo registro", idTxExterno, mapMov.get("_id_tx_externo"));
      Assert.assertEquals("debe ser el mismo registro", tipoMovimiento, mapMov.get("_tipo_movimiento"));
      Assert.assertEquals("debe ser el mismo registro", estado, mapMov.get("_estado"));
      Assert.assertEquals("debe ser el mismo registro", cuenta, mapMov.get("_cuenta"));
      Assert.assertEquals("debe ser el mismo registro", clamon, numberUtils.toInteger(mapMov.get("_clamon")));
      Assert.assertEquals("debe ser el mismo registro", indnorcor, numberUtils.toInteger(mapMov.get("_indnorcor")));
      Assert.assertEquals("debe ser el mismo registro", tipofac, numberUtils.toInteger(mapMov.get("_tipofac")));
    }

    tipoMovimiento = "CARGA2";
    estado = "PRUEBA2";
    clamon = 153;
    indnorcor = 1;
    tipofac = 3000;

    Map<String, Object> mapMov2 = insertaMovimiento(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, cuenta, clamon, indnorcor, tipofac);

    {
      Long id = numberUtils.toLong(mapMov2.get("_id"));

      Map<String, Object> resp = searchMovements(id, null, null, null, null, null, null, null, null, null);

      List result = (List)resp.get("result");

      System.out.println(result);

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1, result.size());

      Map<String, Object> mapMov = (Map)result.get(0);

      checkColumns(mapMov);

      Assert.assertEquals("debe ser el mismo registro", idMovimientoRef, mapMov.get("_id_movimiento_ref"));
      Assert.assertEquals("debe ser el mismo registro", idPrepaidUser, mapMov.get("_id_usuario"));
      Assert.assertEquals("debe ser el mismo registro", idTxExterno, mapMov.get("_id_tx_externo"));
      Assert.assertEquals("debe ser el mismo registro", tipoMovimiento, mapMov.get("_tipo_movimiento"));
      Assert.assertEquals("debe ser el mismo registro", estado, mapMov.get("_estado"));
      Assert.assertEquals("debe ser el mismo registro", cuenta, mapMov.get("_cuenta"));
      Assert.assertEquals("debe ser el mismo registro", clamon, numberUtils.toInteger(mapMov.get("_clamon")));
      Assert.assertEquals("debe ser el mismo registro", indnorcor, numberUtils.toInteger(mapMov.get("_indnorcor")));
      Assert.assertEquals("debe ser el mismo registro", tipofac, numberUtils.toInteger(mapMov.get("_tipofac")));
    }

  }

  @Test
  public void findMovements_tipoMovimiento() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idPrepaidUser = (Long)mapCard.get("id_usuario");
    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA1";
    String estado = "PRUEBA1";
    String cuenta = RandomStringUtils.randomNumeric(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;

    insertaMovimiento(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, cuenta, clamon, indnorcor, tipofac);
    insertaMovimiento(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, cuenta, clamon, indnorcor, tipofac);

    Map<String, Object> resp = searchMovements(null, null, idPrepaidUser, null, tipoMovimiento, null, null, null, null, null);

    List result = (List)resp.get("result");

    System.out.println(result);

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 2, result.size());

    for (int j = 0; j < result.size(); j++) {

      Map<String, Object> mapMov = (Map) result.get(j);

      checkColumns(mapMov);

      Assert.assertEquals("debe ser el mismo registro", idMovimientoRef, mapMov.get("_id_movimiento_ref"));
      Assert.assertEquals("debe ser el mismo registro", idPrepaidUser, mapMov.get("_id_usuario"));
      Assert.assertEquals("debe ser el mismo registro", idTxExterno, mapMov.get("_id_tx_externo"));
      Assert.assertEquals("debe ser el mismo registro", tipoMovimiento, mapMov.get("_tipo_movimiento"));
      Assert.assertEquals("debe ser el mismo registro", estado, mapMov.get("_estado"));
      Assert.assertEquals("debe ser el mismo registro", cuenta, mapMov.get("_cuenta"));
      Assert.assertEquals("debe ser el mismo registro", clamon, numberUtils.toInteger(mapMov.get("_clamon")));
      Assert.assertEquals("debe ser el mismo registro", indnorcor, numberUtils.toInteger(mapMov.get("_indnorcor")));
      Assert.assertEquals("debe ser el mismo registro", tipofac, numberUtils.toInteger(mapMov.get("_tipofac")));
    }
  }

  @Test
  public void findMovements_estado() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idPrepaidUser = (Long)mapCard.get("id_usuario");
    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA1";
    String estado = "PRUEBA1";
    String cuenta = RandomStringUtils.randomNumeric(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;

    insertaMovimiento(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, cuenta, clamon, indnorcor, tipofac);
    insertaMovimiento(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, cuenta, clamon, indnorcor, tipofac);

    Map<String, Object> resp = searchMovements(null, null, idPrepaidUser, null, null, estado, null, null, null, null);

    List result = (List)resp.get("result");

    System.out.println(result);

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 2, result.size());

    for (int j = 0; j < result.size(); j++) {

      Map<String, Object> mapMov = (Map) result.get(j);

      checkColumns(mapMov);

      Assert.assertEquals("debe ser el mismo registro", idMovimientoRef, mapMov.get("_id_movimiento_ref"));
      Assert.assertEquals("debe ser el mismo registro", idPrepaidUser, mapMov.get("_id_usuario"));
      Assert.assertEquals("debe ser el mismo registro", idTxExterno, mapMov.get("_id_tx_externo"));
      Assert.assertEquals("debe ser el mismo registro", tipoMovimiento, mapMov.get("_tipo_movimiento"));
      Assert.assertEquals("debe ser el mismo registro", estado, mapMov.get("_estado"));
      Assert.assertEquals("debe ser el mismo registro", cuenta, mapMov.get("_cuenta"));
      Assert.assertEquals("debe ser el mismo registro", clamon, numberUtils.toInteger(mapMov.get("_clamon")));
      Assert.assertEquals("debe ser el mismo registro", indnorcor, numberUtils.toInteger(mapMov.get("_indnorcor")));
      Assert.assertEquals("debe ser el mismo registro", tipofac, numberUtils.toInteger(mapMov.get("_tipofac")));
    }
  }
}
