package cl.multicaja.test.unit;

import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.helpers.tenpo.TenpoApiCall;
import cl.multicaja.prepaid.helpers.tenpo.model.Level;
import cl.multicaja.prepaid.helpers.tenpo.model.State;
import cl.multicaja.prepaid.helpers.tenpo.model.TenpoUser;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class Test_PrepaidEJBBean10_validateTempoUser {


  @Spy
  private TenpoApiCall tenpoApiCall;

  @Spy
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @Spy
  @InjectMocks
  private PrepaidEJBBean10 prepaidEJBBean10;


  @Test
  public void validateTenpoUser() throws Exception {

    TenpoUser tenpoUser = getTempoUser();
    Mockito.doReturn(tenpoUser).when(tenpoApiCall).getUserById(Mockito.any(UUID.class));

    PrepaidUser10 prepaidUsertmp = getPrepaidUser(tenpoUser);
    Mockito.doReturn(prepaidUsertmp).when(prepaidUserEJBBean10).createUser(Mockito.isNull(),Mockito.any());

    try {
      PrepaidUser10 prepaidUser10 = prepaidEJBBean10.validateTempoUser(UUID.randomUUID().toString());
      Assert.assertNotNull("El objeto usuario no debe ser null",prepaidUser10);
    }
    catch (Exception e){
      Assert.fail("No debe caer aca");
    }
  }

  private PrepaidUser10 getPrepaidUser(TenpoUser tenpoUser){
    PrepaidUser10 prepaidUser10 = new PrepaidUser10();
    prepaidUser10.setId(Long.MAX_VALUE);
    prepaidUser10.setStatus(PrepaidUserStatus.ACTIVE);
    prepaidUser10.setDocumentNumber(tenpoUser.getTributaryIdentifier());
    prepaidUser10.setName(tenpoUser.getFirstName());
    prepaidUser10.setLastName(tenpoUser.getLastName());
    prepaidUser10.setUuid(tenpoUser.getId().toString());
    return prepaidUser10;
  }

  private TenpoUser getTempoUser(){
    TenpoUser tenpoUser = new TenpoUser();
    tenpoUser.setDocumentNumber("1111111");
    tenpoUser.setDocumentType("XXX");
    tenpoUser.setFirstName("ajsfkalksjf");
    tenpoUser.setLastName("asdasdas");
    tenpoUser.setTributaryIdentifier("16616881-3");
    tenpoUser.setLevel(Level.LEVEL_1);
    tenpoUser.setState(State.ACTIVE);
    tenpoUser.setId(UUID.randomUUID());
    tenpoUser.setUserId(UUID.randomUUID());

    return tenpoUser;
  }

}
