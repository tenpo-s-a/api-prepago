package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Test_PrepaidCardEJBBean11_hashPan extends TestBaseUnit {

  @Test(expected = BadRequestException.class)
  public void hashPan_accountUuid_null() throws Exception {
    try {
      getPrepaidCardEJBBean11().hashPan(null, getRandomNumericString(16));
      Assert.fail("Should not be here");
    } catch(BadRequestException vex) {
      assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void hashPan_accountUuid_empty() throws Exception {
    try {
      getPrepaidCardEJBBean11().hashPan("", getRandomNumericString(16));
      Assert.fail("Should not be here");
    } catch(BadRequestException vex) {
      assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void hashPan_pan_null() throws Exception {
    try {
      getPrepaidCardEJBBean11().hashPan(UUID.randomUUID().toString(), null);
      Assert.fail("Should not be here");
    } catch(BadRequestException vex) {
      assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  @Test(expected = BadRequestException.class)
  public void hashPan_pan_empty() throws Exception {
    try {
      getPrepaidCardEJBBean11().hashPan(UUID.randomUUID().toString(), "");
      Assert.fail("Should not be here");
    } catch(BadRequestException vex) {
      assertEquals(PARAMETRO_FALTANTE_$VALUE.getValue(), vex.getCode());
      throw vex;
    }
  }

  public void hashPan() throws Exception {
    assertNotNull(getPrepaidCardEJBBean11().hashPan(UUID.randomUUID().toString(), getRandomNumericString(16)));
  }
}
