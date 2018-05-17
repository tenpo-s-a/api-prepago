package cl.multicaja.cdt.ejb.v10;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class CdtEJBBean10 implements CdtEJB10{

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);
  private ConfigUtils configUtils;
  private DBUtils dbUtils;
  private NumberUtils numberUtils = NumberUtils.getInstance();
  private final String SP_CARGA_FASES_MOVIMIENTOS  = "mc_cdt_carga_fases_movimientos_v10";
  private final String SP_CREA_MOVIMIENTO_CUENTA = "mc_cdt_crea_movimiento_cuenta_v10";

  @Override
  public CdtTransaction10 addCdtTransaction(Map<String, Object> headers, CdtTransaction10 cdtTransaction10) throws Exception {
    dbUtils = getDbUtils();
    configUtils = getConfigUtils();

    if(StringUtils.isAllBlank(cdtTransaction10.getAccountId().trim())) {
     throw new ValidationException(2);
    }
    if(cdtTransaction10.getTransactionType() == null) {
      throw new ValidationException(2);
    }
    if(cdtTransaction10.getExternalTransactionId() == null) {
      throw new ValidationException(2);
    }
    if(cdtTransaction10.getAmount() == null || cdtTransaction10.getAmount().doubleValue() == 0){
      throw new ValidationException(2);
    }

    Object[] params = {cdtTransaction10.getTransactionType().getName() , new NullParam(Types.NUMERIC),new OutParam("fase",Types.OTHER),new OutParam("numerror",Types.VARCHAR),new OutParam("msjerror",Types.VARCHAR)};
    Map<String,Object> outputData = dbUtils.execute(getSchema()+"."+SP_CARGA_FASES_MOVIMIENTOS,params);

    List lstFases = (List) outputData.get("fase");
    if(lstFases == null || lstFases.size()== 0) {
      throw new ValidationException(2);
    }
    HashMap<String,Object> mapFase = (HashMap<String, Object>) lstFases.get(0);
    Long faseId = (Long) mapFase.get("id");
     params = new Object[] {
        cdtTransaction10.getAccountId(),
        faseId,
        cdtTransaction10.getTransactionReference(),
        cdtTransaction10.getExternalTransactionId(),
        cdtTransaction10.getGloss(),
        cdtTransaction10.getAmount(),
        new OutParam("IdMovimiento", Types.NUMERIC),
        new OutParam("NumError", Types.VARCHAR),
        new OutParam("MsjError", Types.VARCHAR)
     };
    outputData = dbUtils.execute( getSchema() +"."+ SP_CREA_MOVIMIENTO_CUENTA, params);

    String numError = (String) outputData.get("NumError");
    String msjError = (String) outputData.get("MsjError");
    if(numError.equals("0")){
      cdtTransaction10.setTransactionReference(((BigDecimal)outputData.get("IdMovimiento")).longValue());
    } else {
      log.error("[CdtEJBBean10][addCdtTransaction] NumError: "+numError+" MsjError: "+msjError);
      long lNumError = numberUtils.toLong(numError,-1);
      if(lNumError != -1 && lNumError > 10000)
        throw new ValidationException(4).setData(new KeyValue("value",msjError));
      else
        throw new ValidationException(2);
    }
    return cdtTransaction10;
  }

  /**
   *
   * @return
   */
  public ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  /**
   *
   * @return
   */
  public DBUtils getDbUtils() {
    if (this.dbUtils == null) {
      this.dbUtils = new DBUtils(this.getConfigUtils());
    }
    return this.dbUtils;
  }

  /**
   *
   * @return
   */
  private String getSchema() {
    return this.getConfigUtils().getProperty("schema.cdt");
  }
}
