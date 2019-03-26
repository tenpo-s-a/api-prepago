package cl.multicaja.prepaid.helpers.tenpo.model;

import java.util.UUID;

public class User {

  private String address;
  private String apartment;
  private String countryCode;
  private String document;
  private String documentNumber;
  private String documentType;
  private String email;
  private String firstName;
  private UUID id;
  private UUID idIdentityProvider;
  private String lastName;
  private String phone;
  private String qrKey;
  private String street;
  private String streetNumber;
  private UUID userId;
  private enum state{
    PENDING,
    VALIDATED,
    UNCONFIRMED,
    ACTIVE,
    BLOCKED
  };
  private enum level{
    LEVEL_1,
    LEVEL_2
  };
  private enum plan{
    FREE,
    PREMIUM
  };

}
