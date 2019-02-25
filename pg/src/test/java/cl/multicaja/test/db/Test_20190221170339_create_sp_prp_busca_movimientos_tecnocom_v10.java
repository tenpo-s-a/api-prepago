package cl.multicaja.test.db;

import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.test.TestDbBasePg;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class Test_20190221170339_create_sp_prp_busca_movimientos_tecnocom_v10 extends TestDbBasePg {

  private static final String SP_NAME = SCHEMA + ".prp_busca_movimientos_tecnocom_v10";

  @Test
  public void testBuscaMovimientosTecnocom() throws Exception{
    Map<String, Object> conFile = Test_20190219142005_create_sp_insert_intermediate_reconciliation_files.insertArchivoReconcialicionLog(getRandomNumericString(10),getRandomNumericString(10),getRandomNumericString(10),"PENDING");
    Long fileId = numberUtils.toLong(conFile.get("_r_id"));

    Assert.assertNotNull("Id Archivo no puede ser nulo",fileId);

    for(int i=0;i <3 ;i++) {
      Map<String, Object> movTecnocom = Test_20190221092320_create_sp_prp_crea_movimiento_tecnocom_v10.creaMovimientoTecnocom(fileId,getRandomString(10),getRandomString(30),
        numberUtils.random(1,9999),new BigDecimal(numberUtils.random(300,99999)),getRandomNumericString(6));
      System.out.println(movTecnocom);
      Assert.assertNotNull("Respuesta no puede ser null",movTecnocom);
      Assert.assertEquals("Respuesta no puede ser != 0","0",movTecnocom.get("_error_code"));
      Assert.assertEquals("Respuesta no puede ser vacio","",movTecnocom.get("_error_msg"));
      Assert.assertNotEquals("Id no puede ser 0",0,movTecnocom.get("_r_id"));
    }

    Map<String, Object> searchResult =  buildQuery(fileId);
    Assert.assertNotNull("El resultado no debe ser null",searchResult);
    List result = (List)searchResult.get("result");
    Assert.assertEquals("EL resultado debe ser 3",3,result.size());
    for(Object data : result){
      System.out.println(data);
    }
  }

  private static Map<String, Object> buildQuery(Long fileId) throws Exception {

    Object[] params = {
      new InParam(fileId, Types.BIGINT),
    };
    return dbUtils.execute(SP_NAME, params);
  }

}
