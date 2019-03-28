package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.dao.CardDao;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v11.Card;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

public class Test_dao_card extends TestBaseUnit{

  private CardDao cuentaDao = new CardDao();

  @Before
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("delete  from %s.%s",getSchema(),"prp_tarjeta"));

  }

  @Test
  public void testInsert() {
    cuentaDao.setEm(createEntityManager());
    Card card = new Card();
    card.setCreatedAt(LocalDateTime.now());
    card.setUpdatedAt(LocalDateTime.now());
    card.setStatus(PrepaidCardStatus.ACTIVE);
    card.setAccountId(0l);
    card.setCardName(getRandomString(10));
    card.setPan(getRandomNumericString(10));
    card.setCryptedPan(getRandomString(20));
    card.setPanHash(getRandomString(20));

    card = cuentaDao.insert(card);
    Assert.assertNotNull("No debe ser null",card);
    Assert.assertNotNull("No debe ser null",card.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,card.getId().longValue());
  }


}
