package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.core.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.List;

public class Test_PrepaidAccountingEJBBean10_updateAccountingData extends TestBaseUnit {
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
