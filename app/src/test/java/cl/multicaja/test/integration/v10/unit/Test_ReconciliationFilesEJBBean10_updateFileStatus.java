package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.FileStatus;
import cl.multicaja.prepaid.model.v10.ReconciliationFile10;
import cl.multicaja.prepaid.model.v10.ReconciliationFileType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class Test_ReconciliationFilesEJBBean10_updateFileStatus extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_archivos_conciliacion", getSchema()));
  }

  @Test
  public void updateFilesStatus_ok() throws Exception {
    ReconciliationFile10 reconciliationFile10 = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    reconciliationFile10.setStatus(FileStatus.READING);
    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

    ReconciliationFile10 anotherFile = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    anotherFile.setStatus(FileStatus.READING);
    anotherFile = getReconciliationFilesEJBBean10().createReconciliationFile(null, anotherFile);

    getReconciliationFilesEJBBean10().updateFileStatus(null, reconciliationFile10.getId(), FileStatus.OK);

    List<ReconciliationFile10> reconciliationFile10List = getReconciliationFilesEJBBean10().getReconciliationFile(null, null, reconciliationFile10.getFileName(), null, null, null);
    ReconciliationFile10 foundFile = reconciliationFile10List.get(0);

    Assert.assertEquals("Debe tener estado OK", FileStatus.OK, foundFile.getStatus());

    reconciliationFile10List = getReconciliationFilesEJBBean10().getReconciliationFile(null, null, anotherFile.getFileName(), null, null, null);
    foundFile = reconciliationFile10List.get(0);

    Assert.assertEquals("No debe haber cambiado", FileStatus.READING, foundFile.getStatus());
  }

  @Test(expected = BadRequestException.class)
  public void updateFileStatus_nullId() throws Exception {
    getReconciliationFilesEJBBean10().updateFileStatus(null, null, FileStatus.OK);
  }

  @Test(expected = BadRequestException.class)
  public void updateFileStatus_nullStatus() throws Exception {
    getReconciliationFilesEJBBean10().updateFileStatus(null, 45L, null);
  }
}
