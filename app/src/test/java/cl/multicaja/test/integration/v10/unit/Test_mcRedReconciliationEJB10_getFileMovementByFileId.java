package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import org.junit.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_mcRedReconciliationEJB10_getFileMovementByFileId extends TestBaseUnit {

  @Before
  @After
  public void beforeAndAfter() {
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento_switch cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_archivos_conciliacion cascade", getSchema()));
  }

  @Test
  public void getReconciliationFile_allOK() throws Exception {
    Map<String, Object> fileMap = Test_McRedReconciliationEJB10_addFileMovement.insertArchivoReconcialicionLog("archivo.txt", "SWITCH", "Retiros", "OK");
    Long fileId = numberUtils.toLong(fileMap.get("_r_id"));

    ArrayList<McRedReconciliationFileDetail> allInserted = new ArrayList<>();
    System.out.println( new Timestamp(System.currentTimeMillis()));
    McRedReconciliationFileDetail reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId, "MC23", 49L, 88L, new BigDecimal(1000), new Timestamp(System.currentTimeMillis()));
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));

    reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId, "MC24", 50L, 89L, new BigDecimal(1001), new Timestamp(System.currentTimeMillis()));
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));

    McRedReconciliationFileDetail insertedMovement = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId, "MC25", 51L, 90L, new BigDecimal(1002), new Timestamp(System.currentTimeMillis()));
    insertedMovement = getMcRedReconciliationEJBBean10().addFileMovement(null, insertedMovement);
    allInserted.add(insertedMovement);

    fileMap = Test_McRedReconciliationEJB10_addFileMovement.insertArchivoReconcialicionLog("archivo.txt", "SWITCH", "Retiros", "OK");
    Long fileId2 = numberUtils.toLong(fileMap.get("_r_id"));
    reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId2, "MC26", 52L, 91L, new BigDecimal(1003), new Timestamp(System.currentTimeMillis()));
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));

    // busca por file id
    {
      List<McRedReconciliationFileDetail> foundMovements = getMcRedReconciliationEJBBean10().getFileMovements(null, fileId, null, null);

      Assert.assertNotNull("Debe existir", foundMovements);
      Assert.assertEquals("Debe tener largo 3", 3, foundMovements.size());

      int comparedMovements = 0;
      for (McRedReconciliationFileDetail createdMovement : allInserted) { // Por cada movimiento insertado
        for (McRedReconciliationFileDetail foundMovement : foundMovements) { // Por cada movimiento encontrado
          if (foundMovement.getId().equals(createdMovement.getId())) { // Buscar si tienen el mismo id
            Assert.assertEquals("Debe tener mismo archivo_id", fileId, foundMovement.getFileId());
            Assert.assertEquals("Debe tener mismo multicaja id", createdMovement.getMcCode(), foundMovement.getMcCode());
            Assert.assertEquals("Debe tener mismo cliente_id", createdMovement.getClientId(), foundMovement.getClientId());
            Assert.assertEquals("Debe tener mismo id_multicaja_ref", createdMovement.getExternalId(), foundMovement.getExternalId());
            Assert.assertEquals("Debe tener mismo monto", createdMovement.getAmount().stripTrailingZeros(), foundMovement.getAmount().stripTrailingZeros());
            Assert.assertEquals("Debe tener misma fecha", createdMovement.getDateTrx(), foundMovement.getDateTrx());

            comparedMovements++;
          }
        }
      }
      Assert.assertEquals("Debe comparar 3 elementos", 3, comparedMovements);

    }

    // Busca por id
    {
      List<McRedReconciliationFileDetail> foundMovements = getMcRedReconciliationEJBBean10().getFileMovements(null, null, insertedMovement.getId(), null);

      Assert.assertNotNull("Debe existir", foundMovements);
      Assert.assertEquals("Debe tener largo 1", 1, foundMovements.size());

      McRedReconciliationFileDetail foundMovement = foundMovements.get(0);
      Assert.assertEquals("Debe tener mismo archivo_id", fileId, foundMovement.getFileId());
      Assert.assertEquals("Debe tener mismo multicaja id", insertedMovement.getMcCode(), foundMovement.getMcCode());
      Assert.assertEquals("Debe tener mismo cliente_id", insertedMovement.getClientId(), foundMovement.getClientId());
      Assert.assertEquals("Debe tener mismo id_multicaja_ref", insertedMovement.getExternalId(), foundMovement.getExternalId());
      Assert.assertEquals("Debe tener mismo monto", insertedMovement.getAmount().stripTrailingZeros(), foundMovement.getAmount().stripTrailingZeros());
      Assert.assertEquals("Debe tener misma fecha", insertedMovement.getDateTrx(), foundMovement.getDateTrx());
    }

    // Busca por id multicaja
    {
      List<McRedReconciliationFileDetail> foundMovements = getMcRedReconciliationEJBBean10().getFileMovements(null, null, null, insertedMovement.getMcCode());

      Assert.assertNotNull("Debe existir", foundMovements);
      Assert.assertEquals("Debe tener largo 1", 1, foundMovements.size());

      McRedReconciliationFileDetail foundMovement = foundMovements.get(0);
      Assert.assertEquals("Debe tener mismo archivo_id", fileId, foundMovement.getFileId());
      Assert.assertEquals("Debe tener mismo multicaja id", insertedMovement.getMcCode(), foundMovement.getMcCode());
      Assert.assertEquals("Debe tener mismo cliente_id", insertedMovement.getClientId(), foundMovement.getClientId());
      Assert.assertEquals("Debe tener mismo id_multicaja_ref", insertedMovement.getExternalId(), foundMovement.getExternalId());
      Assert.assertEquals("Debe tener mismo monto", insertedMovement.getAmount().stripTrailingZeros(), foundMovement.getAmount().stripTrailingZeros());
      Assert.assertEquals("Debe tener misma fecha", insertedMovement.getDateTrx(), foundMovement.getDateTrx());
    }
  }

  @Test
  public void getReconciliationFile_AllNull() throws Exception {
    Map<String, Object> fileMap = Test_McRedReconciliationEJB10_addFileMovement.insertArchivoReconcialicionLog("archivo.txt", "SWITCH", "Retiros", "OK");
    Long fileId = numberUtils.toLong(fileMap.get("_r_id"));

    ArrayList<McRedReconciliationFileDetail> allInserted = new ArrayList<>();
    McRedReconciliationFileDetail reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId, "MC23", 49L, 88L, new BigDecimal(1000), new Timestamp(System.currentTimeMillis()));
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));

    reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId, "MC24", 50L, 89L, new BigDecimal(1001), new Timestamp(System.currentTimeMillis()));
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));

    reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId, "MC25", 51L, 90L, new BigDecimal(1002), new Timestamp(System.currentTimeMillis()));
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));

    fileMap = Test_McRedReconciliationEJB10_addFileMovement.insertArchivoReconcialicionLog("archivo.txt", "SWITCH", "Retiros", "OK");
    Long fileId2 = numberUtils.toLong(fileMap.get("_r_id"));
    reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId2, "MC26", 52L, 91L, new BigDecimal(1003), new Timestamp(System.currentTimeMillis()));
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));

    List<McRedReconciliationFileDetail> foundMovements = getMcRedReconciliationEJBBean10().getFileMovements(null, null, null, null);

    Assert.assertNotNull("Debe existir", foundMovements);
    Assert.assertEquals("Debe tener largo 4", 4, foundMovements.size());

    int comparedMovements = 0;
    for (McRedReconciliationFileDetail createdMovement : allInserted) { // Por cada movimiento insertado
      for (McRedReconciliationFileDetail foundMovement : foundMovements) { // Por cada movimiento encontrado
        if (foundMovement.getId().equals(createdMovement.getId())) { // Buscar si tienen el mismo id
          Assert.assertEquals("Debe tener mismo archivo_id", createdMovement.getFileId(), foundMovement.getFileId());
          Assert.assertEquals("Debe tener mismo multicaja id", createdMovement.getMcCode(), foundMovement.getMcCode());
          Assert.assertEquals("Debe tener mismo cliente_id", createdMovement.getClientId(), foundMovement.getClientId());
          Assert.assertEquals("Debe tener mismo id_multicaja_ref", createdMovement.getExternalId(), foundMovement.getExternalId());
          Assert.assertEquals("Debe tener mismo monto", createdMovement.getAmount().stripTrailingZeros(), foundMovement.getAmount().stripTrailingZeros());
          Assert.assertEquals("Debe tener misma fecha", createdMovement.getDateTrx(), foundMovement.getDateTrx());

          comparedMovements++;
        }
      }
    }
    Assert.assertEquals("Debe comparar 4 elementos", 4, comparedMovements);
  }
}
