package cl.multicaja.prepaid.helpers.users.model;

public class UserPath {

  private static String PATH = "/1.0/users";

  public static final String SOFT_SIGNUP = String.format("%s/soft_signup",PATH);
  public static String GET_SOFT_SIGNUP(Long userId){
    return String.format("%s/soft_signup/%s",PATH,userId);
  }
  public static String POST_SIGNUP(Long userId){
    return String.format("%s/%s/finish_signup",PATH,userId);
  }
  public static String FINISH_SIGNUP(Long userId){return String.format("%s/%s/finish_signup",PATH,userId);}
  public static String GET_USER_BY_ID(Long userId){
    return String.format("%s/%s",PATH,userId);
  }
  public static String GET_USERS = "%s/";
  public static String UPDATE_PASSWORD(Long userId){
    return String.format("%s/%s/update_password",PATH,userId);
  }
  public static String CHECK_PASSWORD(Long userId){
    return String.format("%s/%s/check_password",PATH,userId);
  }
  public static String UPDATE_PERSONAL_DATA(Long userId){
    return String.format("%s/%s/update_personal_data",PATH,userId);
  }
  public static String VALIDATE_RUT(Long userId){
    return String.format("%s/%s/validate_rut",PATH,userId);
  }
  public static String UPDATE_EMAIL(Long userId){
    return String.format("%s/%s/update_email",PATH,userId);
  }
  public static String UPDATE_CELLPHONE(Long userId){
    return String.format("%s/%s/update_cellphone",PATH,userId);
  }
  public static String SEND_MAIL(Long userId){
    return String.format("%s/%s/mail",PATH,userId);
  }
  public static String VERIFY_MAIL(Long userId){
    return String.format("%s/%s/mail",PATH,userId);
  }
  public static String SEND_SMS(Long userId){
    return String.format("%s/%s/sms",PATH,userId);
  }
  public static String VERIFY_SMS(Long userId){
    return String.format("%s/%s/sms",PATH,userId);
  }

}
