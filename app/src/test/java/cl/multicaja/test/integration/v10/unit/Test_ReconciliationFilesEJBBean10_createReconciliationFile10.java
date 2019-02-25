package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.FileStatus;
import cl.multicaja.prepaid.model.v10.ReconciliationFile10;
import cl.multicaja.prepaid.model.v10.ReconciliationFileType;
import cl.multicaja.prepaid.model.v10.ReconciliationOriginType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class Test_ReconciliationFilesEJBBean10_createReconciliationFile10 extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_archivos_conciliacion", getSchema()));
  }

  @Test
  public void createFile_Ok() throws Exception {
    ReconciliationFile10 reconciliationFile10 = buildReconciliationFile();
    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

    Map<String, Object> dataFound = getReconciliationFile(reconciliationFile10.getFileName());

    Assert.assertNotNull("Debe tener un id asignado", dataFound.get("id"));
    Assert.assertEquals("Nombre de Archivo Encontrado igual a Nombre de Archivo registrado?", reconciliationFile10.getFileName(), dataFound.get("nombre_de_archivo"));
    Assert.assertEquals("Proceso Encontrado igual a Proceso registrado?", reconciliationFile10.getProcess().toString(), dataFound.get("proceso").toString());
    Assert.assertEquals("Tipo Encontrado igual a Tipo registrado?", reconciliationFile10.getType().toString(), dataFound.get("tipo").toString());
    Assert.assertEquals("Status igual a Status registrado?", reconciliationFile10.getStatus().toString(), dataFound.get("status").toString());
  }

  @Test(expected = BadRequestException.class)
  public void createFile_notOK() throws Exception {
    getReconciliationFilesEJBBean10().createReconciliationFile(null, null);
  }

  @Test(expected = BadRequestException.class)
  public void createFile_notOK_nullName() throws Exception {
    ReconciliationFile10 reconciliationFile10 = buildReconciliationFile();
    reconciliationFile10.setFileName(null);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);
  }

  @Test(expected = BadRequestException.class)
  public void createFile_notOK_nullProcess() throws Exception {
    ReconciliationFile10 reconciliationFile10 = buildReconciliationFile();
    reconciliationFile10.setProcess(null);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);
  }

  @Test(expected = BadRequestException.class)
  public void createFile_notOK_nullStatus() throws Exception {
    ReconciliationFile10 reconciliationFile10 = buildReconciliationFile();
    reconciliationFile10.setStatus(null);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);
  }

  @Test(expected = BadRequestException.class)
  public void createFile_notOK_nullType() throws Exception {
    ReconciliationFile10 reconciliationFile10 = buildReconciliationFile();
    reconciliationFile10.setType(null);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);
  }

  public static ReconciliationFile10 buildReconciliationFile() {
    ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
    reconciliationFile10.setFileName(getRandomString(10).concat(".txt"));
    reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);
    reconciliationFile10.setStatus(FileStatus.OK);
    reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
    return reconciliationFile10;
  }

  Map<String, Object> getReconciliationFile(String fileName) {
    String sql = "SELECT * FROM " + getSchema() + ".prp_archivos_conciliacion WHERE nombre_de_archivo = '" + fileName +"'";
    Map<String, Object> dataFound = getDbUtils().getJdbcTemplate().queryForMap(sql);
    return dataFound;
  }
}
