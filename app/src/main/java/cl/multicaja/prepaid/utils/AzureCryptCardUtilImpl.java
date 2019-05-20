package cl.multicaja.prepaid.utils;

import cl.multicaja.prepaid.external.azure.CryptCardAzureImpl;
import cl.multicaja.prepaid.util.CryptCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


public class AzureCryptCardUtilImpl implements CryptCardUtil {
    private CryptCard cryptCard = CryptCardAzureImpl.getInstance();

  public AzureCryptCardUtilImpl(String clientId, String clientKey, String vaulturi) {
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

    private static AzureCryptCardUtilImpl instance;


    public static AzureCryptCardUtilImpl getInstance() {
        if (null == instance)
            instance = new AzureCryptCardUtilImpl(null, null, null);
        return instance;
    }
}
