package cl.multicaja.test.unit;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.http.HttpError;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.core.utils.http.HttpUtils;
import cl.multicaja.prepaid.helpers.fees.FeeService;
import cl.multicaja.prepaid.helpers.fees.model.Charge;
import cl.multicaja.prepaid.helpers.fees.model.ChargeType;
import cl.multicaja.prepaid.helpers.fees.model.Fee;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class Test_FeeService_calculateFees {

  @Spy
  private HttpUtils httpUtils;

  @Spy
  @InjectMocks
  private FeeService feeService = FeeService.getInstance();

  @Test
  public void calculateFees_respondsOk_Mockito() throws TimeoutException, BaseException {

    PrepaidMovementType movementType = PrepaidMovementType.PURCHASE;
    CodigoMoneda currencyCode = CodigoMoneda.CHILE_CLP;
    Long amount = 1000L;

    HttpResponse httpResponse = new HttpResponse();
    httpResponse.setHttpError(HttpError.NONE);
    httpResponse.setStatus(200);
    httpResponse.setResp("{\"total\": 20, \"charges\": [{\"charge_type\": \"IVA\",\"amount\": 4},{\"charge_type\": \"COMMISSION\",\"amount\": 16}]}");

    String apiUrl = new ConfigUtils("api-prepaid").getProperty("apis.fees.url");
    String expectedCall = String.format(apiUrl, feeService.getTransactionType(movementType), currencyCode.getValue().toString(), amount);
    System.out.println("Expected call: " + expectedCall);

    Mockito.doReturn(httpResponse).when(httpUtils).execute(
      eq(HttpUtils.ACTIONS.GET),
      Mockito.isNull(),
      Mockito.anyInt(),
      Mockito.anyInt(),
      eq(expectedCall),
      Mockito.isNull(),
      Mockito.any());

    Fee fee = feeService.calculateFees(movementType, currencyCode, amount);
    Assert.assertNotNull("Debe existir la respuesta", fee);
    Assert.assertEquals("Debe tener fee total 20", new Long(20L), fee.getTotal());

    List<Charge> charges = fee.getCharges();
    Assert.assertEquals("Debe tener 2 charges", 2, charges.size());

    Charge commissionCharge = charges.stream().filter(c -> ChargeType.COMMISSION.equals(c.getChargeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe tener un charge de commission", commissionCharge);
    Assert.assertEquals("El charge de ommission es de 16", new Long(16), commissionCharge.getAmount());

    Charge ivaCharge = charges.stream().filter(c -> ChargeType.IVA.equals(c.getChargeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe tener un charge de iva", ivaCharge);
    Assert.assertEquals("El charge de ommission es de 4", new Long(4), ivaCharge.getAmount());
  }

  // Test ignorado debido a que requiere que el servicio de fees esté arriba
  // Sólo se usó para verificar que FeeService llame correctamente al servicio
  @Ignore
  @Test
  public void calculateFees_respondsOk_Integration() throws TimeoutException, BaseException {
    Fee fee = FeeService.getInstance().calculateFees(PrepaidMovementType.PURCHASE, CodigoMoneda.CHILE_CLP, 1000L);
    Assert.assertNotNull("Debe existir la respuesta", fee);
    Assert.assertEquals("Debe tener fee total 20", new Long(20L), fee.getTotal());

    List<Charge> charges = fee.getCharges();
    Assert.assertEquals("Debe tener 2 charges", 2, charges.size());

    Charge commissionCharge = charges.stream().filter(c -> ChargeType.COMMISSION.equals(c.getChargeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe tener un charge de commission", commissionCharge);
    Assert.assertEquals("El charge de ommission es de 16", new Long(16), commissionCharge.getAmount());

    Charge ivaCharge = charges.stream().filter(c -> ChargeType.IVA.equals(c.getChargeType())).findAny().orElse(null);
    Assert.assertNotNull("Debe tener un charge de iva", ivaCharge);
    Assert.assertEquals("El charge de ommission es de 4", new Long(4), ivaCharge.getAmount());
  }
}
