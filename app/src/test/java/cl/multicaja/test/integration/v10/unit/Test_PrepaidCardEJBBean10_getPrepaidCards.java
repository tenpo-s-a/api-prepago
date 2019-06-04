package cl.multicaja.test.integration.v10.unit;


import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
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

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidCard10 c1 = getPrepaidCardEJBBean11().getPrepaidCardById(null, prepaidCard10.getId());

    Assert.assertNotNull("debe retornar una tarjeta", c1);
    Assert.assertEquals("debe ser igual al registrado anteriormemte", prepaidCard10, c1);
  }

  @Test
  public void getPrepaidCards_ok_by_userId_and_status() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    {
      PrepaidCard10 card1 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card1.setStatus(PrepaidCardStatus.EXPIRED);
      card1 = createPrepaidCardV2(card1);

      PrepaidCard10 card2 =buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card2.setStatus(PrepaidCardStatus.EXPIRED);
      card2 = createPrepaidCardV2(card2);

      List<Long> lstFind = new ArrayList<>();
      List<PrepaidCard10> lst = getPrepaidCardEJBBean11().getPrepaidCards(null,null,account.getId(),null,PrepaidCardStatus.EXPIRED);
      for (PrepaidCard10 p : lst) {
        if (p.getId().equals(card1.getId()) || p.getId().equals(card2.getId())) {
          lstFind.add(p.getId());
        }
      }

      Assert.assertEquals("deben ser 2", 2, lstFind.size());
      Assert.assertTrue("debe contener id", lstFind.contains(card1.getId()) && lstFind.contains(card2.getId()));
    }

    {
      PrepaidCard10 card1 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card1.setStatus(PrepaidCardStatus.PENDING);
      card1.setAccountId(account.getId());
      card1 = createPrepaidCardV2(card1);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, account.getId(), PrepaidCardStatus.PENDING);

      Assert.assertNotNull("debe existir", prepaidCard);
      Assert.assertEquals("debe ser igual a", card1, prepaidCard);
    }

    {
      PrepaidCard10 card1 =buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card1.setStatus(PrepaidCardStatus.PENDING);
      card1.setAccountId(account.getId());
      card1 = createPrepaidCardV2(card1);

      PrepaidCard10 card2 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card2.setStatus(PrepaidCardStatus.ACTIVE);
      card2.setAccountId(account.getId());
      card2 = createPrepaidCardV2(card2);

      PrepaidCard10 prepaidCard = getPrepaidCardEJBBean11().getLastPrepaidCardByAccountIdAndStatus(null, account.getId(), PrepaidCardStatus.PENDING);
      Assert.assertNull("no debe existir", prepaidCard);
    }
  }

  @Test
  public void getPrepaidCards_check_order_desc() throws Exception {

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    for (int j = 0; j < 10; j++) {
      PrepaidCard10 card = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      card.setAccountId(account.getId());
      createPrepaidCardV2(card);
    }
    List<PrepaidCard10> lst = getPrepaidCardEJBBean11().getPrepaidCards(null,null,account.getId(),null,null,null,null);

    Long id = Long.MAX_VALUE;

    for (PrepaidCard10 p : lst) {
      Assert.assertTrue("Debe estar en orden Descendente", p.getId() < id);
      id = p.getId();
    }
  }

  @Test
  public void getPrepaidCards_ok_by_pan_and_processor_user_id () throws Exception {
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      PrepaidCard10 card = getPrepaidCardEJBBean11().getPrepaidCardByPanAndProcessorUserId(null, prepaidCard10.getPan(), account.getAccountNumber());

      Assert.assertNotNull("debe retornar una tarjeta", card);
      Assert.assertEquals("debe ser igual al registrado anteriormemte", prepaidCard10, card);
    }
    {
      PrepaidCard10 card = getPrepaidCardEJBBean11().getPrepaidCardByPanAndProcessorUserId(null, getRandomString(100), getRandomString(20));
      Assert.assertNull("no debe retornar una tarjeta", card);
    }
  }

  @Test
  public void getPrepaidCardsByPanEncriptado() throws Exception{
    {
      PrepaidUser10 prepaidUser = buildPrepaidUserv2();
      prepaidUser = createPrepaidUserV2(prepaidUser);

      Account account = buildAccountFromTecnocom(prepaidUser);
      account = createAccount(account.getUserId(),account.getAccountNumber());

      PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser,account);
      prepaidCard10 = createPrepaidCardV2(prepaidCard10);

      PrepaidCard10 card = getPrepaidCardEJBBean11().getPrepaidCardByEncryptedPan(null, prepaidCard10.getEncryptedPan());

      Assert.assertNotNull("debe retornar una tarjeta", card);
      Assert.assertEquals("debe ser igual al registrado anteriormemte", prepaidCard10, card);
    }
  }


}
