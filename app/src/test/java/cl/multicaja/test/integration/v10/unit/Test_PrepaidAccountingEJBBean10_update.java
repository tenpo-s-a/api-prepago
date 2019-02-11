package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.core.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class Test_PrepaidAccountingEJBBean10_update extends TestBaseUnit{

  @Test
  public void update_Ok() throws Exception {

    AccountingData10 accounting10 = buildRandomAccouting();
    accounting10.setStatus(AccountingStatusType.OK);
    accounting10.setFileId(36L);
    accounting10 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10);

    accounting10.setFileId(1L);
    accounting10.setStatus(AccountingStatusType.REJECTED);
    accounting10.setAccountingStatus(AccountingStatusType.REVERSED);

    getPrepaidAccountingEJBBean10().update(null, accounting10);

    AccountingData10 updated = getAccountingMovement(accounting10.getId());
    Assert.assertEquals("Debe tener estado REJECTED", AccountingStatusType.REJECTED, updated.getStatus());
    Assert.assertEquals("Debe tener file id 1", Long.valueOf(1), updated.getFileId());
    Assert.assertEquals("Debe tener estado REJECTED", AccountingStatusType.REVERSED, updated.getAccountingStatus());
  }

  @Test(expected = BadRequestException.class)
  public void update_IdNull() throws Exception {
    AccountingData10 accounting10 = new AccountingData10();
    getPrepaidAccountingEJBBean10().update(null, accounting10);
    Assert.fail("No debe pasar por aqui.");
  }

  @Test(expected = BadRequestException.class)
  public void update_DataNull() throws Exception {
    getPrepaidAccountingEJBBean10().update(null, null);
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
