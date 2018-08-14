package cl.multicaja.test.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import cl.multicaja.prepaid.ejb.v10.PrepaidUserEJBBean10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.users.ejb.v10.DataEJBBean10;
import cl.multicaja.users.ejb.v10.FilesEJBBean10;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.core.model.Errors.CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO;

/**
 * @author abarazarte
 **/
@RunWith(MockitoJUnitRunner.class)
public class Test_PrepaidEJBBean10_uploadIdentityVerificationFiles {

  @Spy
  private UsersEJBBean10 usersEJBBean10;

  @Spy
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @Spy
  private FilesEJBBean10 filesEJBBean10;

  @Spy
  private DataEJBBean10 usersDataEJB10;

  @InjectMocks
  @Spy
  private PrepaidEJBBean10 prepaidEJBBean10;

  private Map<String, Object> headers = new HashMap<>();
  private UserFile f = new UserFile();

  @Test
  public void userIdMcNull() throws Exception {
    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(null, null, null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error userIdMc null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void userIdMc0() throws Exception {
    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(null, 0L, null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error userIdMc 0", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void filesNull() throws Exception {
    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(null, Long.MAX_VALUE, null);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error files null 0", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void filesEmpty() throws Exception {
    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(null, Long.MAX_VALUE, new HashMap<>());
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error files empty", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void fileUserIdFrontNull() throws Exception {

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_SELFIE", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(null, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error file USER_ID_FRONT null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void fileUserIdBackNull() throws Exception {

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(null, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error file USER_ID_BACK null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void fileUserSelfieNull() throws Exception {

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(null, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (BadRequestException ex) {
      Assert.assertEquals("Debe retornar error file USER_SELFIE null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcNull() throws Exception {
    Mockito.doReturn(null).when(usersEJBBean10).getUserById(Mockito.any(), Mockito.anyLong());

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(headers, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (NotFoundException ex) {
      Assert.assertEquals("Debe retornar error user null", CLIENTE_NO_EXISTE.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcDisabled() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.DISABLED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(Mockito.any(), Mockito.anyLong());

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(headers, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc disabled", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcLocked() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.LOCKED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(Mockito.any(), Mockito.anyLong());

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(headers, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc locked", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcDeleted() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.DELETED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(Mockito.any(), Mockito.anyLong());

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(headers, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc deleted", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void userMcPreregistered() throws Exception {
    User user = Mockito.mock(User.class);
    user.setGlobalStatus(UserStatus.PREREGISTERED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(Mockito.any(), Mockito.anyLong());

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(headers, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error userMc preregistered", CLIENTE_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void prepaidUserNull() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(Mockito.any(), Mockito.anyLong());
    Mockito.doReturn(null).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(Mockito.any(), Mockito.anyLong());

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(headers, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (NotFoundException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser null", CLIENTE_NO_TIENE_PREPAGO.getValue(), ex.getCode());
    }
  }

  @Test
  public void prepaidUserDisabled() throws Exception {
    User user = new User();
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.DISABLED);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(Mockito.any(), Mockito.anyLong());
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(Mockito.any(), Mockito.anyLong());

    Map<String, UserFile> files = new HashMap<>();
    files.put("USER_ID_BACK", f);
    files.put("USER_ID_FRONT", f);
    files.put("USER_SELFIE", f);

    try{
      prepaidEJBBean10.uploadIdentityVerificationFiles(headers, Long.MAX_VALUE, files);
      Assert.fail("should not be here");
    } catch (ValidationException ex) {
      Assert.assertEquals("Debe retornar error prepaidUser disabled", CLIENTE_PREPAGO_BLOQUEADO_O_BORRADO.getValue(), ex.getCode());
    }
  }

  @Test
  public void uploadIdentityVerificationFiles() throws Exception {
    User user = new User();
    user.setId(Long.MAX_VALUE);
    Rut rut = new Rut();
    rut.setValue(11111111);
    user.setRut(rut);
    user.setGlobalStatus(UserStatus.ENABLED);

    User user2 = new User();
    user2.setId(Long.MAX_VALUE);
    Rut rut2 = new Rut();
    rut.setValue(11111111);
    user2.setRut(rut2);
    user2.setGlobalStatus(UserStatus.ENABLED);
    user2.setNameStatus(NameStatus.IN_REVIEW);

    PrepaidUser10 prepaidUser = new PrepaidUser10();
    prepaidUser.setStatus(PrepaidUserStatus.ACTIVE);

    Mockito.doReturn(user).when(usersEJBBean10).getUserById(Mockito.any(), Mockito.anyLong());
    Mockito.doReturn(prepaidUser).when(prepaidUserEJBBean10).getPrepaidUserByUserIdMc(Mockito.any(), Mockito.anyLong());
    Mockito.doReturn(user2).when(usersDataEJB10).updateNameStatus(Mockito.any(), Mockito.anyLong(), Mockito.any(NameStatus.class));

    Mockito.doReturn(f).when(filesEJBBean10).createUserFile(Mockito.any(), Mockito.anyLong(), Mockito.nullable(Long.class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.notNull(), Mockito.notNull());

    Map<String, UserFile> files = new HashMap<>();
    UserFile idFront = new UserFile();
    idFront.setMimeType("asdsadsad");
    idFront.setLocation("sadsadsa");
    files.put("USER_ID_FRONT", idFront);

    UserFile idBack= new UserFile();
    idBack.setMimeType("fasdas");
    idBack.setLocation("fasdasd");
    files.put("USER_ID_BACK", idBack);

    UserFile selfie = new UserFile();
    selfie.setMimeType("dgdsgdsg");
    selfie.setLocation("werewrewr");
    files.put("USER_SELFIE", selfie);

    User u = prepaidEJBBean10.uploadIdentityVerificationFiles(headers, Long.MAX_VALUE, files);

    Assert.assertEquals("Debe tener nameStatus IN_REVIEW", NameStatus.IN_REVIEW, u.getNameStatus());
  }
}
