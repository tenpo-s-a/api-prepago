package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.*;
import sun.applet.resources.MsgAppletViewer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_20190221170339_create_sp_prp_busca_movimientos_tecnocom_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".prp_busca_movimientos_tecnocom_v11";

  @Before
  @After
  public void beforeAfter() {
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimientos_tecnocom cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_movimientos_tecnocom_hist cascade",SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("truncate %s.prp_archivos_conciliacion cascade", SCHEMA));
  }

  @Test
  public void testBuscaMovimientosTecnocom() throws Exception{
    Map<String, Object> conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId = numberUtils.toLong(conFile.get("_r_id"));
    Assert.assertNotNull("Id Archivo no puede ser nulo", fileId);

    // Segundo archivo
    conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId2 = numberUtils.toLong(conFile.get("_r_id"));
    Assert.assertNotNull("Id Archivo no puede ser nulo", fileId2);

    List<Map<String,Object>> insertedMovements = new ArrayList<>();

    Map<String, Object> movTecnocom = Test_20190221092320_create_sp_prp_crea_movimiento_tecnocom_v10.creaMovimientoTecnocom(fileId, "876123786", "7687238284", 3001, new BigDecimal(45000), "876986", 0);
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null", movTecnocom);
    Assert.assertEquals("Respuesta no puede ser != 0","0", movTecnocom.get("_error_code"));
    Assert.assertEquals("Respuesta no puede ser vacio","", movTecnocom.get("_error_msg"));
    Assert.assertNotEquals("Id no puede ser 0", 0, movTecnocom.get("_r_id"));
    insertedMovements.add(movTecnocom);

    movTecnocom = Test_20190221092320_create_sp_prp_crea_movimiento_tecnocom_v10.creaMovimientoTecnocom(fileId, "874678264", "9827347863", 3001, new BigDecimal(25000), "376518", 1);
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null", movTecnocom);
    Assert.assertEquals("Respuesta no puede ser != 0","0", movTecnocom.get("_error_code"));
    Assert.assertEquals("Respuesta no puede ser vacio","", movTecnocom.get("_error_msg"));
    Assert.assertNotEquals("Id no puede ser 0", 0, movTecnocom.get("_r_id"));
    insertedMovements.add(movTecnocom);

    movTecnocom = Test_20190221092320_create_sp_prp_crea_movimiento_tecnocom_v10.creaMovimientoTecnocom(fileId, "4747282847", "74758573737", 3002, new BigDecimal(15000), "478829", 0);
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null", movTecnocom);
    Assert.assertEquals("Respuesta no puede ser != 0","0", movTecnocom.get("_error_code"));
    Assert.assertEquals("Respuesta no puede ser vacio","", movTecnocom.get("_error_msg"));
    Assert.assertNotEquals("Id no puede ser 0", 0, movTecnocom.get("_r_id"));
    insertedMovements.add(movTecnocom);

    movTecnocom = Test_20190221092320_create_sp_prp_crea_movimiento_tecnocom_v10.creaMovimientoTecnocom(fileId2, "3747585637272", "2647448373", 3001, new BigDecimal(5000), "473835", 0);
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null", movTecnocom);
    Assert.assertEquals("Respuesta no puede ser != 0","0", movTecnocom.get("_error_code"));
    Assert.assertEquals("Respuesta no puede ser vacio","", movTecnocom.get("_error_msg"));
    Assert.assertNotEquals("Id no puede ser 0", 0, movTecnocom.get("_r_id"));
    insertedMovements.add(movTecnocom);



    Map<String, Object> searchResult =  buildQuery("prp_movimientos_tecnocom", fileId,"ONLI", null, null, null, null, null);
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    List<Map<String, Object>> result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 3",3,result.size());
    System.out.println("Result: " + result);

    int comparedResults = 0;
    for(Map<String, Object> insertedMov : insertedMovements){
      for(Map<String, Object> foundMov : result) {
        if(insertedMov.get("id").equals(foundMov.get("_id"))) {
          Assert.assertTrue("Deben ser iguales", compareMovs(insertedMov, foundMov));
          comparedResults++;
        }
      }
    }
    Assert.assertEquals("Se deben comparar 3 resultados", 3, comparedResults);

    // null busca en tabla por defecto
    searchResult =  buildQuery(null, fileId,"ONLI", null, null, null, null, null);
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 3",3,result.size());
    System.out.println("Result: " + result);

    comparedResults = 0;
    for(Map<String, Object> insertedMov : insertedMovements){
      for(Map<String, Object> foundMov : result) {
        if(insertedMov.get("id").equals(foundMov.get("_id"))) {
          Assert.assertTrue("Deben ser iguales", compareMovs(insertedMov, foundMov));
          comparedResults++;
        }
      }
    }
    Assert.assertEquals("Se deben comparar 3 resultados", 3, comparedResults);

    // Debe haber 3 copias en hist
    searchResult =  buildQuery("prp_movimientos_tecnocom_hist", fileId,"ONLI", null, null, null, null, null);
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 3",3,result.size());
    System.out.println("Result: " + result);

    comparedResults = 0;
    for(Map<String, Object> insertedMov : insertedMovements){
      for(Map<String, Object> foundMov : result) {
        if(insertedMov.get("pan").equals(foundMov.get("_pan"))) {
          Assert.assertTrue("Deben ser iguales", compareMovs(insertedMov, foundMov));
          comparedResults++;
        }
      }
    }
    Assert.assertEquals("Se deben comparar 3 resultados", 3, comparedResults);

    Map<String, Object> firstInsertedMov = insertedMovements.get(0);

    // Buscar por pan
    searchResult =  buildQuery("prp_movimientos_tecnocom", null,null, firstInsertedMov.get("pan").toString(), null, null, null, null);
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 1",1, result.size());
    Map<String, Object> currentFoundMov = result.get(0);
    Assert.assertTrue("Deben ser iguales", compareMovs(firstInsertedMov, currentFoundMov));

    // Buscar por indnorcor
    firstInsertedMov = insertedMovements.get(1);
    searchResult =  buildQuery("prp_movimientos_tecnocom", null,null, null, 1, null, null, null);
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 1",1, result.size());
    currentFoundMov = result.get(0);
    Assert.assertTrue("Deben ser iguales", compareMovs(firstInsertedMov, currentFoundMov));

    // Buscar por tipofac
    searchResult =  buildQuery("prp_movimientos_tecnocom", null,null, null, null, 3001, null, null);
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 3",3, result.size());
    comparedResults = 0;
    for(Map<String, Object> insertedMov : insertedMovements){
      for(Map<String, Object> foundMov : result) {
        if(insertedMov.get("pan").equals(foundMov.get("_pan"))) {
          Assert.assertTrue("Deben ser iguales", compareMovs(insertedMov, foundMov));
          comparedResults++;
        }
      }
    }
    Assert.assertEquals("Se deben comparar 3 resultados", 3, comparedResults);


    // Buscar por fecfac
    searchResult =  buildQuery("prp_movimientos_tecnocom", null,null, null, null, null, new Date(System.currentTimeMillis()), null);
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 4",4, result.size());
    comparedResults = 0;
    for(Map<String, Object> insertedMov : insertedMovements){
      for(Map<String, Object> foundMov : result) {
        if(insertedMov.get("pan").equals(foundMov.get("_pan"))) {
          Assert.assertTrue("Deben ser iguales", compareMovs(insertedMov, foundMov));
          comparedResults++;
        }
      }
    }
    Assert.assertEquals("Se deben comparar 4 resultados", 4, comparedResults);

    // Buscar por indnorcor
    firstInsertedMov = insertedMovements.get(1);
    searchResult =  buildQuery("prp_movimientos_tecnocom", null,null, null, null, null, null, firstInsertedMov.get("numaut").toString());
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 1",1, result.size());
    currentFoundMov = result.get(0);
    Assert.assertTrue("Deben ser iguales", compareMovs(firstInsertedMov, currentFoundMov));

    // All null trae todos
    searchResult =  buildQuery("prp_movimientos_tecnocom", null,null, null, null, null, null, null);
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 4",4, result.size());
    comparedResults = 0;
    for(Map<String, Object> insertedMov : insertedMovements){
      for(Map<String, Object> foundMov : result) {
        if(insertedMov.get("pan").equals(foundMov.get("_pan"))) {
          Assert.assertTrue("Deben ser iguales", compareMovs(insertedMov, foundMov));
          comparedResults++;
        }
      }
    }
    Assert.assertEquals("Se deben comparar 4 resultados", 4, comparedResults);
  }

  boolean compareMovs(Map<String, Object> insertedMov, Map<String, Object> foundMov) {
    Assert.assertEquals("Deben tener mismo fileId", insertedMov.get("idArchivo"), foundMov.get("_idarchivo"));
    Assert.assertEquals("Deben tener mismo cuenta", insertedMov.get("cuenta"), foundMov.get("_cuenta"));
    Assert.assertEquals("Deben tener mismo pan", insertedMov.get("pan"), foundMov.get("_pan"));
    Assert.assertEquals("Deben tener mismo tipofac", insertedMov.get("tipofac"), foundMov.get("_tipofac"));
    Assert.assertEquals("Deben tener mismo numaut", insertedMov.get("numaut"), foundMov.get("_numaut"));
    Assert.assertEquals("Deben tener mismo impfac", ((BigDecimal) insertedMov.get("impfac")).stripTrailingZeros(), ((BigDecimal)foundMov.get("_impfac")).stripTrailingZeros());
    return true;
  }

  private static Map<String, Object> buildQuery(String tableName, Long fileId, String originope, String encryptedPan, Integer indnorcor, Integer tipofac, Date fecfac, String numaut) throws Exception {
    Object[] params = {
      tableName != null ? new InParam(tableName, Types.VARCHAR) : new InParam(tableName, Types.VARCHAR),
      fileId != null ? new InParam(fileId, Types.BIGINT) : new NullParam(Types.BIGINT),
      originope != null ? new InParam(originope, Types.VARCHAR) : new NullParam(Types.VARCHAR),
      encryptedPan != null ? new InParam(encryptedPan, Types.VARCHAR) : new NullParam(Types.VARCHAR),
      indnorcor != null ? new InParam(indnorcor, Types.NUMERIC) : new NullParam(Types.NUMERIC),
      tipofac != null ? new InParam(tipofac, Types.NUMERIC) : new NullParam(Types.NUMERIC),
      fecfac != null ? new InParam(fecfac, Types.DATE) : new NullParam(Types.DATE),
      numaut != null ? new InParam(numaut, Types.VARCHAR) : new NullParam(Types.VARCHAR)
    };
    return dbUtils.execute(SP_NAME, params);
  }
}
