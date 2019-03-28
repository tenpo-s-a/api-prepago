package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.dao.UserDao;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.prepaid.model.v11.DocumentType;
import cl.multicaja.prepaid.model.v11.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

public class Test_dao_user extends TestBaseUnit {

  private UserDao userDao = new UserDao();

  @Before
  public void clearData(){
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.%s",getSchema(),"prp_movimiento"));
    getDbUtils().getJdbcTemplate().execute(String.format("delete from %s.%s",getSchema(),"prp_usuario"));
  }
  @Test
  public void testInsert() {
    userDao.setEm(createEntityManager());
    User user = new User();
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    user.setUserId(0L);
    user.setLastName(getRandomString(10));
    user.setName(getRandomString(10));
    user.setStatus(PrepaidUserStatus.ACTIVE);
    user.setLevel(0);
    user.setDocumentNumber(getRandomNumericString(10));
    user.setDocumentType(DocumentType.DNI_CL);

    user = userDao.insert(user);
    Assert.assertNotNull("No debe ser null",user);
    Assert.assertNotNull("No debe ser null",user.getId());
    Assert.assertNotEquals("El Id no debe ser 0",0,user.getId().longValue());

  }


}
