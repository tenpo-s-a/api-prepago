package cl.multicaja.test.integration.v10.unit;


import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.Assert;
import org.junit.Test;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

/**
 * @autor vutreras
 */
public class Test_PrepaidCardEJBBean10_updatePrepaidCardStatus extends TestBaseUnit {

  @Test
  public void updatePrepaidCardStatus_ok() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    getPrepaidCardEJBBean11().updatePrepaidCardStatus(null, prepaidCard10.getId(), PrepaidCardStatus.EXPIRED);

    PrepaidCard10 c1 = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard10.getId());

    Assert.assertNotNull("debe retornar un usuario", c1);
    Assert.assertEquals("el estado debe estar actualizado", PrepaidCardStatus.EXPIRED, c1.getStatus());
  }

  @Test
  public void updatePrepaidCardStatus_not_ok() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    try {

      getPrepaidCardEJBBean11().updatePrepaidCardStatus(null, prepaidCard10.getId(), null);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(BadRequestException vex) {
      Assert.assertEquals("debe se error 101004", PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
    }

    PrepaidCard10 c1 = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard10.getId());

    Assert.assertNotNull("debe retornar un usuario", c1);
    Assert.assertEquals("el estado debe estar actualizado", PrepaidCardStatus.ACTIVE, c1.getStatus());
  }
}
