package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class Test_20190221170407_create_sp_prp_elimina_movimientos_tecnocom_v10 extends TestDbBasePg {


  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimientos_tecnocom", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimientos_tecnocom_hist", SCHEMA));
  }
  @Test
  public void eliminarOK() throws Exception {
    Map<String, Object> conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId = numberUtils.toLong(conFile.get("_r_id"));

    Assert.assertNotNull("Id Archivo no puede ser nulo",fileId);

    for(int i=0;i <3 ;i++) {
      Map<String, Object> movTecnocom = Test_20190221092320_create_sp_prp_crea_movimiento_tecnocom_v10.creaMovimientoTecnocom(fileId,getRandomString(10),getRandomString(30),
        numberUtils.random(1,9999),new BigDecimal(numberUtils.random(300,99999)),getRandomNumericString(6));
      System.out.println(movTecnocom);
      Assert.assertNotNull("Respuesta no puede ser null",movTecnocom);
      Assert.assertEquals("Respuesta no puede ser != 0","0",movTecnocom.get("_error_code"));
      Assert.assertEquals("Respuesta debe ser vacio","",movTecnocom.get("_error_msg"));
      Assert.assertNotEquals("Id no puede ser 0",0,movTecnocom.get("_r_id"));
    }

    // Verifica que se insertaron los datos en la tabla prp_movimientos_tecnocom
    List<Map<String, Object>> rows = dbUtils.getJdbcTemplate().queryForList(String.format("select * from  %s.prp_movimientos_tecnocom", SCHEMA));
    Assert.assertNotNull("Rows no debe ser vacio",rows);
    Assert.assertEquals("El resultado debe ser ",3,rows.size());
    for(Map<String, Object> data: rows){
      Assert.assertNotNull("pan No debe ser null",data.get("pan"));
      Assert.assertNotNull("tipofac No debe ser null",data.get("tipofac"));
      Assert.assertNotNull("numaut No debe ser null",data.get("numaut"));
      Assert.assertNotNull("impfac No debe ser null",data.get("impfac"));
    }
    // Verifica que se insertaron los datos en la tabla prp_movimientos_tecnocom_hist
    List<Map<String, Object>> rowsHis = dbUtils.getJdbcTemplate().queryForList(String.format("select * from  %s.prp_movimientos_tecnocom_hist", SCHEMA));
    Assert.assertNotNull("Rows no debe ser vacio",rows);
    Assert.assertEquals("El resultado debe ser ",3,rows.size());

    for(Map<String, Object> data: rowsHis){
      Assert.assertNotNull("pan No debe ser null",data.get("pan"));
      Assert.assertNotNull("tipofac No debe ser null",data.get("tipofac"));
      Assert.assertNotNull("numaut No debe ser null",data.get("numaut"));
      Assert.assertNotNull("impfac No debe ser null",data.get("impfac"));
    }

    Map<String, Object> respEliminar = eliminaMovimientosTc(fileId);
    Assert.assertNotNull("Respuesta no puede ser null",respEliminar);
    Assert.assertEquals("Respuesta no puede ser != 0","0",respEliminar.get("_error_code"));
    Assert.assertEquals("Respuesta debe ser vacio","",respEliminar.get("_error_msg"));


    // Verifica que los movimientos ayan sido eliminados.
    rows = dbUtils.getJdbcTemplate().queryForList(String.format("select * from  %s.prp_movimientos_tecnocom", SCHEMA));
    Assert.assertNotNull("Rows no debe ser vacio",rows);
    Assert.assertEquals("El resultado debe ser ",0,rows.size());

    // Se verifica que la historia no se borra
    rowsHis = dbUtils.getJdbcTemplate().queryForList(String.format("select * from  %s.prp_movimientos_tecnocom_hist", SCHEMA));
    Assert.assertNotNull("Rows no debe ser vacio",rowsHis);
    Assert.assertEquals("El resultado debe ser ",3,rowsHis.size());

    for(Map<String, Object> data: rowsHis){
      Assert.assertNotNull("pan No debe ser null",data.get("pan"));
      Assert.assertNotNull("tipofac No debe ser null",data.get("tipofac"));
      Assert.assertNotNull("numaut No debe ser null",data.get("numaut"));
      Assert.assertNotNull("impfac No debe ser null",data.get("impfac"));
    }

  }

  @Test
  public void eliminarNoOK() throws Exception {
    Map<String, Object> resp = eliminaMovimientosTc(null);
    Assert.assertNotNull("Respuesta no puede ser null",resp);
    Assert.assertEquals("Debe ser MC001","MC001",resp.get("_error_code"));
    Assert.assertEquals("El mensaje debe ser igual a:","El _idArchivo es obligatorio",resp.get("_error_msg"));
  }

  public Map<String, Object> eliminaMovimientosTc(Long fileId)throws Exception{
    Object[] params = {
      new InParam(fileId, Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    return dbUtils.execute(SCHEMA + ".mc_prp_elimina_movimientos_tecnocom_v10", params);
  }

}
