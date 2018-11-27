package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.core.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class Test_PrepaidAccountingEJBBean10_saveAccountingData extends TestBaseUnit {

  @Test(expected = BadRequestException.class)
  public void saveAccountingDataNullList() throws Exception {
    try {
      getPrepaidAccountingEJBBean10().saveAccountingData(null,null);
    }catch (BadRequestException e) {
      Assert.assertEquals("","accounting10s",e.getData()[0].getValue());
      throw new BadRequestException();
    }
  }

  @Test
  public void aveAccountingDataErrorInData() throws Exception {

    // Falla en parametro 1
    {
      List<Accounting10> lstAccounts =  generateRandomAccountingList(1,2);
      try {
        getPrepaidAccountingEJBBean10().saveAccountingData(null,lstAccounts);
      }catch (BadRequestException e) {
        Assert.assertEquals("Id Tx Null","getIdTransaction",e.getData()[0].getValue());
      }
    }
    // Falla en parametro 2
    {
      List<Accounting10> lstAccounts =  generateRandomAccountingList(2,2);
      try {
        getPrepaidAccountingEJBBean10().saveAccountingData(null,lstAccounts);
      }catch (BadRequestException e) {
        Assert.assertEquals("Tipo Tx Null","getType",e.getData()[0].getValue());
      }
    }
    // Falla en parametro 3
    {
      List<Accounting10> lstAccounts =  generateRandomAccountingList(3,2);
      try {
        getPrepaidAccountingEJBBean10().saveAccountingData(null,lstAccounts);
      }catch (BadRequestException e) {
        Assert.assertEquals("Origen Null","getOrigin",e.getData()[0].getValue());
      }
    }
    // Falla en parametro 4
    {
      List<Accounting10> lstAccounts =  generateRandomAccountingList(4,2);
      try {
        getPrepaidAccountingEJBBean10().saveAccountingData(null,lstAccounts);
      }catch (BadRequestException e) {
        Assert.assertEquals("Fecha Tx Null","getTransactionDate",e.getData()[0].getValue());
      }
    }

  }

  @Test
  public void aveAccountingData() throws Exception {

  }

}
