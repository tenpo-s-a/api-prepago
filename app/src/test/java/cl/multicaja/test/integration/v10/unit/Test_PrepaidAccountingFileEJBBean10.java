package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingFileFormatType;
import cl.multicaja.accounting.model.v10.AccountingFileType;
import cl.multicaja.accounting.model.v10.AccountingFiles10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidAccountingFileEJBBean10 extends TestBaseUnit {

  public AccountingFiles10 buildRandomAccountingFile(){
    AccountingFiles10 accountingFiles10 = new AccountingFiles10();
    accountingFiles10.setName(getRandomString(10));
    accountingFiles10.setFileId(getRandomNumericString(10));
    accountingFiles10.setFileType(AccountingFileType.ACCOUNTING);
    accountingFiles10.setStatus(AccountingStatusType.PENDING);
    accountingFiles10.setFileFormatType(AccountingFileFormatType.CSV);
    return accountingFiles10;
  }

  @Test
  public void insertAccountingFileOk() throws Exception {
    AccountingFiles10 accountingFiles10 = buildRandomAccountingFile();
    accountingFiles10 = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null,accountingFiles10);
    Assert.assertNotNull("El objeto debe existir",accountingFiles10);
    Assert.assertNotEquals("ID no debe ser 0",0L,accountingFiles10.getId().longValue());
  }
  @Test(expected = BadRequestException.class)
  public void insertAccountingFile_fail_FileId() throws Exception {
    AccountingFiles10 accountingFiles10 = buildRandomAccountingFile();
    accountingFiles10.setFileId(null);
    accountingFiles10 = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null,accountingFiles10);
  }
  @Test(expected = BadRequestException.class)
  public void insertAccountingFile_fail_Name() throws Exception {
    AccountingFiles10 accountingFiles10 = buildRandomAccountingFile();
    accountingFiles10.setName(null);
    accountingFiles10 = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null,accountingFiles10);
  }
  @Test(expected = BadRequestException.class)
  public void insertAccountingFile_fail_Status() throws Exception {
    AccountingFiles10 accountingFiles10 = buildRandomAccountingFile();
    accountingFiles10.setStatus(null);
    accountingFiles10 = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null,accountingFiles10);
  }

  @Test
  public void updateAccountingFileOk() throws Exception {
    AccountingFiles10 accountingFiles10 = buildRandomAccountingFile();
    accountingFiles10 = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null,accountingFiles10);
    Assert.assertNotNull("El objeto debe existir",accountingFiles10);
    Assert.assertNotEquals("ID no debe ser 0",0L,accountingFiles10.getId().longValue());
    Assert.assertEquals("Status Pending",AccountingStatusType.PENDING,accountingFiles10.getStatus());
    AccountingFiles10 accountingFiles10update = getPrepaidAccountingFileEJBBean10().updateAccountingFile(null,accountingFiles10.getId(),null,"url.test",AccountingStatusType.OK);
    Assert.assertEquals("Status debe ser OK",AccountingStatusType.OK,accountingFiles10update.getStatus());
    Assert.assertNotNull("URL Not Null",accountingFiles10update.getUrl());
  }
}
