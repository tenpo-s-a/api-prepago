package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.JMSHeader;
import cl.multicaja.prepaid.async.v10.model.PrepaidTopupData10;
import cl.multicaja.prepaid.async.v10.routes.ConciliationMcRedRoute10;
import cl.multicaja.prepaid.helpers.mcRed.McRedReconciliationFileDetail;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.test.integration.v10.unit.Test_ReconciliationFilesEJBBean10_createReconciliationFile10;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;
import java.sql.Timestamp;
import java.time.LocalDate;

public class Test_PendingConciliationMcRed10_process extends TestBaseUnitAsync {

  // Chequea que el mensaje esta llegando al proceso conciliador
  // Esto solo chequea que se este llamando al proceso mediante la cola
  // Las funciones de conciliacion se estan testeando individualmente en Test_Reconciliation_FullTest.java
  @Test
  public void process_messageIsProcessed() throws Exception {
    // Crear usuario, cuenta, tarjeta
    PrepaidUser10 prepaidUser10= buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);
    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(account.getUserId(),account.getAccountNumber());
    PrepaidCard10 card = buildPrepaidCardWithTecnocomData(prepaidUser10,account);
    card = createPrepaidCardV2(card);

    // Inserta un movimiento
    PrepaidTopup10 prepaidTopup10 = buildPrepaidTopup10();
    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser10, prepaidTopup10, card);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // Insertar un archivo de conciliacion
    ReconciliationFile10 reconciliationFile10 = Test_ReconciliationFilesEJBBean10_createReconciliationFile10.buildReconciliationFile();
    reconciliationFile10 = getReconciliationFilesEJBBean10().createReconciliationFile(null, reconciliationFile10);

    // Insertar un movimiento swicth para conciliar
    McRedReconciliationFileDetail switchMovement = createSwitchMovement(reconciliationFile10.getId(), prepaidMovement10);
    getMcRedReconciliationEJBBean10().addFileMovement(null, switchMovement);

    // Se envia un mensaje a la cola, con el id del archivo creado, para que sea procesado
    Queue qReq = camelFactory.createJMSQueue(ConciliationMcRedRoute10.PENDING_PROCESS_SWITCHMC_FILE_REQ);
    camelFactory.createJMSMessenger().putMessage(qReq, getRandomString(20), null, new JMSHeader("fileId", reconciliationFile10.getId().toString()));

    // Espera que lo tome la cola y lo concilie
    boolean reconciled = false;
    for (int i = 0; i < 20; i++) {
      Thread.sleep(1000);
      PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean11().getPrepaidMovementById(prepaidMovement10.getId());
      if (ReconciliationStatusType.RECONCILED.equals(foundMovement.getConSwitch())) {
        reconciled = true;
        break;
      }
    }
    Assert.assertTrue("Debe estar conciliado", reconciled);
  }

  McRedReconciliationFileDetail createSwitchMovement(Long fileId, PrepaidMovement10 prepaidMovement10) {
    McRedReconciliationFileDetail registroSwitch = new McRedReconciliationFileDetail();
    registroSwitch.setMcCode(prepaidMovement10.getIdTxExterno());
    registroSwitch.setClientId(prepaidMovement10.getIdPrepaidUser());
    registroSwitch.setExternalId(0L);
    registroSwitch.setDateTrx(Timestamp.valueOf(LocalDate.now().atStartOfDay()));
    registroSwitch.setFileId(fileId);
    registroSwitch.setAmount(prepaidMovement10.getMonto());
    return registroSwitch;
  }
}
