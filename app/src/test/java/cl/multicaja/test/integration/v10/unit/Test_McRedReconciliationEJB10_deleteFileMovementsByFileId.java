package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test_McRedReconciliationEJB10_deleteFileMovementsByFileId extends TestBaseUnit {

  @Before
  @After
  public void beforeAndAfter() {
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento_switch cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_archivos_conciliacion cascade", getSchema()));
  }

  @Test
  public void deleteAllMovements_allOK() throws Exception {
    Map<String, Object> fileMap = Test_McRedReconciliationEJB10_addFileMovement.insertArchivoReconcialicionLog("archivo.txt", "SWITCH", "Retiros", "OK");
    Long fileId = numberUtils.toLong(fileMap.get("_r_id"));

    ArrayList<McRedReconciliationFileDetail> allInserted = new ArrayList<>();

    // Insertar 3 movimientos del mismo archivo
    McRedReconciliationFileDetail reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId, "MC23", 49L, 88L, new BigDecimal(1000), "03-02-1998 14:23");
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));
    reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId, "MC24", 50L, 89L, new BigDecimal(1001), "04-02-1998 16:23");
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));
    reconciliationMcRed10 = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId, "MC25", 51L, 90L, new BigDecimal(1002), "04-02-1999 18:23");
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, reconciliationMcRed10));

    // Insertar un segundo archivo con otro movimiento
    fileMap = Test_McRedReconciliationEJB10_addFileMovement.insertArchivoReconcialicionLog("archivo.txt", "SWITCH", "Retiros", "OK");
    Long fileId2 = numberUtils.toLong(fileMap.get("_r_id"));
    McRedReconciliationFileDetail movementFromSecondFile = Test_McRedReconciliationEJB10_addFileMovement.buildReconciliationMcRed10(fileId2, "MC26", 52L, 91L, new BigDecimal(1003), "05-02-1999 18:23");
    allInserted.add(getMcRedReconciliationEJBBean10().addFileMovement(null, movementFromSecondFile));

    // Borrar todos los del archivo 1
    getMcRedReconciliationEJBBean10().deleteFileMovementsByFileId(null, fileId);

    // No deben existir los del archivo 1
    List<McRedReconciliationFileDetail> mcRedReconciliationEJB10List = getMcRedReconciliationEJBBean10().getFileMovements(null, fileId, null, null);
    Assert.assertNull("No debe existir", mcRedReconciliationEJB10List);

    List<McRedReconciliationFileDetail> mcRedReconciliationEJB10List2 = getMcRedReconciliationEJBBean10().getFileMovements(null, fileId2, null, null);
    Assert.assertNotNull("Debe existir", mcRedReconciliationEJB10List2);
    Assert.assertEquals("Debe encontrar 1 movimiento", 1, mcRedReconciliationEJB10List2.size());

    McRedReconciliationFileDetail foundMovement = mcRedReconciliationEJB10List2.get(0);
    Assert.assertEquals("Debe tener mismo id", movementFromSecondFile.getId(), foundMovement.getId());
  }

  @Test(expected = BadRequestException.class)
  public void deleteAllMovements_null() throws Exception {
    getMcRedReconciliationEJBBean10().deleteFileMovementsByFileId(null, null);
  }
}
