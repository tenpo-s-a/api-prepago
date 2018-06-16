package cl.multicaja.test.v10.async;


import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.NameStatus;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.SALDO_SUPERARA_LOS_$$VALUE;
import static cl.multicaja.core.model.Errors.TRANSACCION_ERROR_GENERICO_$VALUE;

/**
 * @autor vutreras
 */
@Ignore
public class Test_topupSimulation_CDT extends TestBaseUnitAsync {

  @Test
  public void topupSimulation_not_ok_by_cdt_limit() throws Exception {

    User user = registerUser();

    //user.setNameStatus(NameStatus.UNVERIFIED);

    //updateUser(user);

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    //primera carga
    {
      NewPrepaidTopup10 prepaidTopup10 = buildNewPrepaidTopup10(user);
      prepaidTopup10.getAmount().setValue(BigDecimal.valueOf(3000));

      PrepaidTopup10 resp = getPrepaidEJBBean10().topupUserBalance(null, prepaidTopup10);

      System.out.println("resp:: " + resp);

      Assert.assertNotNull("debe tener un id", resp.getId());
    }

    PrepaidCard10 prepaidCard10 = null;

    for(int j = 0; j < 10; j++) {

      Thread.sleep(1000);

      prepaidCard10 = getPrepaidCardEJBBean10().getLastPrepaidCardByUserId(null, prepaidUser10.getId());

      if (prepaidCard10 != null) {
        break;
      }
    }

    System.out.println(prepaidCard10);

    PrepaidBalance10 prepaidBalance10 = getPrepaidUserEJBBean10().getPrepaidUserBalance(null, prepaidUser10.getId());

    System.out.println(prepaidBalance10);

    /*
    //se intenta cargar 100.001, debe dar error dado que el maximo es 500.000
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(BigDecimal.valueOf(10000));

    SimulationNew10 simulationNew = new SimulationNew10();
    simulationNew.setAmount(amount);
    simulationNew.setPaymentMethod(TransactionOriginType.WEB); //da lo mismo si es WEB o POS, para los 2 casos es la misma validacion

    System.out.println("Calcular carga WEB: " + simulationNew);

    try {

      //debe lanzar excepcion de supera saldo, dado que intenta cargar 100.001 que sumado al saldo inicial de 400.000
      //supera el maximo de 500.000
      getPrepaidEJBBean10().topupSimulation(null, prepaidUser10.getId(), simulationNew);

      Assert.fail("No debe pasar por acá, debe lanzar excepcion de validacion");

    } catch(ValidationException vex) {
      System.out.println(vex);
      Assert.assertTrue("debe ser error de limite de cdt", vex.getCode() > TRANSACCION_ERROR_GENERICO_$VALUE.getValue() && vex.getCode() < SALDO_SUPERARA_LOS_$$VALUE.getValue());
    }
    */
  }
}
