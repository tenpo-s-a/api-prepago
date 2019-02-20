package cl.multicaja.test.integration.v10.unit;


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
public class Test_PrepaidCardEJBBean10_getPrepaidCards extends TestBaseUnit {

  @Test
  public void getPrepaidCardById() throws Exception {

    /**
     * Caso en que se registra una nueva tarjet y luego se busca por su id y idUser
     */

    PrepaidCard10 card = buildPrepaidCard10();
    createPrepaidCard10(card);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", card, c1);
  }

  @Test
  public void getPrepaidCards_ok_by_userId_and_status() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUser10();

    prepaidUser = createPrepaidUser10(prepaidUser);

    {
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

      Assert.assertEquals("deben ser 2", 2, lstFind.size());
      Assert.assertTrue("debe contener id", lstFind.contains(card1.getId()) && lstFind.contains(card2.getId()));
    }

    {
      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCard10(card1);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card1, prepaidCard);
    }

    {
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
  public void getPrepaidCards_check_order_desc() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUser10();

    prepaidUser = createPrepaidUser10(prepaidUser);

    for (int j = 0; j < 10; j++) {
      PrepaidCard10 card = buildPrepaidCard10(prepaidUser);
      createPrepaidCard10(card);
    }

    List<PrepaidCard10> lst = getPrepaidCardEJBBean10().getPrepaidCards(null, null, prepaidUser.getId(), null, null, null);

    Long id = Long.MAX_VALUE;

    for (PrepaidCard10 p : lst) {
      Assert.assertTrue("Debe estar en orden Descendente", p.getId() < id);
      id = p.getId();
    }
  }

  @Test
  public void getPrepaidCards_ok_by_pan_and_processor_user_id () throws Exception {
    {
      PrepaidCard10 originalCard = buildPrepaidCard10();
      createPrepaidCard10(originalCard);

      PrepaidCard10 card = getPrepaidCardEJBBean10().getPrepaidCardByPanAndProcessorUserId(null, originalCard.getPan(), originalCard.getProcessorUserId());

      Assert.assertNotNull("debe retornar una tarjeta", card);
      Assert.assertEquals("debe ser igual al registrado anteriormemte", originalCard, card);
    }
    {
      PrepaidCard10 originalCard = buildPrepaidCard10();
      createPrepaidCard10(originalCard);

      PrepaidCard10 card = getPrepaidCardEJBBean10().getPrepaidCardByPanAndProcessorUserId(null, getRandomString(100), getRandomString(20));

      Assert.assertNull("no debe retornar una tarjeta", card);
    }
  }

  @Test
  public void getPrepaidCardsByPanEncriptado() throws Exception{
    {
      PrepaidCard10 originalCard = buildPrepaidCard10();
      createPrepaidCard10(originalCard);

      PrepaidCard10 card = getPrepaidCardEJBBean10().getPrepaidCardByEncryptedPan(null, originalCard.getEncryptedPan());

      Assert.assertNotNull("debe retornar una tarjeta", card);
      Assert.assertEquals("debe ser igual al registrado anteriormemte", originalCard, card);
    }
  }


}
