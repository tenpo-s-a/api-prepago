package cl.multicaja.prepaid.utils;

import cl.multicaja.prepaid.external.azure.CryptCardAzureImpl;
import cl.multicaja.prepaid.util.CryptCard;


public class AzureEncryptCardUtilImpl implements EncryptCardUtil {
  private CryptCard cryptCard = CryptCardAzureImpl.getInstance();

  public AzureEncryptCardUtilImpl(){
    super();
  }
  public AzureEncryptCardUtilImpl(String clientId, String clientKey, String vaulturi) {
    super();
    if (cryptCard instanceof CryptCardAzureImpl) {
      CryptCardAzureImpl cryptCardAzure = (CryptCardAzureImpl) cryptCard;
      cryptCardAzure.setClientId(clientId);
      cryptCardAzure.setClientKey(clientKey);
      cryptCardAzure.setVaultUri(vaulturi);
    }
  }

  public CryptCard getCryptCard() {
        return cryptCard;
    }

    @Override
    public String encryptPan(String pan, String password) {
        return getCryptCard().encryptPan(pan, password);
    }

    @Override
    public String decryptPan(String cryptedPan, String password) {
        return getCryptCard().decryptPan(cryptedPan, password);
    }

    private static AzureEncryptCardUtilImpl instance;


    public static AzureEncryptCardUtilImpl getInstance() {
        if (null == instance)
            instance = new AzureEncryptCardUtilImpl(null, null, null);
        return instance;
    }
}
