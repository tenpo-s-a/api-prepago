package cl.multicaja.test.v10.unit;


import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import org.junit.Test;

/**
 * @autor vutreras
 */
public class Test_PrepaidCardEJBBean10_createPrepaidCard extends TestBaseUnit {

  @Test
  public void createPrepaidCard_ok() throws Exception {
    PrepaidCard10 card = buildPrepaidCard10();
    createPrepaidCard10(card);
  }
}
