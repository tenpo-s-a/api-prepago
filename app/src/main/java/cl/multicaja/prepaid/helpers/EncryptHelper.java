package cl.multicaja.prepaid.helpers;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.utils.AESEncryptCardUtilImpl;
import cl.multicaja.prepaid.utils.AzureEncryptCardUtilImpl;
import cl.multicaja.prepaid.utils.EncryptCardUtil;
import cl.multicaja.prepaid.utils.EnvironmentUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EncryptHelper {

  private static Log log = LogFactory.getLog(EncryptHelper.class);

  private ConfigUtils configUtils;
  private static EncryptHelper instance;
  private static EncryptCardUtil encryptCardUtil;

  private static final String AZURE_KEYVAULT_CLIENT_ID = "AZURE_KEYVAULT_CLIENT_ID";
  private static final String AZURE_KEYVAULT_CLIENT_SECRET = "AZURE_KEYVAULT_CLIENT_SECRET";
  private static final String AZURE_KEYVAULT_URL = "AZURE_KEYVAULT_URL";
  private static final String AZURE_KEYVAULT_ENABLED = "AZURE_KEYVAULT_KEY_ENABLED";
  private static final String ENCRYPT_PASSWORD  = "AZURE_KEYVAULT_KEY_NAME";

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

  public String encryptPan(String data) {
    ConfigUtils config = getConfigUtils();
    String encryptPassword = EnvironmentUtil.getVariable(ENCRYPT_PASSWORD, () ->
      config.getProperty("encrypt.password",""));
   return  getCryptCardUtil().encryptPan(data,encryptPassword);
  }

  public String decryptPan(String data){
    ConfigUtils config = getConfigUtils();
    String encryptPassword = EnvironmentUtil.getVariable(ENCRYPT_PASSWORD, () ->
      config.getProperty("encrypt.password",""));
    return  getCryptCardUtil().decryptPan(data,encryptPassword);
  }

  private synchronized EncryptCardUtil getCryptCardUtil() {
    if (encryptCardUtil == null) {
      ConfigUtils config = getConfigUtils();

      String useAzure = EnvironmentUtil.getVariable(AZURE_KEYVAULT_ENABLED, () ->
        config.getProperty("azure.client.enabled", "false"));
      if (Boolean.valueOf(useAzure)) {
        String azureClientId = EnvironmentUtil.getVariable(AZURE_KEYVAULT_CLIENT_ID, () ->
          config.getProperty("azure.client.id",""));
        String azureClientSecret = EnvironmentUtil.getVariable(AZURE_KEYVAULT_CLIENT_SECRET, () ->
          config.getProperty("azure.client.secret",""));
        String azureVaultUri = EnvironmentUtil.getVariable(AZURE_KEYVAULT_URL, () ->
          config.getProperty("azure.vault.uri",""));

        encryptCardUtil = new AzureEncryptCardUtilImpl(azureClientId,azureClientSecret,azureVaultUri);
      }
      else {
        encryptCardUtil = new AESEncryptCardUtilImpl();
      }
    }
    return encryptCardUtil;
  }

}
