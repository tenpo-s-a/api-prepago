package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Test_PrepaidMovementEJBBean10_addPrepaidMovement extends TestBaseUnit {

  @Test
  public void addPrepaidMovement_ok() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);

    createPrepaidMovement10(prepaidMovement10);
  }

  @Test
  public void addPrepaidMovementWithDateOk() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10();

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Date   date       = format.parse ( "2010-10-10" );
    Timestamp timestamp = new Timestamp(date.getTime());

    prepaidMovement10.setFechaCreacion(timestamp);
    createPrepaidMovement10(prepaidMovement10);
    PrepaidMovement10 prepaidMovement2 = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    Assert.assertNotNull("No debe ser null",prepaidMovement10);
    Assert.assertEquals("Las fechas deben councidir",prepaidMovement10.getFechaCreacion(),prepaidMovement2.getFechaCreacion());
    Assert.assertEquals("Las fechas deben councidir",prepaidMovement10.getFechaCreacion(),prepaidMovement2.getFechaActualizacion());
  }

}
