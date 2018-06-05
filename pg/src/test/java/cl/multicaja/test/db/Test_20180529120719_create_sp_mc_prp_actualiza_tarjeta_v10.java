package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.multicaja.test.db.Test_20180514105345_create_sp_mc_prp_crear_tarjeta_v10.insertEmptyCard;
import static cl.multicaja.test.db.Test_20180514105358_create_sp_mc_prp_buscar_tarjetas_v10.searchCards;

public class Test_20180529120719_create_sp_mc_prp_actualiza_tarjeta_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_actualiza_tarjeta_v10";
  private static final String TABLE_NAME = SCHEMA + ".prp_tarjeta";
  @BeforeClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("DELETE from %s", TABLE_NAME));
  }


  public static Object[] buildUpdateCard(Long idTarjeta,Long idUsuario,String oldState, String newState) throws SQLException {

    Object[] params = {
      idTarjeta == null ? new NullParam(Types.BIGINT): idTarjeta,
      idUsuario == null ? new NullParam(Types.BIGINT):idUsuario , //_id_usuario
      oldState == null ? new NullParam(Types.VARCHAR):oldState ,
      getRandomString(10), //_pan
      getRandomString(10), //_pan_encriptado
      new NullParam(Types.VARCHAR), //_contrato
      0, //_expiracion
      newState, //_estado
      getRandomString(10)+" "+getRandomString(10), //_nombre_tarjeta
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return params;
  }

  @Test
  public  void testUpdateOk() throws SQLException {

    Map<String,Object> card = insertEmptyCard(getRandomString(10),"PEND");

    Object[] params = buildUpdateCard((Long) card.get("id"),(Long) card.get("id_usuario"),(String) card.get("estado"),"CREATED");
    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser 0", "0", resp.get("_error_code"));

    Map<String, Object> cardSearch = searchCards(null,(Long) card.get("id_usuario"),null,"CREATED",null);
    List  mCard1 = (List) cardSearch.get("result");
    Assert.assertEquals("El estado debe ser CREATED","CREATED",((HashMap<String,Object>)mCard1.get(0)).get("_estado"));
  }

  @Test
  public  void testUpdateIdTarjetaNull() throws SQLException {

    Map<String,Object> card = insertEmptyCard(getRandomString(10),"PEND");
    Object[] params = buildUpdateCard(null,(Long) card.get("id_usuario"),(String) card.get("estado"),"CREATED");
    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser MC001", "MC001", resp.get("_error_code"));

  }

  @Test
  public  void testUpdateIdUsuarioNull() throws SQLException {

    Map<String,Object> card = insertEmptyCard(getRandomString(10),"PEND");
    Object[] params = buildUpdateCard((Long) card.get("id"),null,(String) card.get("estado"),"CREATED");
    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser MC002", "MC002", resp.get("_error_code"));

  }

  @Test
  public  void testUpdateEstadoNull() throws SQLException {

    Map<String,Object> card = insertEmptyCard(getRandomString(10),"PEND");
    Object[] params = buildUpdateCard((Long) card.get("id"),(Long) card.get("id_usuario"),null,"CREATED");
    Map<String, Object> resp = dbUtils.execute(SP_NAME, params);

    Assert.assertNotNull("Debe retornar respuesta", resp);
    Assert.assertEquals("Codigo de error debe ser MC003", "MC003", resp.get("_error_code"));

  }

}
