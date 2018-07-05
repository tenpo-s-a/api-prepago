package cl.multicaja.test.v10.async;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidTransaction10;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.InclusionMovimientosDTO;
import cl.multicaja.users.model.v10.User;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class Test_PrepaidEJBBean10_getTransactions extends TestBaseUnitAsync{

  @Test
  public void getTransacctions() throws Exception {

    User user = registerUser();

    PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

    prepaidUser10 = createPrepaidUser10(prepaidUser10);

    AltaClienteDTO altaClienteDTO = registerInTecnocom(user);

    Assert.assertTrue("debe ser exitoso", altaClienteDTO.isRetornoExitoso());

    PrepaidCard10 prepaidCard10 = buildPrepaidCard10(prepaidUser10, altaClienteDTO);

    prepaidCard10 = createPrepaidCard10(prepaidCard10);

    BigDecimal impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));

    InclusionMovimientosDTO inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);
    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());


    impfac = BigDecimal.valueOf(numberUtils.random(3000, 10000));
    inclusionMovimientosDTO = topupInTecnocom(prepaidCard10, impfac);
    Assert.assertTrue("debe ser exitoso", inclusionMovimientosDTO.isRetornoExitoso());

    List<PrepaidTransaction10> transaction10List =getPrepaidEJBBean10().getTransactions(getDefaultHeaders(),user.getId(),null,null, null);
    Assert.assertNotNull("List<PrepaidTransaction10> Not Null ",transaction10List);
    System.out.println(transaction10List.size());
    Assert.assertEquals("Size = a 2", 2,transaction10List.size() );

  }

  @Test(expected = BadRequestException.class)
  public void getTransacctionsParamsError() throws Exception{
    getPrepaidEJBBean10().getTransactions(null,null,null,null, null);
  }

}
