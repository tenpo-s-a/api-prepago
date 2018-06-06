package cl.multicaja.test.v10.unit;


import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @autor vutreras
 */
public class Test_PrepaidEJBBean10_PrepaidCard10 extends TestBaseUnit {

  @Test
  public void insertCardOk() throws Exception {
    PrepaidCard10 card = buildPrepaidCard10();
    createPrepaidCard10(card);
  }

  @Test
  public void searchCardOk() throws Exception {

    /**
     * Caso en que se registra una nueva tarjet y luego se busca por su id y idUser
     */

    PrepaidCard10 card = buildPrepaidCard10();
    createPrepaidCard10(card);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", card, c1);

    PrepaidCard10 c2 = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, card.getIdUser(), card.getStatus());

    Assert.assertNotNull("debe retornar una tarjeta", c2);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", card, c2);
  }

  @Test
  public void searchListCarsOkByUserIdAndStatus() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUser10();

    prepaidUser = createPrepaidUser10(prepaidUser);

    PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
    card1.setStatus(PrepaidCardStatus.EXPIRED);
    createPrepaidCard10(card1);

    PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
    card2.setStatus(PrepaidCardStatus.EXPIRED);
    createPrepaidCard10(card2);

    List<Long> lstFind = new ArrayList<>();
    List<PrepaidCard10> lst = getPrepaidCardEJBBean10().getPrepaidCards(null, null, prepaidUser.getId(), null, PrepaidCardStatus.EXPIRED, null);
    for (PrepaidCard10 p : lst) {
      if (p.getId().equals(card1.getId()) || p.getId().equals(card2.getId())) {
        lstFind.add(p.getId());
      }
    }

    Assert.assertEquals("deben ser 2", 2 , lstFind.size());
    Assert.assertEquals("debe contener id", true, lstFind.contains(card1.getId()) && lstFind.contains(card2.getId()));
  }

  @Test
  public void updateStatusOk() throws Exception {

    PrepaidCard10 card = buildPrepaidCard10();
    card = createPrepaidCard10(card);

    getPrepaidCardEJBBean10().updatePrepaidCardStatus(null, card.getId(), PrepaidCardStatus.EXPIRED);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar un usuario", c1);
    Assert.assertEquals("el estado debe estar actualizado", PrepaidCardStatus.EXPIRED, c1.getStatus());
  }

  @Test
  public void updateCard() throws Exception {

    PrepaidCard10 card = buildPrepaidCard10Pending();
    card = createPrepaidCard10(card);
    Long cardId = card.getId();
    Long userId = card.getIdUser();
    PrepaidCardStatus state = card.getStatus();

    card.setStatus(PrepaidCardStatus.ACTIVE);
    card.setExpiration(1023);
    card.setNameOnCard(getRandomString(20));
    card.setPan(getRandomString(16));
    card.setEncryptedPan(getRandomString(20));
    card.setProducto(getRandomNumericString(2));
    card.setNumeroUnico(getRandomNumericString(8));

    boolean updated = getPrepaidCardEJBBean10().updatePrepaidCard(null,cardId,userId,state,card);

    Assert.assertTrue("La tarjeta be haber sido actualizada, debe ser true", updated);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("la tarjeta debe estar actualizada", card, c1);
  }

  @Test
  public void checkOrderDesc() throws Exception {

    for (int j = 0; j < 10; j++) {
      PrepaidCard10 card = buildPrepaidCard10();
      createPrepaidCard10(card);
    }

    List<PrepaidCard10> lst = getPrepaidCardEJBBean10().getPrepaidCards(null, null, null, null, null, null);

    Long id = Long.MAX_VALUE;

    for (PrepaidCard10 p : lst) {
      System.out.println(p);
      Assert.assertEquals("Debe estar en orden Descendente", true, p.getId() < id);
      id = p.getId();
    }
  }

  @Test
  public void searchCarsOkByUserIdAndStatus() throws Exception {
    {
      PrepaidUser10 prepaidUser = buildPrepaidUser10();

      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card2);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card2, prepaidCard);
    }

    {
      PrepaidUser10 prepaidUser = buildPrepaidUser10();

      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.ACTIVE);
      createPrepaidCard10(card2);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING);

      Assert.assertNull("no debe existir", prepaidCard);
    }
  }

  @Test
  public void searchCarsOkByUserIdAndOneOfStatus() throws Exception {

    {
      PrepaidUser10 prepaidUser = buildPrepaidUser10();

      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.LOCKED);
      createPrepaidCard10(card2);

      PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
      card3.setStatus(PrepaidCardStatus.ACTIVE);
      createPrepaidCard10(card3);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
        PrepaidCardStatus.ACTIVE,
        PrepaidCardStatus.LOCKED,
        PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card3, prepaidCard);
    }

    {
      PrepaidUser10 prepaidUser = buildPrepaidUser10();

      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.ACTIVE);
      createPrepaidCard10(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.LOCKED);
      createPrepaidCard10(card2);

      PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
      card3.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card3);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
        PrepaidCardStatus.ACTIVE,
        PrepaidCardStatus.LOCKED,
        PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card3, prepaidCard);
    }

    {
      PrepaidUser10 prepaidUser = buildPrepaidUser10();

      prepaidUser = createPrepaidUser10(prepaidUser);

      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.ACTIVE);
      createPrepaidCard10(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card2);

      PrepaidCard10 card3 = buildPrepaidCard10(prepaidUser);
      card3.setStatus(PrepaidCardStatus.LOCKED);
      createPrepaidCard10(card3);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser.getId(),
        PrepaidCardStatus.ACTIVE,
        PrepaidCardStatus.LOCKED,
        PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card3, prepaidCard);
    }
  }
}
