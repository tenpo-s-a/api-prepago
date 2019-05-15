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
    createPrepaidCardV2(card);

    PrepaidCard10 c1 = getPrepaidCardEJBBean10().getPrepaidCardById(null, card.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", card, c1);
  }

  @Test
  public void getPrepaidCards_ok_by_userId_and_status() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUser11();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    {
      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.EXPIRED);
      createPrepaidCardV2(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.EXPIRED);
      createPrepaidCardV2(card2);

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
      createPrepaidCardV2(card1);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card1, prepaidCard);
    }

    {
      PrepaidCard10 card1 = buildPrepaidCard10(prepaidUser);
      card1.setStatus(PrepaidCardStatus.PENDING);
      createPrepaidCardV2(card1);

      PrepaidCard10 card2 = buildPrepaidCard10(prepaidUser);
      card2.setStatus(PrepaidCardStatus.ACTIVE);
      createPrepaidCardV2(card2);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean10().getLastPrepaidCardByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.PENDING);

      Assert.assertNull("no debe existir", prepaidCard);
    }
  }

  @Test
  public void getPrepaidCards_check_order_desc() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUser11();
    prepaidUser = createPrepaidUserV2(prepaidUser);


    for (int j = 0; j < 10; j++) {
      PrepaidCard10 card = buildPrepaidCard10(prepaidUser);
      createPrepaidCardV2(card);
    }

    List<PrepaidCard10> lst = getPrepaidCardEJBBean10().getPrepaidCards(null, null, prepaidUser.getId(), null, null, null);

    Long id = Long.MAX_VALUE;

    for (PrepaidCard10 p : lst) {
      Assert.assertTrue("Debe estar en orden Descendente", p.getId() < id);
      id = p.getId();
    }
  }

  @Test
  public void getPrepaidCardsByPanEncriptado() throws Exception{
    {
      PrepaidCard10 originalCard = buildPrepaidCard10();
      createPrepaidCardV2(originalCard);

      PrepaidCard10 card = getPrepaidCardEJBBean10().getPrepaidCardByEncryptedPan(null, originalCard.getEncryptedPan());

      Assert.assertNotNull("debe retornar una tarjeta", card);
      Assert.assertEquals("debe ser igual al registrado anteriormemte", originalCard, card);
    }
  }


}
