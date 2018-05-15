package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @autor vutreras
 */
public class Test_20180514105358_create_sp_mc_prp_buscar_tarjetas_v10 extends Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10 {

  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_tarjeta", SCHEMA));
  }

  /**
   *
   * @param id
   * @param idUsuario
   * @param expiracion
   * @param estado
   * @param contrato
   * @return
   * @throws SQLException
   */
  protected Map<String, Object> searchCards(Long id, Long idUsuario, Integer expiracion, String estado, String contrato) throws SQLException {
    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      idUsuario != null ? idUsuario : new NullParam(Types.BIGINT),
      expiracion != null ? expiracion : new NullParam(Types.INTEGER),
      estado != null ? estado : new NullParam(Types.VARCHAR),
      contrato != null ? contrato : new NullParam(Types.VARCHAR),
      new OutParam("_result", Types.OTHER),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return dbUtils.execute(SCHEMA + ".mc_prp_buscar_tarjetas_v10", params);
  }

  @Test
  public void searchCardByAllFields() throws SQLException {

    Map<String, Object> obj1 = insertCard("ACTIVA");

    Map<String, Object> resp = searchCards((long) obj1.get("id"), (long) obj1.get("id_usuario"), (int) obj1.get("expiracion"), (String) obj1.get("estado"), (String) obj1.get("contrato"));

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }
  }

  @Test
  public void searchUserBy_id() throws SQLException {

    Map<String, Object> obj1 = insertCard("ACTIVA");

    Map<String, Object> resp = searchCards((long) obj1.get("id"), null, null, null, null);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchCards(((long) obj1.get("id")) + 1, null, null, null, null);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_id_usuario() throws SQLException {

    Map<String, Object> obj1 = insertCard("ACTIVA");

    Map<String, Object> resp = searchCards(null, (long) obj1.get("id_usuario"), null, null, null);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchCards(null, ((long) obj1.get("id_usuario")) + 1, null, null, null);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_expiracion() throws SQLException {

    Map<String, Object> obj1 = insertCard("ACTIVA");

    Map<String, Object> resp = searchCards(null, null, (int) obj1.get("expiracion"), null, null);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchCards(null, null, ((int) obj1.get("expiracion")) + 1, null, null);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_estado() throws SQLException {

    /**
     * se crean un registro y solo se deberia encontrar uno que coincidan con el criterio de busqueda
     */

    String status = "ACTIVA" + numberUtils.random(1111,9999);

    Map<String, Object> obj1 = insertCard(status);
    Map<String, Object> obj2 = insertCard(status);

    Map<String, Object> resp = searchCards(null, null, null, (String) obj1.get("estado"), null);

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 2 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }

    Map mCard2 = (Map)result.get(1);
    Set<String> keys2 = obj2.keySet();
    for (String k : keys2) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj2.get(k), mCard2.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchCards(null, null, null, (String) obj1.get("estado") + 1, null);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }

  @Test
  public void searchUserBy_contrato() throws SQLException {

    Map<String, Object> obj1 = insertCard("ACTIVA");

    Map<String, Object> resp = searchCards(null, null, null, null, (String) obj1.get("contrato"));

    List result = (List)resp.get("_result");

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));
    Assert.assertNotNull("debe retornar una lista", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());

    Map mCard1 = (Map)result.get(0);
    Set<String> keys = obj1.keySet();
    for (String k : keys) {
      Assert.assertEquals("Debe ser la misma tarjeta", obj1.get(k), mCard1.get(k));
    }

    //Caso en donde no deberia encontrar un registro

    Map<String, Object> resp2 = searchCards(null, null, null, null, (String) obj1.get("contrato") + 1);

    Assert.assertEquals("Codigo de error debe ser 0", "0", resp2.get("_error_code"));
    Assert.assertNull("no debe retornar una lista", resp2.get("_result"));
  }
}
