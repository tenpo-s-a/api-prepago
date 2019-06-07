package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.ejb.v11.PrepaidCardEJBBean11;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.AzureEncryptCardUtilImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class Test_PrepaidCardEJBBean11_fixEncryptData extends TestBaseUnit {


  @Spy
  private AzureEncryptCardUtilImpl  azureEncryptCardUtil;
  @Spy
  @InjectMocks
  private PrepaidCardEJBBean11 prepaidCardEJBBean11;

  @Before
  @After
  public void beforeAndAfter() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_tarjeta CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_cuenta CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE TABLE %s.prp_usuario CASCADE", getSchema()));
  }

  @Test
  public void testChangeEncrypt() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard1 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard1 = createPrepaidCardV2(prepaidCard1);

    PrepaidCard10 prepaidCard2 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard2 = createPrepaidCardV2(prepaidCard2);

    PrepaidCard10 prepaidCard3 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard3 = createPrepaidCardV2(prepaidCard3);

    Mockito.doReturn(UUID.randomUUID().toString()+"_ENCRYPT_FIXED").when(azureEncryptCardUtil).encryptPan(Mockito.any(),Mockito.any());
    prepaidCardEJBBean11.fixEncryptData(getConfigUtils().getProperty("encrypt.password",""));

    PrepaidCard10 prepaidCard1After = getPrepaidCardEJBBean11().getPrepaidCardById(null,prepaidCard1.getId());
    Assert.assertTrue("Debe contener el texto _ENCRYPT_FIXED", prepaidCard1After.getEncryptedPan().contains("_ENCRYPT_FIXED"));
    Assert.assertNotEquals("Los Objetos deben ser distintos",prepaidCard1,prepaidCard1After);

    PrepaidCard10 prepaidCard2After = getPrepaidCardEJBBean11().getPrepaidCardById(null,prepaidCard1.getId());
    Assert.assertTrue("Debe contener el texto _ENCRYPT_FIXED", prepaidCard2After.getEncryptedPan().contains("_ENCRYPT_FIXED"));
    Assert.assertNotEquals("Los Objetos deben ser distintos",prepaidCard2,prepaidCard2After);

    PrepaidCard10 prepaidCard3After = getPrepaidCardEJBBean11().getPrepaidCardById(null,prepaidCard1.getId());
    Assert.assertTrue("Debe contener el texto _ENCRYPT_FIXED", prepaidCard3After.getEncryptedPan().contains("_ENCRYPT_FIXED"));
    Assert.assertNotEquals("Los Objetos deben ser distintos",prepaidCard3,prepaidCard3After);


  }

}
