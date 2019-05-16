package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import org.junit.*;

import java.util.ArrayList;

public class Test_PrepaidMovementEJBBean10_expireNotReconciledMovements extends TestBaseUnit {

  @Before
  @After
  public void beforeClass() {
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_movimiento cascade", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_archivos_conciliacion cascade", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void expireTecnocomMovements_nullFile() throws Exception {
    getPrepaidMovementEJBBean10().expireNotReconciledMovements(null);
  }

  @Test
  public void expire_rejectedTopups() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    ArrayList<PrepaidMovement10> topupMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> topupReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> reconciledMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
      reconciliationFile10.setStatus(FileStatus.OK);
      reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);

      reconciliationFile10.setFileName(String.format("archivo%d_1.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_2.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_3.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_4.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_5.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_6.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_7.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      reconciledMovements.add(0, prepaidMovement10);

      Thread.sleep(10);
    }

    getPrepaidMovementEJBBean10().expireNotReconciledMovements(ReconciliationFileType.SWITCH_REJECTED_TOPUP);

    // Nada debe cambiar
    for(int i = 0; i < topupMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < topupReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < reconciledMovements.size(); i++) {
      PrepaidMovement10 originalMovement = reconciledMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConSwitch());
    }
  }

  @Test
  public void expire_rejectedWithdraw() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    ArrayList<PrepaidMovement10> topupMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> topupReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> reconciledMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
      reconciliationFile10.setStatus(FileStatus.OK);
      reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);

      reconciliationFile10.setFileName(String.format("archivo%d_1.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_2.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_3.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_4.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_5.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_6.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_7.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      reconciledMovements.add(0, prepaidMovement10);

      Thread.sleep(10);
    }

    getPrepaidMovementEJBBean10().expireNotReconciledMovements(ReconciliationFileType.SWITCH_REJECTED_WITHDRAW);

    // Nada debe cambiar
    for(int i = 0; i < topupMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < topupReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < reconciledMovements.size(); i++) {
      PrepaidMovement10 originalMovement = reconciledMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConSwitch());
    }
  }

  @Test
  public void expire_topup_ok() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    ArrayList<PrepaidMovement10> topupMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> topupReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> reconciledMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
      reconciliationFile10.setStatus(FileStatus.OK);
      reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);

      reconciliationFile10.setFileName(String.format("archivo%d_1.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_2.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_3.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_4.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_5.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_6.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_7.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      reconciledMovements.add(0, prepaidMovement10);

      Thread.sleep(10);
    }

    getPrepaidMovementEJBBean10().expireNotReconciledMovements(ReconciliationFileType.SWITCH_TOPUP);

    // Cambian los mas antiguos
    for(int i = 0; i < topupMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());

      if(i <= 1) {
        Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
      } else {
        Assert.assertEquals("Deben tener estado NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, storedMovement.getConSwitch());
      }
    }

    // Nada debe cambiar
    for(int i = 0; i < topupReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < reconciledMovements.size(); i++) {
      PrepaidMovement10 originalMovement = reconciledMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConSwitch());
    }
  }

  @Test
  public void expire_topup_files_notOK() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    ArrayList<PrepaidMovement10> topupMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> topupReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> reconciledMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
      reconciliationFile10.setStatus(FileStatus.READING); // Archivos no listos
      reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);

      reconciliationFile10.setFileName(String.format("archivo%d_1.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_2.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_3.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_4.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_5.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_6.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_7.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      reconciledMovements.add(0, prepaidMovement10);

      Thread.sleep(10);
    }

    getPrepaidMovementEJBBean10().expireNotReconciledMovements(ReconciliationFileType.SWITCH_TOPUP);

    // Nada cambia debido a que los archivos no estan con status OK
    for(int i = 0; i < topupMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < topupReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < reconciledMovements.size(); i++) {
      PrepaidMovement10 originalMovement = reconciledMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConSwitch());
    }
  }

  @Test
  public void expire_topup_reversed() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    ArrayList<PrepaidMovement10> topupMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> topupReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> reconciledMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
      reconciliationFile10.setStatus(FileStatus.OK);
      reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);

      reconciliationFile10.setFileName(String.format("archivo%d_1.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_2.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_3.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_4.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_5.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_6.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_7.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      reconciledMovements.add(0, prepaidMovement10);

      Thread.sleep(10);
    }

    getPrepaidMovementEJBBean10().expireNotReconciledMovements(ReconciliationFileType.SWITCH_REVERSED_TOPUP);

    // Nada debe cambiar
    for(int i = 0; i < topupMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Cambian los mas antiguos
    for(int i = 0; i < topupReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      if(i <= 1) {
        Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
      } else {
        Assert.assertEquals("Deben tener estado NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, storedMovement.getConSwitch());
      }
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < reconciledMovements.size(); i++) {
      PrepaidMovement10 originalMovement = reconciledMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConSwitch());
    }
  }

  @Test
  public void expire_withdraw_ok() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    ArrayList<PrepaidMovement10> topupMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> topupReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> reconciledMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
      reconciliationFile10.setStatus(FileStatus.OK);
      reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);

      reconciliationFile10.setFileName(String.format("archivo%d_1.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_2.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_3.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_4.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_5.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_6.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_7.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      reconciledMovements.add(0, prepaidMovement10);

      Thread.sleep(10);
    }

    getPrepaidMovementEJBBean10().expireNotReconciledMovements(ReconciliationFileType.SWITCH_WITHDRAW);

    // Nada debe cambiar
    for(int i = 0; i < topupMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < topupReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Cambian los mas antiguos
    for(int i = 0; i < withdrawMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      if(i <= 1) {
        Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
      } else {
        Assert.assertEquals("Deben tener estado NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, storedMovement.getConSwitch());
      }
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < reconciledMovements.size(); i++) {
      PrepaidMovement10 originalMovement = reconciledMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConSwitch());
    }
  }

  @Test
  public void expire_withdraw_reversed() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    ArrayList<PrepaidMovement10> topupMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> topupReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> reconciledMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
      reconciliationFile10.setStatus(FileStatus.OK);
      reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);

      reconciliationFile10.setFileName(String.format("archivo%d_1.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_2.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_3.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_4.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_5.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_6.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_7.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      reconciledMovements.add(0, prepaidMovement10);

      Thread.sleep(10);
    }

    getPrepaidMovementEJBBean10().expireNotReconciledMovements(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);

    // Nada debe cambiar
    for(int i = 0; i < topupMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < topupReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
    }

    // Cambian los mas antiguos
    for(int i = 0; i < withdrawReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      if(i <= 1) {
        Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
      } else {
        Assert.assertEquals("Deben tener estado NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, storedMovement.getConSwitch());
      }
    }

    // Nada debe cambiar
    for(int i = 0; i < reconciledMovements.size(); i++) {
      PrepaidMovement10 originalMovement = reconciledMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConSwitch());
    }
  }

  @Test
  public void expire_tecnocom() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    ArrayList<PrepaidMovement10> topupMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> topupReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> withdrawReversedMovements = new ArrayList<>();
    ArrayList<PrepaidMovement10> reconciledMovements = new ArrayList<>();

    for(int i = 0; i < 5; i++) {
      ReconciliationFile10 reconciliationFile10 = new ReconciliationFile10();
      reconciliationFile10.setStatus(FileStatus.OK);
      reconciliationFile10.setProcess(ReconciliationOriginType.SWITCH);

      reconciliationFile10.setFileName(String.format("archivo%d_1.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_2.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_3.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_TOPUP);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_4.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_5.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REJECTED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_6.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.SWITCH_REVERSED_WITHDRAW);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      reconciliationFile10.setFileName(String.format("archivo%d_7.txt", i));
      reconciliationFile10.setType(ReconciliationFileType.TECNOCOM_FILE);
      getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      topupReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.CORRECTORA);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      withdrawReversedMovements.add(0, prepaidMovement10);

      prepaidMovement10 = buildPrepaidMovement11(prepaidUser, prepaidTopup);
      prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
      prepaidMovement10.setIndnorcor(IndicadorNormalCorrector.NORMAL);
      prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
      prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
      prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);
      reconciledMovements.add(0, prepaidMovement10);

      Thread.sleep(10);
    }

    getPrepaidMovementEJBBean10().expireNotReconciledMovements(ReconciliationFileType.TECNOCOM_FILE);

    // Nada debe cambiar
    for(int i = 0; i < topupMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
      if(i <= 1) {
        Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      } else {
        Assert.assertEquals("Deben tener estado NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, storedMovement.getConTecnocom());
      }
    }

    // Nada debe cambiar
    for(int i = 0; i < topupReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = topupReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
      if(i <= 1) {
        Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      } else {
        Assert.assertEquals("Deben tener estado NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, storedMovement.getConTecnocom());
      }
    }

    // Nada debe cambiar
    for(int i = 0; i < withdrawMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
      if(i <= 1) {
        Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      } else {
        Assert.assertEquals("Deben tener estado NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, storedMovement.getConTecnocom());
      }
    }

    // Cambian los mas antiguos
    for(int i = 0; i < withdrawReversedMovements.size(); i++) {
      PrepaidMovement10 originalMovement = withdrawReversedMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConSwitch());
      if(i <= 1) {
        Assert.assertEquals("Deben tener estado PENDING", ReconciliationStatusType.PENDING, storedMovement.getConTecnocom());
      } else {
        Assert.assertEquals("Deben tener estado NOT_RECONCILED", ReconciliationStatusType.NOT_RECONCILED, storedMovement.getConTecnocom());
      }
    }

    // Nada debe cambiar
    for(int i = 0; i < reconciledMovements.size(); i++) {
      PrepaidMovement10 originalMovement = reconciledMovements.get(i);
      PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(originalMovement.getId());
      Assert.assertEquals("Deben tener mismo id", originalMovement.getId(), storedMovement.getId());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConTecnocom());
      Assert.assertEquals("Deben tener estado RECONCILED", ReconciliationStatusType.RECONCILED, storedMovement.getConSwitch());
    }
  }
}
