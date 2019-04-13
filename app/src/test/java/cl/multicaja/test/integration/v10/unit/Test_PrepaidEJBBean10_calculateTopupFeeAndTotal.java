package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author abarazarte
 */
public class Test_PrepaidEJBBean10_calculateTopupFeeAndTotal extends TestBaseUnit {

  @Test(expected = BadRequestException.class)
  public void calculateFeeAndTotal_nullTransaction() throws Exception {
    getPrepaidEJBBean10().calculateFeeAndTotal(null, new ArrayList<PrepaidMovementFee10>());
  }

  @Test(expected = BadRequestException.class)
  public void calculateFeeAndTotal_nullFeeList() throws Exception {
    PrepaidTopup10 prepaidTopup10 = buildPrepaidTopup10();
    getPrepaidEJBBean10().calculateFeeAndTotal(prepaidTopup10, null);
  }

  @Test
  public void calculateFeeAndTotal_topup() throws Exception {
    PrepaidTopup10 prepaidTopup10 = buildPrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(new BigDecimal(5000));
    prepaidTopup10.setAmount(amount);
    ArrayList<PrepaidMovementFee10> feeList = new ArrayList<>();

    PrepaidMovementFee10 prepaidMovementFee10 = new PrepaidMovementFee10();
    prepaidMovementFee10.setAmount(new BigDecimal(10L));
    feeList.add(prepaidMovementFee10);

    prepaidMovementFee10 = new PrepaidMovementFee10();
    prepaidMovementFee10.setAmount(new BigDecimal(5L));
    feeList.add(prepaidMovementFee10);

    prepaidTopup10 = (PrepaidTopup10) getPrepaidEJBBean10().calculateFeeAndTotal(prepaidTopup10, feeList);

    Assert.assertEquals("Debe tener fee total 15", new BigDecimal(15L), prepaidTopup10.getFee().getValue());
    Assert.assertEquals("Debe tener total 5000 - 15 = 4985", new BigDecimal(4985L), prepaidTopup10.getTotal().getValue());
  }

  @Test
  public void calculateFeeAndTotal_withdraw() throws Exception {
    PrepaidWithdraw10 prepaidWithdraw10 = buildPrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10(new BigDecimal(5000));
    prepaidWithdraw10.setAmount(amount);
    ArrayList<PrepaidMovementFee10> feeList = new ArrayList<>();

    PrepaidMovementFee10 prepaidMovementFee10 = new PrepaidMovementFee10();
    prepaidMovementFee10.setAmount(new BigDecimal(10L));
    feeList.add(prepaidMovementFee10);

    prepaidMovementFee10 = new PrepaidMovementFee10();
    prepaidMovementFee10.setAmount(new BigDecimal(5L));
    feeList.add(prepaidMovementFee10);

    prepaidWithdraw10 = (PrepaidWithdraw10) getPrepaidEJBBean10().calculateFeeAndTotal(prepaidWithdraw10, feeList);

    Assert.assertEquals("Debe tener fee total 15", new BigDecimal(15L), prepaidWithdraw10.getFee().getValue());
    Assert.assertEquals("Debe tener total 5000 + 15 = 5015", new BigDecimal(5015L), prepaidWithdraw10.getTotal().getValue());
  }


}
