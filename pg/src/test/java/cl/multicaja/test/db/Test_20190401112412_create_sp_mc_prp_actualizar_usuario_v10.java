package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.*;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;


import static cl.multicaja.test.db.Test_20190401160011_create_sp_mc_prp_buscar_usuarios_v11.searchUsers;
import static cl.multicaja.test.db.Test_20190401112103_create_sp_mc_prp_crear_usuario_v11.buildUser;
import static cl.multicaja.test.db.Test_20190401112103_create_sp_mc_prp_crear_usuario_v11.insertUser;

public class Test_20190401112412_create_sp_mc_prp_actualizar_usuario_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".mc_prp_actualizar_estado_usuario_v11";


  @Before
  public void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_usuario cascade", SCHEMA));
  }

  /**
   *
   * @return
   */
  public static Object[] buildUserForUpdate(Long userId) {

    Object[] params = {
      userId, //id
      getRandomString(10), //_numero_documento
      getRandomString(10), //_nombre
      getRandomString(10), //_apellido
      getRandomString(10), //_estado
      getRandomString(10), //_nivel
      getRandomString(10), //_uuid
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return params;
  }

  public Map<String, Object> updateUser(Object[] params) throws SQLException {

    Map<String,Object> response = dbUtils.execute(SP_NAME, params);

    response.put("_id",params[0]);
    response.put("_numero_documento",params[1]);
    response.put("_nombre",params[2]);
    response.put("_apellido",params[3]);
    response.put("_estado",params[4]);
    response.put("_nivel",params[5]);
    response.put("_uuid",params[6]);

    return response;
  }


  @Test
  public void updateStatusOk() throws SQLException {


    Object[] paramsToCreate = buildUser();
    Map<String, Object> respCreated = insertUser(paramsToCreate);
    Assert.assertNotNull("Debe retornar respuesta", respCreated);
    Assert.assertEquals("Codigo de error debe ser 0", "0", respCreated.get("_error_code"));
    Assert.assertTrue("debe retornar un id", numberUtils.toLong(respCreated.get("_r_id")) > 0);

    Object[] paramsToUpdate = buildUserForUpdate(numberUtils.toLong(respCreated.get("_r_id")));
    Map<String, Object> respUpdated = updateUser(paramsToUpdate);
    Assert.assertNotNull("Debe retornar respuesta", respUpdated);
    Assert.assertEquals("Codigo de error debe ser 0", "0", respUpdated.get("_error_code"));

    Map<String, Object> userFoundResponse = searchUsers(numberUtils.toLong(respCreated.get("_r_id")), null, null);
    List result = (List)userFoundResponse.get("result");
    Map<String,Object> userFound = (Map)result.get(0);

    Assert.assertNotNull("debe retornar datos", result);
    Assert.assertEquals("Debe contener un elemento", 1 , result.size());
    Assert.assertEquals("igual id",  respUpdated.get("_id"), userFound.get("_id"));
    Assert.assertEquals("igual numero documento",  respUpdated.get("_numero_documento"), userFound.get("_numero_documento"));
    Assert.assertEquals("igual tipo documento",  respUpdated.get("_tipo_documento"), userFound.get("_tipo_documento"));
    Assert.assertEquals("igual nombre",  respUpdated.get("_nombre"), userFound.get("_nombre"));
    Assert.assertEquals("igual apellido",  respUpdated.get("_apellido"), userFound.get("_apellido"));
    Assert.assertEquals("igual estado",  respUpdated.get("_estado"), userFound.get("_estado"));
    Assert.assertEquals("igual nivel",  respUpdated.get("_nivel"), userFound.get("_nivel"));

  }




}
