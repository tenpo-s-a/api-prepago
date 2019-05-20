package cl.multicaja.prepaid.helpers;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.utils.AESCryptCardUtilImpl;
import cl.multicaja.prepaid.utils.AzureCryptCardUtilImpl;
import cl.multicaja.prepaid.utils.CryptCardUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CryptHelper {

  private static Log log = LogFactory.getLog(TecnocomServiceHelper.class);
  private ConfigUtils configUtils;
  private static CryptHelper instance;
  private static CryptCardUtil cryptCardUtil;


  public static CryptHelper getInstance() {
    if (instance == null) {
      instance = new CryptHelper();
    }
    return instance;
  }

  private ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  public String encryptPan(String data){
   return  getCryptCardUtil().encryptPan(data,getConfigUtils().getProperty("crypt.password",""));
  }

  public String decryptPan(String data){
    return  getCryptCardUtil().decryptPan(data,getConfigUtils().getProperty("crypt.password",""));
  }

  private synchronized CryptCardUtil getCryptCardUtil() {
    if (cryptCardUtil == null) {
      boolean useAzure = getConfigUtils().getPropertyBoolean("azure.clien.enabled", false);
      if (useAzure) {
        String azureClientId = getConfigUtils().getProperty("azure.clien.id","");
        String azureClientSecret = getConfigUtils().getProperty("azure.client.secret","");
        String azureVaultUri = getConfigUtils().getProperty("azure.vault.uri","");
        cryptCardUtil = new AzureCryptCardUtilImpl(azureClientId,azureClientSecret,azureVaultUri);
      }
      else {
        cryptCardUtil = new AESCryptCardUtilImpl();
      }
    }
    return cryptCardUtil;
  }

}
