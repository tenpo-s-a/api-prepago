package cl.multicaja.test.integration.v10.unit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.io.File;
import java.time.ZonedDateTime;

public class Test_BackofficeEJBBean10_getEmergencyTickets extends TestBaseUnit {

  private static final Log log = LogFactory.getLog(Test_BackofficeEJBBean10_getEmergencyTickets.class);

  @Test
  public void getTickets() throws Exception {
    File response = getBackofficeEJBBEan10().generateE06Report(ZonedDateTime.now());


  }
}
