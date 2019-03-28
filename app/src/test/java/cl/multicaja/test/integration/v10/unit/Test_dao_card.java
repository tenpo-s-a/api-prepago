package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.dao.CardDao;
import cl.multicaja.prepaid.model.v11.Card;
import cl.multicaja.prepaid.model.v11.CardStatus;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

public class Test_dao_card extends TestBaseUnit{

  private CardDao cardDao = new CardDao();

  @Before
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("delete  from %s.%s",getSchema(),"prp_tarjeta"));

  }

  @Test
  public void testInsert() {
    cardDao.setEm(createEntityManager());
    Card card = new Card();
    card.setCreatedAt(LocalDateTime.now());
    card.setUpdatedAt(LocalDateTime.now());
    card.setStatus(CardStatus.ACTIVE);
    card.setAccountId(0l);
    card.setCardName(getRandomString(10));
    card.setPan(getRandomNumericString(10));
    card.setCryptedPan(getRandomString(20));
    card.setPanHash(getRandomString(20));

    card = cardDao.insert(card);
    Assert.assertNotNull("No debe ser null",card);
    Assert.assertNotNull("No debe ser null",card.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,card.getId().longValue());
  }

  @Test
  public void testFinByAccountId() {
    cardDao.setEm(createEntityManager());
    Card card = new Card();
    card.setCreatedAt(LocalDateTime.now());
    card.setUpdatedAt(LocalDateTime.now());
    card.setStatus(CardStatus.ACTIVE);
    card.setAccountId(RandomUtils.nextLong());
    card.setCardName(getRandomString(10));
    card.setPan(getRandomNumericString(10));
    card.setCryptedPan(getRandomString(20));
    card.setPanHash(getRandomString(20));

    card = cardDao.insert(card);
    Assert.assertNotNull("No debe ser null",card);
    Assert.assertNotNull("No debe ser null",card.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,card.getId().longValue());


    Card card2 = cardDao.findByAccountId(card.getAccountId());

    Assert.assertNotNull("No debe ser null",card2);
    Assert.assertNotNull("No debe ser null",card2.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,card2.getId().longValue());
    Assert.assertEquals("Dben ser iguales",card.getAccountId(),card2.getAccountId());

  }


  @Test
  public void testNoExistCard() {
    cardDao.setEm(createEntityManager());
    Card card = cardDao.find(RandomUtils.nextLong());
    Assert.assertNull("Debe ser null", card);

  }

}
