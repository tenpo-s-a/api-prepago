package cl.multicaja.test.unit;

import cl.multicaja.accounting.model.v10.UserAccount;
import org.junit.Assert;
import org.junit.Test;

public class Test_UserAccount {
  @Test
  public void test_getCensoredAccountNumber() {
    UserAccount userAccount = new UserAccount();
    userAccount.setAccountNumber(123456789L);

    String expected = "XXXXXX6789";
    Assert.assertEquals("Debe ser " + expected, expected, userAccount.getCensoredAccount());
  }
}
