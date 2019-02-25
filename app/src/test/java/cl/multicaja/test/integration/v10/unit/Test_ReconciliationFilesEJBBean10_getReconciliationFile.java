package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.FileStatus;
import cl.multicaja.prepaid.model.v10.ReconciliationFile10;
import cl.multicaja.prepaid.model.v10.ReconciliationFileType;
import cl.multicaja.prepaid.model.v10.ReconciliationOriginType;
import cl.multicaja.tecnocom.constants.TipoFactura;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Test_ReconciliationFilesEJBBean10_getReconciliationFile extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("DELETE FROM %s.prp_archivos_conciliacion", getSchema()));
  }

  @Test
  public void getReconciliationFile_byName() throws Exception {
    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    int currentDay = ldt.getDayOfYear();

    ReconciliationFile10 reconciliationFile10 = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

    ReconciliationFile10 anotherFile = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    anotherFile.setFileName("otro_nombre.txt");
    getReconciliationFilesEJBBean10().createReconciliationFile(null, anotherFile);

    List<ReconciliationFile10> reconciliationFile10List = getReconciliationFilesEJBBean10().getReconciliationFile(null, reconciliationFile10.getFileName(), null, null, null);
    Assert.assertNotNull("Debe existir", reconciliationFile10List);
    Assert.assertEquals("Debe tener 1 elemento", 1, reconciliationFile10List.size());

    ReconciliationFile10 foundFile = reconciliationFile10List.get(0);
    Assert.assertEquals("Deben tener mismo id", reconciliationFile10.getId(), foundFile.getId());
    Assert.assertEquals("Nombre de Archivo Encontrado igual a Nombre de Archivo registrado?", reconciliationFile10.getFileName(), foundFile.getFileName());
    Assert.assertEquals("Proceso Encontrado igual a Proceso registrado?", reconciliationFile10.getProcess(), foundFile.getProcess());
    Assert.assertEquals("Tipo Encontrado igual a Tipo registrado?", reconciliationFile10.getType(), foundFile.getType());
    Assert.assertEquals("Status igual a Status registrado?", reconciliationFile10.getStatus(), foundFile.getStatus());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getCreatedAt().toLocalDateTime().getDayOfYear());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getUpdatedAt().toLocalDateTime().getDayOfYear());
  }

  @Test
  public void getReconciliationFile_byProcess() throws Exception {
    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    int currentDay = ldt.getDayOfYear();

    ReconciliationFile10 reconciliationFile10 = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

    ReconciliationFile10 anotherFile = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    anotherFile.setProcess(ReconciliationOriginType.TECNOCOM);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, anotherFile);

    List<ReconciliationFile10> reconciliationFile10List = getReconciliationFilesEJBBean10().getReconciliationFile(null, null, ReconciliationOriginType.SWITCH, null, null);
    Assert.assertNotNull("Debe existir", reconciliationFile10List);
    Assert.assertEquals("Debe tener 1 elemento", 1, reconciliationFile10List.size());

    ReconciliationFile10 foundFile = reconciliationFile10List.get(0);
    Assert.assertEquals("Deben tener mismo id", reconciliationFile10.getId(), foundFile.getId());
    Assert.assertEquals("Nombre de Archivo Encontrado igual a Nombre de Archivo registrado?", reconciliationFile10.getFileName(), foundFile.getFileName());
    Assert.assertEquals("Proceso Encontrado igual a Proceso registrado?", reconciliationFile10.getProcess(), foundFile.getProcess());
    Assert.assertEquals("Tipo Encontrado igual a Tipo registrado?", reconciliationFile10.getType(), foundFile.getType());
    Assert.assertEquals("Status igual a Status registrado?", reconciliationFile10.getStatus(), foundFile.getStatus());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getCreatedAt().toLocalDateTime().getDayOfYear());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getUpdatedAt().toLocalDateTime().getDayOfYear());
  }

  @Test
  public void getReconciliationFile_byFileType() throws Exception {
    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    int currentDay = ldt.getDayOfYear();

    ReconciliationFile10 reconciliationFile10 = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

    ReconciliationFile10 anotherFile = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    anotherFile.setType(ReconciliationFileType.SWITCH_TOPUP);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, anotherFile);

    ReconciliationFile10 thirdFile = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    thirdFile.setType(ReconciliationFileType.TECNOCOM_FILE);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, thirdFile);

    List<ReconciliationFile10> reconciliationFile10List = getReconciliationFilesEJBBean10().getReconciliationFile(null, null, null, ReconciliationFileType.SWITCH_TOPUP, null);
    Assert.assertNotNull("Debe existir", reconciliationFile10List);
    Assert.assertEquals("Debe tener 2 elementos", 2, reconciliationFile10List.size());

    // Chequea que el primero se haya encontrado
    List<ReconciliationFile10> filteredFiles = reconciliationFile10List.stream().filter(file -> (file.getId().equals(reconciliationFile10.getId()))).collect(Collectors.toList());
    ReconciliationFile10 foundFile = filteredFiles.get(0);
    Assert.assertEquals("Deben tener mismo id", reconciliationFile10.getId(), foundFile.getId());
    Assert.assertEquals("Nombre de Archivo Encontrado igual a Nombre de Archivo registrado?", reconciliationFile10.getFileName(), foundFile.getFileName());
    Assert.assertEquals("Proceso Encontrado igual a Proceso registrado?", reconciliationFile10.getProcess(), foundFile.getProcess());
    Assert.assertEquals("Tipo Encontrado igual a Tipo registrado?", reconciliationFile10.getType(), foundFile.getType());
    Assert.assertEquals("Status igual a Status registrado?", reconciliationFile10.getStatus(), foundFile.getStatus());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getCreatedAt().toLocalDateTime().getDayOfYear());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getUpdatedAt().toLocalDateTime().getDayOfYear());

    // Chequea que el segundo se haya encontrado
    List<ReconciliationFile10> filteredFiles2 = reconciliationFile10List.stream().filter(file -> (file.getId().equals(anotherFile.getId()))).collect(Collectors.toList());
    foundFile = filteredFiles2.get(0);
    Assert.assertEquals("Deben tener mismo id", anotherFile.getId(), foundFile.getId());
    Assert.assertEquals("Nombre de Archivo Encontrado igual a Nombre de Archivo registrado?", anotherFile.getFileName(), foundFile.getFileName());
    Assert.assertEquals("Proceso Encontrado igual a Proceso registrado?", anotherFile.getProcess(), foundFile.getProcess());
    Assert.assertEquals("Tipo Encontrado igual a Tipo registrado?", anotherFile.getType(), foundFile.getType());
    Assert.assertEquals("Status igual a Status registrado?", anotherFile.getStatus(), foundFile.getStatus());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getCreatedAt().toLocalDateTime().getDayOfYear());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getUpdatedAt().toLocalDateTime().getDayOfYear());
  }

  @Test
  public void getReconciliationFile_byFileStatus() throws Exception {
    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    int currentDay = ldt.getDayOfYear();

    ReconciliationFile10 reconciliationFile10 = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    reconciliationFile10.setStatus(FileStatus.OK);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

    ReconciliationFile10 anotherFile = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    anotherFile.setStatus(FileStatus.OK);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, anotherFile);

    ReconciliationFile10 thirdFile = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    thirdFile.setStatus(FileStatus.READING);
    getReconciliationFilesEJBBean10().createReconciliationFile(null, thirdFile);

    List<ReconciliationFile10> reconciliationFile10List = getReconciliationFilesEJBBean10().getReconciliationFile(null, null, null, null, FileStatus.READING);
    Assert.assertNotNull("Debe existir", reconciliationFile10List);
    Assert.assertEquals("Debe tener 1 elemento", 1, reconciliationFile10List.size());

    ReconciliationFile10 foundFile = reconciliationFile10List.get(0);
    Assert.assertEquals("Deben tener mismo id", thirdFile.getId(), foundFile.getId());
    Assert.assertEquals("Nombre de Archivo Encontrado igual a Nombre de Archivo registrado?", thirdFile.getFileName(), foundFile.getFileName());
    Assert.assertEquals("Proceso Encontrado igual a Proceso registrado?", thirdFile.getProcess(), foundFile.getProcess());
    Assert.assertEquals("Tipo Encontrado igual a Tipo registrado?", thirdFile.getType(), foundFile.getType());
    Assert.assertEquals("Status igual a Status registrado?", thirdFile.getStatus(), foundFile.getStatus());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getCreatedAt().toLocalDateTime().getDayOfYear());
    Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getUpdatedAt().toLocalDateTime().getDayOfYear());
  }

  @Test
  public void getReconciliationFile_allNull() throws Exception {
    LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    int currentDay = ldt.getDayOfYear();

    ArrayList<ReconciliationFile10> allInsertedFiles = new ArrayList<>();

    ReconciliationFile10 reconciliationFile10 = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    reconciliationFile10.setStatus(FileStatus.OK);
    allInsertedFiles.add(getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10));

    ReconciliationFile10 anotherFile = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    anotherFile.setStatus(FileStatus.OK);
    allInsertedFiles.add(getReconciliationFilesEJBBean10().createReconciliationFile(null, anotherFile));

    ReconciliationFile10 thirdFile = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    thirdFile.setStatus(FileStatus.READING);
    allInsertedFiles.add(getReconciliationFilesEJBBean10().createReconciliationFile(null, thirdFile));

    List<ReconciliationFile10> reconciliationFile10List = getReconciliationFilesEJBBean10().getReconciliationFile(null, null, null, null, null);
    Assert.assertNotNull("Debe existir", reconciliationFile10List);
    Assert.assertEquals("Debe tener 3 elemento", 3, reconciliationFile10List.size());

    int comparedFiles = 0;
    for(ReconciliationFile10 insertedFile : allInsertedFiles) {
      for(ReconciliationFile10 foundFile : reconciliationFile10List) {
        if(insertedFile.getId().equals(foundFile.getId())) {
          Assert.assertEquals("Nombre de Archivo Encontrado igual a Nombre de Archivo registrado?", insertedFile.getFileName(), foundFile.getFileName());
          Assert.assertEquals("Proceso Encontrado igual a Proceso registrado?", insertedFile.getProcess(), foundFile.getProcess());
          Assert.assertEquals("Tipo Encontrado igual a Tipo registrado?", insertedFile.getType(), foundFile.getType());
          Assert.assertEquals("Status igual a Status registrado?", insertedFile.getStatus(), foundFile.getStatus());
          Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getCreatedAt().toLocalDateTime().getDayOfYear());
          Assert.assertEquals("Debe tener create date asignada", currentDay, foundFile.getTimestamps().getUpdatedAt().toLocalDateTime().getDayOfYear());
          comparedFiles++;
        }
      }
    }
    Assert.assertEquals("Deben haberse comparado 3 archivos", 3, comparedFiles);
  }
}
