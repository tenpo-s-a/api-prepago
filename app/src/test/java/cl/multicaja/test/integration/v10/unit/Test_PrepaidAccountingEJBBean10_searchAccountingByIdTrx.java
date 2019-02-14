package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.core.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidAccountingEJBBean10_searchAccountingByIdTrx extends TestBaseUnit {

  @Test
  public void search_ok() throws Exception {

    AccountingData10 accounting10 = buildRandomAccouting();
    accounting10.setStatus(AccountingStatusType.OK);
    accounting10.setFileId(36L);
    accounting10 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10);

    AccountingData10 db = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, accounting10.getIdTransaction());

    Assert.assertEquals("Debe ser el mismo", db.getId(), accounting10.getId());
    Assert.assertEquals("Debe ser el mismo", db.getIdTransaction(), accounting10.getIdTransaction());
    Assert.assertEquals("Debe ser el mismo", db.getStatus(), accounting10.getStatus());
    Assert.assertEquals("Debe ser el mismo", db.getAccountingStatus(), accounting10.getAccountingStatus());
    Assert.assertEquals("Debe ser el mismo", db.getAmount().getValue().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe ser el mismo", db.getType(), accounting10.getType());
    Assert.assertEquals("Debe ser el mismo", db.getAccountingMovementType(), accounting10.getAccountingMovementType());
  }

  @Test(expected = BadRequestException.class)
  public void search_IdNull() throws Exception {
    AccountingData10 accounting10 = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, null);
    Assert.fail("No debe pasar por aqui.");
  }

  @Test
  public void search_null() throws Exception {
    AccountingData10 data = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, Long.MAX_VALUE);
    Assert.assertNull("No debe retornar nada", data);
  }
}
