package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.Clearing10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class Test_PrepaidClearingEJBBean10 extends TestBaseUnit{

  public Clearing10 buildClearing() {
    Clearing10 clearing10 = new Clearing10();
    clearing10.setUserAccountId(getUniqueLong());
    clearing10.setClearingFileId(getUniqueLong());
    clearing10.setClearingStatus(AccountingStatusType.PENDING);
    return clearing10;
  }

  @Test
  public void insertClearingOK() throws Exception{
    Accounting10 accounting10 = buildRandomAccouting();
    List<Accounting10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting10s);

    Clearing10 clearing10 = buildClearing();
    clearing10.setId(accounting10s.get(0).getId());
    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
    Assert.assertNotNull("El objeto no puede ser Null",clearing10);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing10.getClearingId().longValue());
  }

  @Test(expected = BaseException.class)
  public void insertClearingAccountingNotExist() throws Exception {
    Clearing10 clearing10 = buildClearing();
    clearing10.setId(getUniqueLong());
    getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
  }

  @Test(expected = BadRequestException.class)
  public void insertClearing_fail_id() throws Exception{
    Clearing10 clearing10 = buildClearing();
    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
    Assert.assertNotNull("El objeto no puede ser Null",clearing10);
  }

  @Test
  public void updateClearingOK() throws Exception{
    Accounting10 accounting10 = buildRandomAccouting();
    List<Accounting10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting10s);

    Clearing10 clearing10 = buildClearing();
    clearing10.setId(accounting10s.get(0).getId());
    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
    Assert.assertNotNull("El objeto no puede ser Null",clearing10);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing10.getClearingId().longValue());

    Clearing10 clearing2 = getPrepaidClearingEJBBean10().updateClearingData(null,clearing10.getClearingId(),getUniqueLong(),AccountingStatusType.OK);
    Assert.assertNotNull("No debe ser Nul", clearing2);
    Assert.assertEquals("El status debe ser OK",AccountingStatusType.OK,clearing2.getClearingStatus());
    Assert.assertNotEquals("No deben ser iguales",clearing2.getClearingStatus(),clearing10.getClearingStatus());

  }
  @Test(expected = BadRequestException.class)
  public void updateClearing_fail_id() throws Exception {
    Accounting10 accounting10 = buildRandomAccouting();
    List<Accounting10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting10s);

    Clearing10 clearing10 = buildClearing();
    clearing10.setId(accounting10s.get(0).getId());
    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
    Assert.assertNotNull("El objeto no puede ser Null",clearing10);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing10.getClearingId().longValue());

    Clearing10 clearing2 = getPrepaidClearingEJBBean10().updateClearingData(null,null,getUniqueLong(),AccountingStatusType.OK);
  }

  @Test(expected = BadRequestException.class)
  public void updateClearing_fail_allNull() throws Exception{
    Accounting10 accounting10 = buildRandomAccouting();
    List<Accounting10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting10s);

    Clearing10 clearing10 = buildClearing();
    clearing10.setId(accounting10s.get(0).getId());
    clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null,clearing10);
    Assert.assertNotNull("El objeto no puede ser Null",clearing10);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing10.getClearingId().longValue());
    Clearing10 clearing2 = getPrepaidClearingEJBBean10().updateClearingData(null,clearing10.getClearingId(),null,null);
  }

  @Test
  public void searchClearingByIdOk() throws Exception{
    Accounting10 accounting10 = buildRandomAccouting();
    List<Accounting10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null,accounting10s);

    Clearing10 clearing = buildClearing();
    clearing.setId(accounting10s.get(0).getId());
    clearing = getPrepaidClearingEJBBean10().insertClearingData(null,clearing);
    Assert.assertNotNull("El objeto no puede ser Null",clearing);
    Assert.assertNotEquals("El id no puede ser 0",0,clearing.getClearingId().longValue());

    Clearing10 clearing2 = getPrepaidClearingEJBBean10().searchClearingDataById(null,clearing.getClearingId());
    Assert.assertNotNull("El objeto no debe ser null",clearing2);
    Assert.assertEquals("Los id deben ser iguales",clearing.getClearingId(),clearing2.getClearingId());
    Assert.assertEquals("Los status deben ser iguales",clearing.getClearingStatus(),clearing2.getClearingStatus());

  }

}
