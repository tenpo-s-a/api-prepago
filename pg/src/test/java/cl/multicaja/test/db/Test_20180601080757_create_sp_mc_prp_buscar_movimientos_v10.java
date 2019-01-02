package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertCard;
import static cl.multicaja.test.db.Test_20180523092338_create_sp_mc_prp_crea_movimiento_v10.insertMovement;

/**
 * @autor vutreras
 */
public class Test_20180601080757_create_sp_mc_prp_buscar_movimientos_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_buscar_movimientos_v10";

  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", SCHEMA));
  }

  /**
   *
   * @param id
   * @param idMovimientoRef
   * @param idPrepaidUser
   * @param idTxExterno
   * @param tipoMovimiento
   * @param estado
   * @param estado_con_switch
   * @param estado_con_tecnocom
   * @param origen_movimiento
   * @param cuenta
   * @param clamon
   * @param indnorcor
   * @param tipofac
   * @return
   * @throws SQLException
   */
  public static Map<String, Object> searchMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, String tipoMovimiento,
                                                    String estado, String estado_con_switch, String estado_con_tecnocom, String origen_movimiento,
                                                    String cuenta, Integer clamon, Integer indnorcor, Integer tipofac, Date fecfac, String numaut) throws SQLException {
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      idMovimientoRef != null ? idMovimientoRef : new NullParam(Types.BIGINT),
      idPrepaidUser != null ? idPrepaidUser : new NullParam(Types.BIGINT),
      idTxExterno != null ? idTxExterno : new NullParam(Types.VARCHAR),
      tipoMovimiento != null ? tipoMovimiento : new NullParam(Types.VARCHAR),
      estado != null ? estado : new NullParam(Types.VARCHAR),
      estado_con_switch != null ? estado_con_switch : new NullParam(Types.VARCHAR),
      estado_con_tecnocom != null ? estado_con_tecnocom : new NullParam(Types.VARCHAR),
      origen_movimiento != null ? origen_movimiento : new NullParam(Types.VARCHAR),
      cuenta != null ? cuenta : new NullParam(Types.VARCHAR),
      clamon != null ? clamon : new NullParam(Types.NUMERIC),
      indnorcor != null ? indnorcor : new NullParam(Types.NUMERIC),
      tipofac != null ? tipofac : new NullParam(Types.NUMERIC),
      fecfac != null ? fecfac :  new NullParam(Types.DATE),
      numaut != null ? numaut : new NullParam(Types.VARCHAR)
    };

    return dbUtils.execute(SP_NAME, params);
  }

  public static Map<String, Object> searchMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, String tipoMovimiento,
                                                    String estado, String estado_con_switch, String estado_con_tecnocom, String origen_movimiento,
                                                    String cuenta, Integer clamon, Integer indnorcor, Integer tipofac, Date fecfac) throws SQLException {
    return searchMovements(id, idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento,
      estado, estado_con_switch, estado_con_tecnocom, origen_movimiento,
      cuenta, clamon, indnorcor, tipofac, fecfac, null);
  }

  /**
   *
   * @param map
   */
  private void checkColumns(Map<String, Object> map) {

    String[] columns = {
      "_id",
      "_id_movimiento_ref",
      "_id_usuario",
      "_id_tx_externo",
      "_tipo_movimiento",
      "_monto",
      "_estado",
      "_estado_de_negocio",
      "_estado_con_switch",
      "_estado_con_tecnocom",
      "_origen_movimiento",
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
      Assert.assertTrue("Debe contener la columna " + column, map.containsKey(column));
    }

    Assert.assertEquals("Debe contener solamente las columnas definidas", columns.length, map.keySet().size());
  }

  @Test
  public void searchMovements() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idPrepaidUser = (Long)mapCard.get("id_usuario");
    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA1";
    String estado = "PRUEBA1";
    String origenMovimiento = "API";
    String cuenta = getRandomNumericString(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;

    Map<String, Object> mapMov1 = insertMovement(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, origenMovimiento, cuenta, clamon, indnorcor, tipofac);

    {
      Long id = numberUtils.toLong(mapMov1.get("_id"));

      Map<String, Object> resp = searchMovements(id, null, null, null, null, null, null, null, null, null, null, null, null, null);

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
      Assert.assertEquals("debe ser el mismo registro", origenMovimiento, mapMov.get("_origen_movimiento"));
      Assert.assertEquals("debe ser el mismo registro", cuenta, mapMov.get("_cuenta"));
      Assert.assertEquals("debe ser el mismo registro", clamon, numberUtils.toInteger(mapMov.get("_clamon")));
      Assert.assertEquals("debe ser el mismo registro", indnorcor, numberUtils.toInteger(mapMov.get("_indnorcor")));
      Assert.assertEquals("debe ser el mismo registro", tipofac, numberUtils.toInteger(mapMov.get("_tipofac")));
    }

    tipoMovimiento = "CARGA2";
    estado = "PRUEBA2";
    origenMovimiento = "SAT";
    clamon = 153;
    indnorcor = 1;
    tipofac = 3000;

    Map<String, Object> mapMov2 = insertMovement(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, origenMovimiento, cuenta, clamon, indnorcor, tipofac);

    {
      Long id = numberUtils.toLong(mapMov2.get("_id"));

      Map<String, Object> resp = searchMovements(id, null, null, null, null, null, null, null, null, null, null, null, null, null);

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
      Assert.assertEquals("debe ser el mismo registro", origenMovimiento, mapMov.get("_origen_movimiento"));
      Assert.assertEquals("debe ser el mismo registro", cuenta, mapMov.get("_cuenta"));
      Assert.assertEquals("debe ser el mismo registro", clamon, numberUtils.toInteger(mapMov.get("_clamon")));
      Assert.assertEquals("debe ser el mismo registro", indnorcor, numberUtils.toInteger(mapMov.get("_indnorcor")));
      Assert.assertEquals("debe ser el mismo registro", tipofac, numberUtils.toInteger(mapMov.get("_tipofac")));
    }
    {
      Map<String, Object> resp = searchMovements(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
      //mapMov1
      //mapMov2
      List result = (List)resp.get("result");

      System.out.println(result);

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertTrue("Debe contener n elementos", result.size() > 0);

      Map<String, Object> mov1 = (Map)result.get(0);
      Map<String, Object> mov2 = (Map)result.get(1);
      Assert.assertEquals("Debe estar ordenado de forma DESC", numberUtils.toLong(mapMov2.get("_id")), numberUtils.toLong(mov1.get("_id")));
      Assert.assertEquals("Debe estar ordenado de forma DESC", numberUtils.toLong(mapMov1.get("_id")), numberUtils.toLong(mov2.get("_id")));
    }

  }

  @Test
  public void searchMovements_tipoMovimiento() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idPrepaidUser = (Long)mapCard.get("id_usuario");
    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA1";
    String estado = getRandomString(8);
    String origenMovimiento = getRandomString(3);
    String cuenta = getRandomNumericString(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;

    insertMovement(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, origenMovimiento, cuenta, clamon, indnorcor, tipofac);
    insertMovement(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, origenMovimiento, cuenta, clamon, indnorcor, tipofac);

    Map<String, Object> resp = searchMovements(null, null, idPrepaidUser, null, tipoMovimiento, null, null, null, null, null, null, null, null, null);

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
      Assert.assertEquals("debe ser el mismo registro", origenMovimiento, mapMov.get("_origen_movimiento"));
      Assert.assertEquals("debe ser el mismo registro", cuenta, mapMov.get("_cuenta"));
      Assert.assertEquals("debe ser el mismo registro", clamon, numberUtils.toInteger(mapMov.get("_clamon")));
      Assert.assertEquals("debe ser el mismo registro", indnorcor, numberUtils.toInteger(mapMov.get("_indnorcor")));
      Assert.assertEquals("debe ser el mismo registro", tipofac, numberUtils.toInteger(mapMov.get("_tipofac")));
    }
  }

  @Test
  public void searchMovements_estado() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idPrepaidUser = (Long)mapCard.get("id_usuario");
    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA1";
    String estado = getRandomString(8);
    String origenMovimiento = getRandomString(3);
    String cuenta = getRandomNumericString(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;

    insertMovement(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, origenMovimiento, cuenta, clamon, indnorcor, tipofac);
    insertMovement(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, origenMovimiento, cuenta, clamon, indnorcor, tipofac);

    Map<String, Object> resp = searchMovements(null, null, idPrepaidUser, null, null, estado, null, null, null, null, null, null, null, null);

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
      Assert.assertEquals("debe ser el mismo registro", origenMovimiento, mapMov.get("_origen_movimiento"));
      Assert.assertEquals("debe ser el mismo registro", cuenta, mapMov.get("_cuenta"));
      Assert.assertEquals("debe ser el mismo registro", clamon, numberUtils.toInteger(mapMov.get("_clamon")));
      Assert.assertEquals("debe ser el mismo registro", indnorcor, numberUtils.toInteger(mapMov.get("_indnorcor")));
      Assert.assertEquals("debe ser el mismo registro", tipofac, numberUtils.toInteger(mapMov.get("_tipofac")));
    }
  }

  private Date yesterday() {
    final Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    return new Date(cal.getTimeInMillis());
  }

  @Test
  public void searchMovements_date() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idPrepaidUser = (Long)mapCard.get("id_usuario");
    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA1";
    String estado = getRandomString(8);
    String cuenta = getRandomNumericString(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;
    String trxSource = "ONLI";

    Date yesterday = yesterday();
    Date today = new Date(System.currentTimeMillis());

    insertMovement(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, trxSource, cuenta, clamon, indnorcor, tipofac);
    insertMovement(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, trxSource, cuenta, clamon, indnorcor, tipofac, yesterday);

    {
      Map<String, Object> resp = searchMovements(null, null, idPrepaidUser, null, null, estado, null, null, null, null, null, null, null, yesterday);

      List result = (List)resp.get("result");

      System.out.println(result);

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1, result.size());

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
        Assert.assertEquals("debe ser el mismo registro", yesterday.toString(), (mapMov.get("_fecfac")).toString());
      }
    }

    {
      Map<String, Object> resp = searchMovements(null, null, idPrepaidUser, null, null, estado, null, null, null, null, null, null, null, today);

      List result = (List) resp.get("result");

      System.out.println(result);

      Assert.assertNotNull("debe retornar una lista", result);
      Assert.assertEquals("Debe contener un elemento", 1, result.size());

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
        Assert.assertEquals("debe ser el mismo registro", today.toString(), (mapMov.get("_fecfac")).toString());
      }
    }


  }

  @Test
  public void searchMovements_numaut() throws SQLException {

    Map<String, Object> mapCard = insertCard("ACTIVA");

    Long idMovimientoRef = getUniqueLong();
    Long idPrepaidUser = (Long)mapCard.get("id_usuario");
    String idTxExterno = getUniqueLong().toString();
    String tipoMovimiento = "CARGA1";
    String estado = getRandomString(8);
    String cuenta = getRandomNumericString(10);
    Integer clamon = 152;
    Integer indnorcor = 0;
    Integer tipofac = 3001;
    String trxSource = "ONLI";
    String numaut = getRandomNumericString(6);

    insertMovement(idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, trxSource, cuenta, clamon, indnorcor, tipofac, numaut);


    Map<String, Object> resp = searchMovements(null, null, null, null, null, null, null, null, null, null, null, null, null, null, numaut);

    List result = (List)resp.get("result");

    System.out.println(result);

    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1, result.size());


    Map<String, Object> mapMov = (Map) result.get(0);

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
    Assert.assertEquals("debe ser el mismo registro", numaut, mapMov.get("_numaut"));
  }
}
