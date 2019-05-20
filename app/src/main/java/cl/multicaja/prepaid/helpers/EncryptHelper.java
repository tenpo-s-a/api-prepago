package cl.multicaja.prepaid.helpers;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.helpers.tecnocom.TecnocomServiceHelper;
import cl.multicaja.prepaid.utils.AESEncryptCardUtilImpl;
import cl.multicaja.prepaid.utils.AzureEncryptCardUtilImpl;
import cl.multicaja.prepaid.utils.EncryptCardUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EncryptHelper {

  private static Log log = LogFactory.getLog(TecnocomServiceHelper.class);
  private ConfigUtils configUtils;
  private static EncryptHelper instance;
  private static EncryptCardUtil encryptCardUtil;


  public static EncryptHelper getInstance() {
    if (instance == null) {
      instance = new EncryptHelper();
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

  private synchronized EncryptCardUtil getCryptCardUtil() {
    if (encryptCardUtil == null) {
      boolean useAzure = getConfigUtils().getPropertyBoolean("azure.client.enabled", false);
      if (useAzure) {
        String azureClientId = getConfigUtils().getProperty("azure.client.id","");
        String azureClientSecret = getConfigUtils().getProperty("azure.client.secret","");
        String azureVaultUri = getConfigUtils().getProperty("azure.vault.uri","");
        encryptCardUtil = new AzureEncryptCardUtilImpl(azureClientId,azureClientSecret,azureVaultUri);
      }
      else {
        encryptCardUtil = new AESEncryptCardUtilImpl();
      }
    }
    return encryptCardUtil;
  }

}
