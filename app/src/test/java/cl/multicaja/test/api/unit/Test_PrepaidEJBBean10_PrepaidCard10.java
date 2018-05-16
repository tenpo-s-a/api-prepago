package cl.multicaja.test.api.unit;


import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_PrepaidCard10 extends TestBaseUnit {

  private PrepaidEJBBean10 prepaidEJBBean10 = new PrepaidEJBBean10();

  private PrepaidUser10 buildUser() throws Exception {
    PrepaidUser10 user = new PrepaidUser10();
    user.setIdUserMc(new Long(getUniqueInteger()));
    user.setRut(getUniqueRutNumber());
    user.setStatus(PrepaidUserStatus.ACTIVE);
    return user;
  }

  private PrepaidUser10 createUser(PrepaidUser10 user) throws Exception {
    return prepaidEJBBean10.createPrepaidUser(null, user);
  }

  private PrepaidCard10 buildCard() throws Exception {

    PrepaidUser10 u = buildUser();
    u = createUser(u);

    int expiryYear = numberUtils.random(1000, 9999);
    int expiryMonth = numberUtils.random(1, 99);
    int expiryDate = numberUtils.toInt(expiryYear + "" + StringUtils.leftPad(String.valueOf(expiryMonth), 2, "0"), 0);
    PrepaidCard10 c = new PrepaidCard10();
    c.setIdUser(u.getId());
    c.setPan(RandomStringUtils.randomNumeric(16));
    c.setEncryptedPan(RandomStringUtils.randomAlphabetic(50));
    c.setExpiration(expiryDate);
    c.setStatus(PrepaidCardStatus.ACTIVE);
    c.setProcessorUserId(RandomStringUtils.randomAlphabetic(20));
    c.setNameOnCard("Tarjeta de: " + RandomStringUtils.randomAlphabetic(5));
    return c;
  }

  private PrepaidCard10 createCard(PrepaidCard10 card) throws Exception {

    card = prepaidEJBBean10.createPrepaidCard(null, card);

    Assert.assertNotNull("debe retornar un usuario", card);
    Assert.assertEquals("debe tener id", true, card.getId() > 0);
    Assert.assertEquals("debe tener idUser", true, card.getIdUser() > 0);
    Assert.assertNotNull("debe tener pan", card.getPan());
    Assert.assertNotNull("debe tener encryptedPan", card.getEncryptedPan());
    Assert.assertNotNull("debe tener expiration", card.getExpiration());
    Assert.assertNotNull("debe tener status", card.getStatus());
    Assert.assertNotNull("debe tener processorUserId", card.getProcessorUserId());
    Assert.assertNotNull("debe tener nameOnCard", card.getNameOnCard());

    return card;
  }

  @Test
  public void insertCardOk() throws Exception {
    PrepaidCard10 card = buildCard();
    createCard(card);
  }

  @Test
  public void searchCardOk() throws Exception {

    /**
     * Caso en que se registra una nueva tarjet y luego se busca por su id y idUser
     */

    PrepaidCard10 card = buildCard();
    createCard(card);

    PrepaidCard10 c1 = prepaidEJBBean10.getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", card, c1);

    PrepaidCard10 c2 = prepaidEJBBean10.getPrepaidCardByUserId(null, card.getIdUser());

    Assert.assertNotNull("debe retornar una tarjeta", c2);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", card, c2);
  }

  @Test
  public void searchCarsOkByStatus() throws Exception {

    PrepaidCard10 card1 = buildCard();
    card1.setStatus(PrepaidCardStatus.EXPIRED);
    createCard(card1);

    PrepaidCard10 card2 = buildCard();
    card2.setStatus(PrepaidCardStatus.EXPIRED);
    createCard(card2);

    List<PrepaidCard10> lst = prepaidEJBBean10.getPrepaidCards(null, null, null, null, PrepaidCardStatus.EXPIRED, null);

    List<Long> lstFind = new ArrayList<>();

    for (PrepaidCard10 p : lst) {
      if (p.getId().equals(card1.getId()) || p.getId().equals(card2.getId())) {
        lstFind.add(p.getId());
      }
    }

    Assert.assertEquals("deben ser 2", 2 , lstFind.size());
    Assert.assertEquals("debe contener id", true, lstFind.contains(card1.getId()) && lstFind.contains(card2.getId()));
  }
}
