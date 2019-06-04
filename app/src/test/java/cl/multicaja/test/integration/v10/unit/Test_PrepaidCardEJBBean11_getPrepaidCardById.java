package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.TARJETA_NO_EXISTE;

public class Test_PrepaidCardEJBBean11_getPrepaidCardById extends TestBaseUnit {

  @BeforeClass
  @AfterClass
  public static void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("truncate %s.prp_tarjeta cascade", getSchema()));
  }

  @Test(expected = BadRequestException.class)
  public void getPrepaidCardById_cardId_null() throws Exception {
    try {
      getPrepaidCardEJBBean11().getPrepaidCardById(null, null);
    } catch(BadRequestException vex) {
      Assert.assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = ValidationException.class)
  public void getPrepaidCardById_card_null() throws Exception {
    try {
      getPrepaidCardEJBBean11().getPrepaidCardById(null, Long.MAX_VALUE);
    } catch(ValidationException vex) {
      Assert.assertEquals(TARJETA_NO_EXISTE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test
  public void getPrepaidCardById() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    PrepaidCard10 dbCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard10.getId());

    Assert.assertEquals("Debe ser la misma tarjeta", prepaidCard10.getId(), dbCard.getId());
    Assert.assertEquals("Debe ser la misma tarjeta", prepaidCard10.getPan(), dbCard.getPan());
  }
}
