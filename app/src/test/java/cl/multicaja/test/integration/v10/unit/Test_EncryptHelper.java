package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.helpers.EncryptHelper;
import org.junit.Assert;
import org.junit.Test;

public class Test_EncryptHelper {


  private EncryptHelper encryptHelper;

  public EncryptHelper getEncryptHelper() {
    if(encryptHelper == null) {
      encryptHelper = EncryptHelper.getInstance();
    }
    return encryptHelper;
  }

  public void setEncryptHelper(EncryptHelper encryptHelper) {
    this.encryptHelper = encryptHelper;
  }

  @Test
  public void test_EncryptMethod() {

    String toEncrypt = "677428292938293";
    String encrypted = getEncryptHelper().encryptPan(toEncrypt);
    String desencrypted = getEncryptHelper().decryptPan(encrypted);
    Assert.assertNotNull("EL texto encryptado no debe ser null", encrypted);
    Assert.assertNotNull("EL texto desencryptado no debe ser null", desencrypted);
    Assert.assertEquals("El texto original y el desencriptado deben ser iguales",toEncrypt,desencrypted);
  
  }

}
