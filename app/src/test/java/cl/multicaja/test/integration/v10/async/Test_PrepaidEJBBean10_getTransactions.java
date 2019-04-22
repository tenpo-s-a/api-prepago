package cl.multicaja.test.integration.v10.async;

import cl.multicaja.core.exceptions.BadRequestException;
import org.junit.Ignore;
import org.junit.Test;

public class Test_PrepaidEJBBean10_getTransactions extends TestBaseUnitAsync{

  //TODO: No se usara
  @Ignore
  @Test
  public void getTransacctions() throws Exception {


    /*PrepaidUser10 prepaidUser10 = buildPrepaidUser10(user);

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

    PrepaidTransactionExtend10 prepaidTransactionExtend10 = getPrepaidEJBBean10().getTransactions(
      getDefaultHeaders(),user.getId(),null,null, null);

    Assert.assertNotNull("List<PrepaidTransaction10> or Data Not Null ",prepaidTransactionExtend10.getData());
    Assert.assertEquals("Size = a 2", 2,prepaidTransactionExtend10.getData().size() );
     */
  }

  @Test(expected = BadRequestException.class)
  public void getTransacctionsParamsError() throws Exception{
    getPrepaidEJBBean10().getTransactions(null,null,null,null, null);
  }

}
