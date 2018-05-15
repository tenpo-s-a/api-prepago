package cl.multicaja.test.api.unit;

import cl.multicaja.prepaid.domain.v10.PrepaidCard10;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;

import javax.inject.Inject;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_PrepaidCard10 extends TestBaseUnit {

  @Inject
  private PrepaidEJBBean10 prepaidEJBBean10 = new PrepaidEJBBean10();

  private PrepaidCard10 createCard() throws Exception {
    PrepaidCard10 c = new PrepaidCard10();

    return c;
  }

}
