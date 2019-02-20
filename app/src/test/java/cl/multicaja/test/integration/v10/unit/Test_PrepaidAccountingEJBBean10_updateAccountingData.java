package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Test_PrepaidAccountingEJBBean10_updateAccountingData extends TestBaseUnit {

  @Test
  public void updateDataFull()throws Exception {

    AccountingData10 accounting10 = buildRandomAccouting();
    accounting10.setStatus(AccountingStatusType.OK);
    accounting10 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10);

    Assert.assertNotNull("EL objeto no puede ser nulo", accounting10);

    AccountingData10 accountingToUpdate = accounting10;
    accountingToUpdate.setAmount(new NewAmountAndCurrency10(new BigDecimal(9999)));
    accountingToUpdate.setFee(new BigDecimal(8888));
    accountingToUpdate.setFeeIva(new BigDecimal(7777));

    accounting10 = getPrepaidAccountingEJBBean10().searchAccountingByIdTrx(null, accounting10.getIdTransaction());
    AccountingData10 dataUpdated = getPrepaidAccountingEJBBean10().updateAccountingDataFull(null,accountingToUpdate);

    Assert.assertNotNull("EL objeto no puede ser nulo",dataUpdated);

    Assert.assertNotEquals("No Deben ser iguales",accounting10.getAmount().getValue().longValue(),dataUpdated.getAmount().getValue().longValue());
    Assert.assertNotEquals("No Deben ser iguales",accounting10.getFee().longValue(),dataUpdated.getFee().longValue());
    Assert.assertNotEquals("No Deben ser iguales",accounting10.getFeeIva().longValue(),dataUpdated.getFeeIva().longValue());


    Assert.assertEquals("Deben ser iguales",accountingToUpdate.getAmount().getValue().longValue(),dataUpdated.getAmount().getValue().longValue());
    Assert.assertEquals("Deben ser iguales",accountingToUpdate.getFee().longValue(),dataUpdated.getFee().longValue());
    Assert.assertEquals("Deben ser iguales",accountingToUpdate.getFeeIva().longValue(),dataUpdated.getFeeIva().longValue());

  }

  @Test
  public void updateData_changesOK() throws Exception {
    AccountingData10 accounting10 = buildRandomAccouting();
    accounting10.setStatus(AccountingStatusType.OK);
    accounting10.setFileId(36L);
    List<AccountingData10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10s);

    // Cambia solo el estado
    getPrepaidAccountingEJBBean10().updateAccountingData(null, accounting10.getId(), null, AccountingStatusType.INVALID_INFORMATION);
    {
      System.out.println("Saque de accounting");
      List<AccountingData10> results = getAccountingMovement(accounting10.getId());
      AccountingData10 foundData = results.get(0);
      Assert.assertEquals("Debe tener estado informacion invalida", AccountingStatusType.INVALID_INFORMATION, foundData.getStatus());
      Assert.assertEquals("Debe tener file id 36", new Long(36L), foundData.getFileId());
    }

    // Cambia solo el file id
    getPrepaidAccountingEJBBean10().updateAccountingData(null, accounting10.getId(), 21L, null);
    {
      System.out.println("Saque de accounting");
      List<AccountingData10> results = getAccountingMovement(accounting10.getId());
      AccountingData10 foundData = results.get(0);
      Assert.assertEquals("Debe tener estado informacion invalida", AccountingStatusType.INVALID_INFORMATION, foundData.getStatus());
      Assert.assertEquals("Debe tener file id 21", new Long(21L), foundData.getFileId());
    }

    // Cambian ambos
    getPrepaidAccountingEJBBean10().updateAccountingData(null, accounting10.getId(), 15L, AccountingStatusType.REJECTED_FORMAT);
    {
      System.out.println("Saque de accounting");
      List<AccountingData10> results = getAccountingMovement(accounting10.getId());
      AccountingData10 foundData = results.get(0);
      Assert.assertEquals("Debe tener estado rejected format", AccountingStatusType.REJECTED_FORMAT, foundData.getStatus());
      Assert.assertEquals("Debe tener file id 15", new Long(15L), foundData.getFileId());
    }
  }

  @Test(expected = BadRequestException.class)
  public void updateData_changesException() throws Exception {
    AccountingData10 accounting10 = buildRandomAccouting();
    accounting10.setStatus(AccountingStatusType.OK);
    accounting10.setFileId(36L);
    List<AccountingData10> accounting10s = new ArrayList<>();
    accounting10s.add(accounting10);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10s);

    // CAmbos parametros null, debe fallar
    getPrepaidAccountingEJBBean10().updateAccountingData(null, accounting10.getId(), null, null);
    Assert.fail("No debe pasar por aqui.");
  }

  private List<AccountingData10> getAccountingMovement(Long movId) {
    RowMapper rowMapper = (rs, rowNum) -> {
      AccountingData10 accountingData = new AccountingData10();
      accountingData.setId(numberUtils.toLong(rs.getLong("id")));
      accountingData.setStatus(AccountingStatusType.fromValue(rs.getString("status")));
      accountingData.setFileId(numberUtils.toLong(rs.getLong("file_id")));
      return accountingData;
    };
    List<AccountingData10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.accounting where id = %d", getSchemaAccounting(), movId), rowMapper);
    return data;
  }
}
