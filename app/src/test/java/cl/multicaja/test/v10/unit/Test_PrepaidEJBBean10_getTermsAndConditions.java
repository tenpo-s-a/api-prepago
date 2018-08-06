package cl.multicaja.test.v10.unit;

import cl.multicaja.prepaid.model.v10.PrepaidTac10;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author abarazarte
 **/
public class Test_PrepaidEJBBean10_getTermsAndConditions extends TestBaseUnit{

  @Test
  public void shouldAcceptTermsAndConditions() throws Exception {
    PrepaidTac10 tac = getPrepaidEJBBean10().getTermsAndConditions(null);

    Assert.assertNotNull("Debe tener tac", tac);
    Assert.assertFalse("Debe tener version", StringUtils.isBlank(tac.getVersion()));
    Assert.assertFalse("Debe tener location", StringUtils.isBlank(tac.getLocation()));
  }
}
