package cl.multicaja.test.integration.v10.unit;

import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidAccountingEJBBean10_isSubscriptionMerchant extends TestBaseUnit {

  @Test(expected = Exception.class)
  public void isSubscriptionMerchant_merchantName_null() throws Exception {
    getPrepaidAccountingEJBBean10().isSubscriptionMerchant(null);
  }

  @Test(expected = Exception.class)
  public void isSubscriptionMerchant_merchantName_empty() throws Exception {
    getPrepaidAccountingEJBBean10().isSubscriptionMerchant("");
  }

  @Test
  public void isSubscriptionMerchant() throws Exception {
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("UBER");
      Assert.assertTrue("Es una suscripcion", isSubscriptionMerchant);
    }
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("Uber");
      Assert.assertTrue("Es una suscripcion", isSubscriptionMerchant);
    }
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("uber");
      Assert.assertTrue("Es una suscripcion", isSubscriptionMerchant);
    }
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("Uber DE");
      Assert.assertTrue("Es una suscripcion", isSubscriptionMerchant);
    }

    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("SpOtIfy");
      Assert.assertTrue("Es una suscripcion", isSubscriptionMerchant);
    }

    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("iTunes");
      Assert.assertTrue("Es una suscripcion", isSubscriptionMerchant);
    }

    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("iTunes.com");
      Assert.assertTrue("Es una suscripcion", isSubscriptionMerchant);
    }

    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("Netflix.com");
      Assert.assertTrue("Es una suscripcion", isSubscriptionMerchant);
    }
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("NETFLIX.COM");
      Assert.assertTrue("Es una suscripcion", isSubscriptionMerchant);
    }
  }

  @Test
  public void isNotSubscriptionMerchant() throws Exception {
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("Amazon");
      Assert.assertFalse("No es una suscripcion", isSubscriptionMerchant);
    }
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("AmaZon");
      Assert.assertFalse("No es una suscripcion", isSubscriptionMerchant);
    }
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("ebay");
      Assert.assertFalse("No es una suscripcion", isSubscriptionMerchant);
    }
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("ALIEXPRESS");
      Assert.assertFalse("No es una suscripcion", isSubscriptionMerchant);
    }
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("12345");
      Assert.assertFalse("No es una suscripcion", isSubscriptionMerchant);
    }
    {
      Boolean isSubscriptionMerchant = getPrepaidAccountingEJBBean10().isSubscriptionMerchant("ALIEXPRESS");
      Assert.assertFalse("No es una suscripcion", isSubscriptionMerchant);
    }
  }
}
