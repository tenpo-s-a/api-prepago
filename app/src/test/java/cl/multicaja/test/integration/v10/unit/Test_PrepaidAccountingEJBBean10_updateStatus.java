package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.core.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class Test_PrepaidAccountingEJBBean10_updateStatus extends TestBaseUnit{

  @Test
  public void updateStatus_Ok() throws Exception {
    AccountingData10 accounting10 = buildRandomAccouting();
    accounting10.setStatus(AccountingStatusType.RESEARCH);
    accounting10.setFileId(36L);
    accounting10 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10);

    // Cambia solo el estado
    getPrepaidAccountingEJBBean10().updateStatus(null, accounting10.getId(), AccountingStatusType.INVALID_INFORMATION);

    AccountingData10 foundData = getAccountingMovement(accounting10.getId());
    Assert.assertEquals("Debe tener estado informacion invalida", AccountingStatusType.INVALID_INFORMATION, foundData.getStatus());
    Assert.assertEquals("Debe tener file id 36", new Long(36L), foundData.getFileId());
  }


  @Test(expected = BadRequestException.class)
  public void updateStatus_statusNull() throws Exception {
    getPrepaidAccountingEJBBean10().updateStatus(null, Long.MAX_VALUE, null);
  }

  @Test(expected = BadRequestException.class)
  public void updateStatus_IdNull() throws Exception {
    getPrepaidAccountingEJBBean10().updateStatus(null, null, AccountingStatusType.REJECTED);
  }

  private AccountingData10 getAccountingMovement(Long movId) {
    RowMapper rowMapper = (rs, rowNum) -> {
      AccountingData10 accountingData = new AccountingData10();
      accountingData.setId(numberUtils.toLong(rs.getLong("id")));
      accountingData.setStatus(AccountingStatusType.fromValue(rs.getString("status")));
      accountingData.setAccountingStatus(AccountingStatusType.fromValue(rs.getString("accounting_status")));
      accountingData.setFileId(numberUtils.toLong(rs.getLong("file_id")));
      return accountingData;
    };
    List<AccountingData10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.accounting where id = %d order by id desc", getSchemaAccounting(), movId), rowMapper);
    return (data == null || data.isEmpty()) ? null : data.get(0);
  }
}
