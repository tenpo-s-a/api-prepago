package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
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
    PrepaidCard10 card = buildPrepaidCard10();
    card = createPrepaidCard10(card);

    PrepaidCard10 dbCard = getPrepaidCardEJBBean11().getPrepaidCardById(null, card.getId());

    Assert.assertEquals("Debe ser la misma tarjeta", card.getId(), dbCard.getId());
    Assert.assertEquals("Debe ser la misma tarjeta", card.getPan(), dbCard.getPan());
  }
}
