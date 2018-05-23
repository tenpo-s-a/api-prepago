package cl.multicaja.test.api.unit;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.model.v10.CdtTransactionType;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class Test_CdtEJBBean10 extends TestBaseUnit {

  @Test
  public void addCdtTx() throws Exception {

    CdtTransaction10 oCdtTx10 = new CdtTransaction10();
    oCdtTx10.setAccountId("PREPAGO"+getUniqueRutNumber());
    oCdtTx10.setTransactionReference(0l);
    oCdtTx10.setExternalTransactionId("POS"+getUniqueInteger());
    oCdtTx10.setGloss("RECARGA DE PREPAGO");
    oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
    oCdtTx10.setAmount(new BigDecimal(20000));
    oCdtTx10 = getCdtEJBBean10().addCdtTransaction(null,oCdtTx10);

    Assert.assertNotNull("Debe retornar Una Tx Cdt", oCdtTx10);
    Assert.assertEquals("debe tener id", true, oCdtTx10.getTransactionReference() > 0);
    Assert.assertEquals("debe tener Amount", true, oCdtTx10.getAmount().doubleValue() > 0);
    Assert.assertNotNull("debe tener Account ID", oCdtTx10.getAccountId());
    Assert.assertNotNull("debe tener External Tx Id", oCdtTx10.getExternalTransactionId());

  }

}
