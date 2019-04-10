package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Test_PrepaidMovementEJBBean11_createPrepaidMovementFeeById extends TestBaseUnit {

  @Test
  public void createPrepaidMovementFee_ok() throws Exception {
    LocalDateTime nowTime = LocalDateTime.now(ZoneId.of("UTC"));

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();
    PrepaidMovement10 prepaidMovement = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    prepaidMovement = createPrepaidMovement10(prepaidMovement);

    PrepaidMovementFee10 fee = buildPrepaidMovementFee10(prepaidMovement);
    fee = getPrepaidMovementEJBBean11().createPrepaidMovementFee(fee);

    PrepaidMovementFee10 foundFee = getPrepaidMovementEJBBean11().getPrepaidMovementFeeById(fee.getId());

    Assert.assertNotNull("Debe existir", foundFee);
    Assert.assertEquals("Debe tener el mismo id", fee.getId(), foundFee.getId());
    Assert.assertEquals("Debe tener mismo id de movimiento", fee.getMovementId(), foundFee.getMovementId());
    Assert.assertEquals("Debe tener mismo amount", fee.getAmount(), foundFee.getAmount());
    Assert.assertEquals("Debe tener mismo iva", fee.getIva(), foundFee.getIva());
    Assert.assertTrue("Debe tener fecha de creacion reciente", isRecentLocalDateTime(foundFee.getTimestamps().getCreatedAt(), 5));
    Assert.assertTrue("Debe tener fecha de creacion actualizacion", isRecentLocalDateTime(foundFee.getTimestamps().getUpdatedAt(), 5));
  }

  @Test(expected = BadRequestException.class)
  public void createPrepaidMovementFee_feeNull() throws Exception {
    getPrepaidMovementEJBBean11().createPrepaidMovementFee(null);
  }

  @Test(expected = BadRequestException.class)
  public void createPrepaidMovementFee_typeNull() throws Exception {
    PrepaidMovementFee10 prepaidMovementFee = new PrepaidMovementFee10();
    prepaidMovementFee.setFeeType(null);
    prepaidMovementFee.setAmount(new BigDecimal(1000L));
    prepaidMovementFee.setIva(new BigDecimal(190L));
    getPrepaidMovementEJBBean11().createPrepaidMovementFee(prepaidMovementFee);
  }

  @Test(expected = BadRequestException.class)
  public void createPrepaidMovementFee_amountNull() throws Exception {
    PrepaidMovementFee10 prepaidMovementFee = new PrepaidMovementFee10();
    prepaidMovementFee.setFeeType(PrepaidMovementFeeType.EXCHANGE_RATE_DIF);
    prepaidMovementFee.setAmount(null);
    prepaidMovementFee.setIva(new BigDecimal(190L));
    getPrepaidMovementEJBBean11().createPrepaidMovementFee(prepaidMovementFee);
  }

  @Test(expected = BadRequestException.class)
  public void createPrepaidMovementFee_ivaNull() throws Exception {
    PrepaidMovementFee10 prepaidMovementFee = new PrepaidMovementFee10();
    prepaidMovementFee.setFeeType(PrepaidMovementFeeType.EXCHANGE_RATE_DIF);
    prepaidMovementFee.setAmount(new BigDecimal(1000L));
    prepaidMovementFee.setIva(null);
    getPrepaidMovementEJBBean11().createPrepaidMovementFee(prepaidMovementFee);
  }

}
