package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class Test_20190221092320_create_sp_prp_crea_movimiento_tecnocom_v10 extends TestDbBasePg {


  @BeforeClass
  @AfterClass
  public static void beforeClass() {
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimientos_tecnocom", SCHEMA));
    dbUtils.getJdbcTemplate().execute(String.format("delete from %s.prp_movimientos_tecnocom_hist", SCHEMA));
  }

  @Test
  public void creaMovimientoOK() throws Exception {
    Map<String, Object> conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId = numberUtils.toLong(conFile.get("_r_id"));

    Assert.assertNotNull("Id Archivo no puede ser nulo",fileId);

    for(int i=0;i <3 ;i++) {
      Map<String, Object> movTecnocom = creaMovimientoTecnocom(fileId,getRandomString(10),getRandomString(30),
        numberUtils.random(1,9999),new BigDecimal(numberUtils.random(300,99999)),getRandomNumericString(6));
      System.out.println(movTecnocom);
      Assert.assertNotNull("Respuesta no puede ser null",movTecnocom);
      Assert.assertEquals("Respuesta no puede ser != 0","0",movTecnocom.get("_error_code"));
      Assert.assertEquals("Respuesta no puede ser vacio","",movTecnocom.get("_error_msg"));
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
  public void creaMovimientoNoOk_f1() throws Exception{

    Map<String, Object> movTecnocom = creaMovimientoTecnocom(null,getRandomString(10),getRandomString(30),
      numberUtils.random(1,9999),new BigDecimal(numberUtils.random(300,99999)),getRandomNumericString(6));
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null",movTecnocom);
    Assert.assertEquals("Debe ser MC001","MC001",movTecnocom.get("_error_code"));
    Assert.assertEquals("El mensaje debe ser igual a:","El _idArchivo es obligatorio",movTecnocom.get("_error_msg"));
    Assert.assertEquals("Id debe ser 0",0L,movTecnocom.get("_r_id"));
  }
  @Test
  public void creaMovimientoNoOk_f2() throws Exception{
    Map<String, Object> conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId = numberUtils.toLong(conFile.get("_r_id"));

    Assert.assertNotNull("Id Archivo no puede ser nulo",fileId);

    Map<String, Object> movTecnocom = creaMovimientoTecnocom(fileId,null,getRandomString(30),
      numberUtils.random(1,9999),new BigDecimal(numberUtils.random(300,99999)),getRandomNumericString(6));
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null",movTecnocom);
    Assert.assertEquals("Debe ser MC001","MC002",movTecnocom.get("_error_code"));
    Assert.assertEquals("El mensaje debe ser igual a:","El _cuenta es obligatorio",movTecnocom.get("_error_msg"));
    Assert.assertEquals("Id debe ser 0",0L,movTecnocom.get("_r_id"));
  }
  @Test
  public void creaMovimientoNoOk_f3() throws Exception{
    Map<String, Object> conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId = numberUtils.toLong(conFile.get("_r_id"));

    Assert.assertNotNull("Id Archivo no puede ser nulo",fileId);

    Map<String, Object> movTecnocom = creaMovimientoTecnocom(fileId,getRandomString(10),null,
      numberUtils.random(1,9999),new BigDecimal(numberUtils.random(300,99999)),getRandomNumericString(6));
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null",movTecnocom);
    Assert.assertEquals("Debe ser MC001","MC003",movTecnocom.get("_error_code"));
    Assert.assertEquals("El mensaje debe ser igual a:","El _pan es obligatorio",movTecnocom.get("_error_msg"));
    Assert.assertEquals("Id debe ser 0",0L,movTecnocom.get("_r_id"));
  }
  @Test
  public void creaMovimientoNoOk_f4() throws Exception{
    Map<String, Object> conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId = numberUtils.toLong(conFile.get("_r_id"));

    Assert.assertNotNull("Id Archivo no puede ser nulo",fileId);

    Map<String, Object> movTecnocom = creaMovimientoTecnocom(fileId,getRandomString(10),getRandomString(30),
      null,new BigDecimal(numberUtils.random(300,99999)),getRandomNumericString(6));
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null",movTecnocom);
    Assert.assertEquals("Debe ser MC001","MC004",movTecnocom.get("_error_code"));
    Assert.assertEquals("El mensaje debe ser igual a:","El _tipofac es obligatorio",movTecnocom.get("_error_msg"));
    Assert.assertEquals("Id debe ser 0",0L,movTecnocom.get("_r_id"));
  }
  @Test
  public void creaMovimientoNoOk_f5() throws Exception{
    Map<String, Object> conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId = numberUtils.toLong(conFile.get("_r_id"));

    Assert.assertNotNull("Id Archivo no puede ser nulo",fileId);

    Map<String, Object> movTecnocom = creaMovimientoTecnocom(fileId,getRandomString(10),getRandomString(30),
      numberUtils.random(1,9999),null,getRandomNumericString(6));
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null",movTecnocom);
    Assert.assertEquals("Debe ser MC001","MC005",movTecnocom.get("_error_code"));
    Assert.assertEquals("El mensaje debe ser igual a:","El _impfac es obligatorio",movTecnocom.get("_error_msg"));
    Assert.assertEquals("Id debe ser 0",0L,movTecnocom.get("_r_id"));
  }
  @Test
  public void creaMovimientoNoOk_f6() throws Exception{
    Map<String, Object> conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId = numberUtils.toLong(conFile.get("_r_id"));

    Assert.assertNotNull("Id Archivo no puede ser nulo",fileId);

    Map<String, Object> movTecnocom = creaMovimientoTecnocom(fileId,getRandomString(10),getRandomString(30),
      numberUtils.random(1,9999),new BigDecimal(numberUtils.random(300,99999)),null);
    System.out.println(movTecnocom);
    Assert.assertNotNull("Respuesta no puede ser null",movTecnocom);
    Assert.assertEquals("Debe ser MC001","MC006",movTecnocom.get("_error_code"));
    Assert.assertEquals("El mensaje debe ser igual a:","El _numaut es obligatorio",movTecnocom.get("_error_msg"));
    Assert.assertEquals("Id debe ser 0",0L,movTecnocom.get("_r_id"));
  }



  public static Map<String, Object>  creaMovimientoTecnocom(Long fileId, String cuenta, String pan, Integer tipoFac, BigDecimal impFac, String numAut)throws Exception{
    Object[] params = {
      new InParam(fileId,Types.BIGINT),
      new InParam(cuenta,Types.VARCHAR), // Cuenta
      new InParam(pan,Types.VARCHAR), // PAN encriptado
      new InParam(getRandomString(4),Types.VARCHAR),// COD ENT
      new InParam(getRandomString(4),Types.VARCHAR), // CENALTA
      new InParam(numberUtils.random(1,999),Types.NUMERIC),//clamon
      new InParam(numberUtils.random(1,9),Types.NUMERIC),//indnorcor
      new InParam(tipoFac,Types.NUMERIC),//tipofac
      new InParam(new Date(System.currentTimeMillis()),Types.DATE),//tipofac
      new InParam(getRandomString(23),Types.VARCHAR), // numreffac
      new InParam(numberUtils.random(1,999),Types.NUMERIC),//clamondiv
      new InParam(numberUtils.random(3000,9999),Types.NUMERIC),//impdiv
      new InParam(impFac,Types.NUMERIC),//impfac
      new InParam(numberUtils.random(3000,9999),Types.NUMERIC),//cmbapli
      new InParam(numAut,Types.VARCHAR), // numaut
      new InParam(getRandomString(1),Types.VARCHAR), // indproaje
      new InParam(getRandomString(15),Types.VARCHAR),//codcom
      new InParam(numberUtils.random(3000,9999),Types.NUMERIC),//codact
      new InParam(numberUtils.random(3000,9999),Types.NUMERIC),//impliq
      new InParam(numberUtils.random(1,999),Types.NUMERIC),//clamonliq
      new InParam(numberUtils.random(1,999),Types.NUMERIC),//codpais
      new InParam(getRandomString(26),Types.VARCHAR),//nompob
      new InParam(numberUtils.random(1,999),Types.NUMERIC),//numextcta
      new InParam(numberUtils.random(3000,9999),Types.NUMERIC),//nummovext
      new InParam(numberUtils.random(1,999),Types.NUMERIC),//clamone
      new InParam(getRandomString(4),Types.VARCHAR),//tipolin
      new InParam(numberUtils.random(3000,9999),Types.NUMERIC),//linref
      new InParam(new Timestamp(System.currentTimeMillis()),Types.TIMESTAMP),
      new InParam(numberUtils.random(3000,9999),Types.NUMERIC),
      new InParam("ONLI",Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    return dbUtils.execute(SCHEMA + ".mc_prp_crea_movimiento_tecnocom_v10", params);
  }

}
